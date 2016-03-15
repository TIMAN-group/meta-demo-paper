import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Paths;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public
class Search {
    final static String dataset = "gov2";
    final static String prefix = "/home/sean/data/" + dataset + "/";
    final static String indexPath = dataset + "-index";
    final static String queryList = dataset + "-queries.txt";
    static int queryId = 300;  // start query ID

   public
    static void main(String[] args) throws Exception {
        Date start = new Date();
        IndexReader reader =
            DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        indexSearcher.setSimilarity(new BM25Similarity());
        Analyzer analyzer = new SpecialAnalyzer();
        BufferedReader br = new BufferedReader(new FileReader(queryList));
        String line = null;
        while ((line = br.readLine()) != null) {
            String escaped = QueryParser.escape(line);
            Query query = new QueryParser("text", analyzer).parse(escaped);
            TopDocs docs = indexSearcher.search(query, 100);
            ScoreDoc[] hits = docs.scoreDocs;
            for (ScoreDoc hit : hits) {
                Document doc = indexSearcher.doc(hit.doc);
                System.out.println("" + queryId + "\t_\t" + doc.get("name") +
                                   "\t_\t" + hit.score + "\t_");
            }
            ++queryId;
        }

        br.close();
        Date end = new Date();

        long mseconds = end.getTime() - start.getTime();
        System.out.println(mseconds + "ms elapsed");
    }
}
