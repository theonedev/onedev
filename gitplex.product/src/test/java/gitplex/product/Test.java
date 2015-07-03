package gitplex.product;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

public class Test {

	@org.junit.Test
	public void test() throws IOException, RevisionSyntaxException, GitAPIException {
		try (	Repository repo = new FileRepository(new File("w:\\temp\\repo\\.git"));
				RevWalk revWalk = new RevWalk(repo)) {
			RevTree prevTree = revWalk.parseCommit(repo.resolve("master~1")).getTree();
			RevTree currentTree = revWalk.parseCommit(repo.resolve("master")).getTree();
			
			TreeWalk treeWalk = TreeWalk.forPath(repo, "dir/file", prevTree.getId(), currentTree.getId());

			System.out.println(treeWalk.getObjectId(0));
			System.out.println(treeWalk.getObjectId(1));
		}
		
	}
	
}