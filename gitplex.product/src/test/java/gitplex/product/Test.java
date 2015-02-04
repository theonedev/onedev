package gitplex.product;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import com.pmease.commons.git.AbstractGitTest;

public class Test extends AbstractGitTest {

	@org.junit.Test
	public void test() {
		/*
		Git git = new Git(new File("w:\\linux\\.git"));
		long len = 0;
		for (TreeNode node: git.listTree("master", "include/media/")) {
			len += git.readBlob(new BlobInfo("master", node.getPath(), node.getMode())).length;
		}
		System.out.println(len);
		*/
		
		try {
			Git git = Git.open(new File("w:\\mozilla\\.git"));

			RevWalk revWalk = new RevWalk(git.getRepository());
		    RevCommit commit = revWalk.parseCommit(git.getRepository().resolve("master"));	        
		    TreeWalk treeWalk = new TreeWalk(git.getRepository());
		    treeWalk.addTree(commit.getTree());
	        treeWalk.setFilter(PathFilter.create("profile"));
			treeWalk.setRecursive(true);
			long len = 0;
			while (treeWalk.next()) {
				 ObjectId objectId = treeWalk.getObjectId(0);
				 ObjectLoader loader = git.getRepository().open(objectId);
				 len += loader.getBytes().length;
			}
			System.out.println(len);
			
			revWalk = new RevWalk(git.getRepository());
		    commit = revWalk.parseCommit(git.getRepository().resolve("master~1000"));	        
		    treeWalk = new TreeWalk(git.getRepository());
		    treeWalk.addTree(commit.getTree());
	        treeWalk.setFilter(PathFilter.create("profile"));
			treeWalk.setRecursive(true);
			len = 0;
			while (treeWalk.next()) {
				 ObjectId objectId = treeWalk.getObjectId(0);
				 ObjectLoader loader = git.getRepository().open(objectId);
				 len += loader.getBytes().length;
			}
			System.out.println(len);
			
			git.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
