#include <vector>
#include <chrono>
#include <fstream>
#include <iostream>
#include <string>

#include "indri/IndexEnvironment.hpp"
#include "indri/QueryEnvironment.hpp"

std::vector<std::string> get_stopwords()
{
    std::cout << "Load stopwords" << std::endl;
    std::ifstream in{"/home/sean/projects/meta/data/lemur-stopwords.txt"};
    std::vector<std::string> stopwords;
    std::string word;
    while (in >> word)
        stopwords.push_back(word);
    return stopwords;
}

void setup_index_env(indri::api::IndexEnvironment& ienv,
                     const std::string& index_name, size_t memory)
{
    std::cout << "Setup index env" << std::endl;
    ienv.create(index_name);
    ienv.setMemory(memory);
    ienv.setStemmer("porter");
    ienv.setStopwords(get_stopwords());
    ienv.setStoreDocs(false);
}

void create_line_index(indri::api::IndexEnvironment& ienv, size_t memory,
                       const std::string& dataset, const std::string& prefix)
{
    std::cout << "Create line index" << std::endl;

    std::string name = dataset + "-index";
    setup_index_env(ienv, name, memory);

    size_t num_indexed = 0;
    std::string line;
    auto dataset_path = prefix + "/" + dataset + ".dat";
    std::cout << "dataset path: " << dataset_path << std::endl;
    std::ifstream docs{dataset_path};
    std::ifstream name_file{prefix + "/" + "/metadata.dat"};
    while (docs.good())
    {
        indri::parse::MetadataPair mdata;
        mdata.key = "docno"; // "document name"
        std::string title;
        std::getline(name_file, title);
        mdata.value = title.c_str();          // this is C++
        mdata.valueLength = title.size() + 1; // definitely C++

        std::getline(docs, line);
        ienv.addString(line, "text", {mdata});
        ++num_indexed;
        if (num_indexed % 1000 == 0)
        {
            std::cout << " Indexed " << num_indexed << " docs...    \r";
            cout.flush();
        }
    }

    ienv.close();
    std::cout << std::endl;
}

int main(int argc, char* argv[])
{
    if (argc != 3)
    {
        std::cerr << "Usage: " << argv[0] << " dataset_name build_index"
                  << std::endl;
        return 1;
    }

    bool build_index = (argv[2][0] == 'y' || argv[2][0] == 't');
    std::string dataset{argv[1]};
    size_t memory = 2ul * 1024ul * 1024ul * 1024ul; // 2 GB

    auto start = std::chrono::steady_clock::now();

    indri::api::IndexEnvironment ienv;
    if (build_index)
    {
        std::string prefix = "/home/sean/data/" + dataset;
        create_line_index(ienv, memory, dataset, prefix);
    }
    auto end = std::chrono::steady_clock::now();
    auto time = std::chrono::duration_cast<std::chrono::seconds>(end - start);
    std::cout << "Index creation time: " << time.count() << " seconds."
              << std::endl;

    // run queries on index

    std::ifstream queries{dataset + "-queries.txt"};
    std::string content;

    size_t query_id = 300;
    indri::api::QueryEnvironment qenv;
    std::string name = dataset + "-index";
    qenv.addIndex(name);
    qenv.setMemory(memory);
    qenv.setStopwords(get_stopwords());
    start = std::chrono::steady_clock::now();
    while (std::getline(queries, content))
    {
        size_t num_results = 100;

        std::vector<indri::api::ScoredExtentResult> results
            = qenv.runQuery(content, num_results);

        std::vector<std::string> names
            = qenv.documentMetadata(results, "docno");

        size_t res = 0;
        for (auto& name : names)
        {
            std::cout << query_id << "\t_\t" << name << "\t_\t"
                      << results[res].score << "\t_" << std::endl;
            ++res;
        }
        ++query_id;
    }

    qenv.close();
    end = std::chrono::steady_clock::now();
    auto rtime
        = std::chrono::duration_cast<std::chrono::milliseconds>(end - start);
    std::cout << "Retrieval time: " << rtime.count() << "ms" << std::endl;
}
