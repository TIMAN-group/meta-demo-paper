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
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

//import .NoPositionsTextField;

public
class Index {
   private
    static IndexWriter setupIndex(String indexPath) throws IOException {
        Analyzer analyzer = new SpecialAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE);
        config.setRAMBufferSizeMB(4096.0);
        FSDirectory dir;
        IndexWriter writer = null;
        dir = FSDirectory.open(Paths.get(indexPath));
        writer = new IndexWriter(dir, config);
        return writer;
    }

   public
    static void main(String[] args) throws Exception {

        final String dataset = args[0];
        final String prefix = "/media/" + dataset + "/";
        final String indexPath = dataset + "-index";

        Date start = new Date();

        IndexWriter writer = setupIndex(indexPath);
        String inputFile = prefix + dataset + ".dat";
        String metadata = prefix + "metadata.dat";
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        BufferedReader namebr = new BufferedReader(new FileReader(metadata));
        String line = null;
        String name = null;
        int indexed = 0;
        while ((line = br.readLine()) != null &&
               (name = namebr.readLine()) != null) {
            Document doc = new Document();
            doc.add(new StringField("name", name, Field.Store.YES));
            doc.add(new TextField("text", line, Field.Store.NO));
            // doc.add(new NoPositionsTextField("text", line, Field.Store.NO));
            writer.addDocument(doc);
            ++indexed;
            if (indexed % 10000 == 0)
                System.out.println(" Indexed " + indexed + " docs...");
        }

        br.close();
        namebr.close();
        writer.close();

        System.out.println("Indexed " + indexed + " total docs.");

        Date end = new Date();
        long seconds = (end.getTime() - start.getTime()) / 1000;
        if (seconds <= 60)
            System.out.println("Done!\n" + seconds + " seconds");
        else
            System.out.println("Done!\n" + (float)seconds / 60 + " minutes");
    }
}
