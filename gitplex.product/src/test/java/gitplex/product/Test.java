package gitplex.product;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FS;

public class Test {

	@org.junit.Test
	public void test() throws IOException, NoHeadException, GitAPIException {
		Repository repo = RepositoryCache.open(FileKey.exact(new File("w:\\temp\\repo\\.git"), FS.DETECTED));
		Git git = Git.wrap(repo);
		LogCommand log = git.log().add(repo.resolve("master"));
		Iterable<RevCommit> commits = log.call();
		Iterator<RevCommit> it = commits.iterator();
		int i=0;
		while (it.hasNext()) {
			RevCommit commit = it.next();
			i++;
		}
		System.out.println(i);
		repo.close();
	}	

}