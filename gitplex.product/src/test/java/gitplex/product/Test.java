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
		System.out.println(repo.resolve("ff4b285827060ef2fc3a29a6a8155a8db89b4863"));
		repo.close();
	}	

}