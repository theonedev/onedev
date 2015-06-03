package gitplex.product;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;

public class Test {

	@org.junit.Test
	public void test() throws IOException, NoHeadException, GitAPIException {
		Repository repo = Git.open(new File("w:\\temp\\repo\\.git"), FS.DETECTED).getRepository();
		RevTree revTree = new RevWalk(repo).parseCommit(repo.resolve("master")).getTree();
		TreeWalk treeWalk = TreeWalk.forPath(repo, "dir/module1", revTree);
		System.out.println(treeWalk.getObjectId(0));
		repo.close();
	}
	
}