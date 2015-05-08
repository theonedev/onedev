package gitplex.product;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.util.FS;

public class Test {

	@org.junit.Test
	public void test() throws IOException, NoHeadException, GitAPIException {
		Git git = Git.open(new File("w:\\linux\\.git"), FS.DETECTED);
		git.log().add(git.getRepository().resolve("master")).addPath("COPYING").setMaxCount(1).call().iterator();
		long time = System.currentTimeMillis();
		git.log().add(git.getRepository().resolve("master")).addPath("COPYING").setMaxCount(1).call().iterator();
		System.out.println(System.currentTimeMillis()-time);
		git.close();
	}
	
}