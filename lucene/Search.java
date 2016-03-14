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
        int i = 1;
        while (((line = br.readLine()) != null) && i <= 500) {
            String escaped = QueryParser.escape(line);
            Query query = new QueryParser("text", analyzer).parse(escaped);
            TopDocs docs = indexSearcher.search(query, 10);
            ScoreDoc[] hits = docs.scoreDocs;

            System.out.println("Running query " + (i++) + ": \"" + escaped +
                               "\"");
            int j = 1;
            for (ScoreDoc hit : hits) {
                Document doc = indexSearcher.doc(hit.doc);
                System.out.println(" " + (j++) + ". " + doc.get("name"));
            }
        }

        br.close();
        Date end = new Date();

        long mseconds = end.getTime() - start.getTime();
        System.out.println(mseconds + "ms elapsed");
    }
}
