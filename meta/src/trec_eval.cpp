/**
 * @file trec_eval.cpp
 * @author Sean Massung
 */

#include <vector>
#include <string>
#include <iostream>

#include "meta/util/time.h"
#include "meta/util/printing.h"
#include "meta/corpus/document.h"
#include "meta/index/inverted_index.h"
#include "meta/index/ranker/ranker_factory.h"

using namespace meta;

int main(int argc, char* argv[])
{
    if (argc != 2)
    {
        std::cerr << "Usage:\t" << argv[0] << " config.toml" << std::endl;
        return 1;
    }

    // Log to standard error
    logging::set_cerr_logging();

    // Create an inverted index based on the config file
    auto config = cpptoml::parse_file(argv[1]);
    auto idx = index::make_index<index::inverted_index>(*config);

    // Create a ranking class based on the config file.
    auto group = config->get_table("ranker");
    if (!group)
        throw std::runtime_error{"\"ranker\" group needed in config file!"};
    auto ranker = index::make_ranker(*group);

    // Get the path to the file containing queries
    auto query_path = config->get_as<std::string>("query-path");
    if (!query_path)
        throw std::runtime_error{
            "config file needs a \"query-path\" parameter"};
    std::ifstream queries{*query_path};

    auto query_id_param = config->get_as<int64_t>("query-start");
    if (!query_id_param)
        throw std::runtime_error{
            "config file needs a \"query-start\" parameter"};

    std::cout.sync_with_stdio(false);
    std::string content;
    auto query_id = *query_id_param;
    auto elapsed_seconds
        = common::time([&]()
                       {
                           while (std::getline(queries, content))
                           {
                               corpus::document query{doc_id{0}};
                               query.content(content);
                               auto ranking = ranker->score(*idx, query, 1000);
                               auto result_num = 1;
                               for (auto& result : ranking)
                               {
                                   auto mdata = idx->metadata(result.d_id);
                                   std::cout << query_id << "\t_\t"
                                             << *mdata.get<std::string>("name")
                                             << "\t" << result_num << "\t"
                                             << result.score << "\tMeTA\n";
                                   if (result_num++ == 1000)
                                       break;
                               }
                               ++query_id;
                           }
                       });

    std::cerr << "Elapsed time: " << elapsed_seconds.count() << "ms"
              << std::endl;
}
