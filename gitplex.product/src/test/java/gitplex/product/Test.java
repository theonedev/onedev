package gitplex.product;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NGramPhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.google.common.base.Throwables;
import com.pmease.gitplex.search.FieldConstants;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		try (Directory directory = FSDirectory.open(new File("w:\\temp\\index"))) {
			try (IndexReader reader = DirectoryReader.open(directory)) {
				IndexSearcher searcher = new IndexSearcher(reader);
//				SymbolQuery query = new SymbolQuery("tiger", true, false, Integer.MAX_VALUE);
				NGramPhraseQuery query = new NGramPhraseQuery(3);
				query.add(FieldConstants.BLOB_SYMBOLS.term("tig"));
				query.add(FieldConstants.BLOB_SYMBOLS.term("ige"));
				TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
				System.out.println(topDocs.totalHits);
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}	
}