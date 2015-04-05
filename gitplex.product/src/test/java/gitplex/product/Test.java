package gitplex.product;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.util.FS;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		Repository repo = RepositoryCache.open(FileKey.exact(new File("w:\\linux\\.git"), FS.DETECTED));
		repo.resolve("master~1000");
		repo.close();
		
		long time = System.currentTimeMillis();
		for (int i=0; i<1000; i++) {
			repo = RepositoryCache.open(FileKey.exact(new File("w:\\linux\\.git"), FS.DETECTED));
			repo.close();
		}
		System.out.println(System.currentTimeMillis()-time);
	}	

}