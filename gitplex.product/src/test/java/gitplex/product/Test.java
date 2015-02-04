package gitplex.product;

import java.io.File;

import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;

public class Test extends AbstractGitTest {

	@org.junit.Test
	public void test() {
		Git git = new Git(new File("w:\\linux\\.git"));
		for (TreeNode node: git.listTree("master~1000", "include/media")) {
			if (node.getMode() == FileMode.TYPE_FILE) {
				System.out.println(new String(git.readBlob(new BlobInfo("master~1000", node.getPath(), node.getMode()))));
			}
		}
	}
	
}
