package gitplex.product;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Throwables;
import com.pmease.commons.util.Charsets;

public class Test {

	@org.junit.Test
	public void test3() throws IOException {
		Git jgit = Git.open(new File("w:\\mozilla\\.git"));
		AnyObjectId id = jgit.getRepository().resolve("6f5121bd65873a784b61f6b8959ec5197ecb5d23");
		System.out.println(jgit.getRepository().hasObject(id));
		jgit.close();
	}
	
	@org.junit.Test
	public void test2() throws IOException {
		Git jgit = Git.open(new File("w:\\mozilla\\.git"));
		try {
			org.eclipse.jgit.lib.Repository jgitRepo = jgit.getRepository();
			RevWalk revWalk = new RevWalk(jgitRepo);
			RevCommit commit = revWalk.parseCommit(jgitRepo.resolve("master"));
			
			long time = System.currentTimeMillis();
			File dir = new File("W:\\mozilla");
			for (File file: new File("W:\\mozilla\\content\\html\\content\\src").listFiles()) {
				String path = file.getAbsolutePath().substring(dir.getAbsolutePath().length()+1).replace('\\', '/');
				TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, path, commit.getTree());
				jgitRepo.open(treeWalk.getObjectId(0)).getCachedBytes();
			}
			System.out.println(System.currentTimeMillis()-time);
		} catch (Exception e) {
			Throwables.propagate(e);
		} finally {
			jgit.close();
		}
	}
	
	@org.junit.Test
	public void test() throws IOException {
		Directory directory = FSDirectory.open(new File("w:\\temp\\index"));
		Analyzer analyzer = new StandardAnalyzer();					
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LATEST, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		
		try (IndexReader indexReader = DirectoryReader.open(directory); IndexWriter writer = new IndexWriter(directory, iwc)) {
			Git jgit = Git.open(new File("w:\\linux\\.git"));
			try {
				org.eclipse.jgit.lib.Repository jgitRepo = jgit.getRepository();

				ObjectId commitId = jgitRepo.resolve("master");
				RevWalk revWalk = new RevWalk(jgitRepo);
				RevCommit commit = revWalk.parseCommit(commitId);
				RevTree revTree = commit.getTree();
				TreeWalk treeWalk = new TreeWalk(jgitRepo);
				treeWalk.addTree(revTree);
				treeWalk.setRecursive(true);

				while (treeWalk.next()) {
					if ((treeWalk.getRawMode(0) & FileMode.TYPE_MASK) == FileMode.TYPE_FILE) {
						ObjectLoader loader = jgitRepo.open(treeWalk.getObjectId(0));
						if (loader.getSize() <= 1000000) {
							byte[] bytes = loader.getCachedBytes();
							Charset charset = Charsets.detectFrom(bytes);
							if (charset != null) {
								String content = new String(bytes, charset);
								Document doc = new Document();

								doc.add(new StringField("blobId", treeWalk.getObjectId(0).getName(), Store.YES));
								doc.add(new StringField("path", treeWalk.getPathString(), Store.YES));
								doc.add(new TextField("content", content, Store.NO));
								
								IndexSearcher searcher = new IndexSearcher(indexReader);
								BooleanQuery query = new BooleanQuery();
								query.add(new TermQuery(new Term("blobId", treeWalk.getObjectId(0).getName())), Occur.MUST);
								query.add(new TermQuery(new Term("path", treeWalk.getPathString())), Occur.MUST);
								
								TopDocs docs = searcher.search(query, 1);
								if (docs.totalHits == 0) {
									writer.addDocument(doc);
								}
							} 
						}
					}
				}
				writer.commit();
			} catch (Exception e) {
				writer.rollback();
				Throwables.propagate(e);
			} finally {
				jgit.close();
			}
		}
	}
	
	@org.junit.Test
	public void test4() throws IOException {
		Directory directory = FSDirectory.open(new File("w:\\temp\\index"));
		Analyzer analyzer = new StandardAnalyzer();					
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LATEST, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		
		try (IndexWriter writer = new IndexWriter(directory, iwc)) {
			Document doc1 = new Document();
			doc1.add(new TextField("animals", "tiger lion", Store.YES));
			doc1.add(new TextField("fruits", "apple pear", Store.YES));
			writer.addDocument(doc1);
			
			Document doc2 = new Document();
			doc2.add(new TextField("animals", "lion tiger", Store.YES));
			doc2.add(new TextField("fruits", "apple pear", Store.YES));
			writer.addDocument(doc2);
			
			writer.commit();
		}
	}
	
	@org.junit.Test
	public void test5() throws IOException {
		Directory directory = FSDirectory.open(new File("w:\\temp\\index"));

		try (IndexReader reader = DirectoryReader.open(directory)) {
			final IndexSearcher searcher = new IndexSearcher(reader);
			final AtomicInteger count = new AtomicInteger(0);
			
			for (int i=0; i<10; i++) {
			long time = System.currentTimeMillis();
			searcher.search(new TermQuery(new Term("content", "void")), new Collector() {

				@Override
				public void setScorer(Scorer scorer) throws IOException {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void collect(int doc) throws IOException {
					Document docu = searcher.doc(doc);
					docu.get("blobId");
					docu.get("path");
					count.incrementAndGet();
				}

				@Override
				public void setNextReader(AtomicReaderContext context)
						throws IOException {
					
				}

				@Override
				public boolean acceptsDocsOutOfOrder() {
					// TODO Auto-generated method stub
					return false;
				}
				
			});
			System.out.println("count: " + count.get());
			System.out.println("time: " + (System.currentTimeMillis()-time));
			}
		}
	}
}