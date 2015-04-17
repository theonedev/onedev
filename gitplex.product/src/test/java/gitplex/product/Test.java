package gitplex.product;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;

import com.pmease.commons.git.LastCommitInfo;
import com.pmease.commons.git.LastCommitsOfChildren;

public class Test {

	@org.junit.Test
	public void test() throws IOException, NoHeadException, GitAPIException {
		Repository repo = new FileRepository(new File("w:\\linux\\.git"));
		AnyObjectId id = repo.resolve("master");
		String dir = "";
		for (Map.Entry<String, LastCommitInfo> entry: new LastCommitsOfChildren(repo, id, dir).entrySet()) {
			
		}
		repo.close();
	}
	
}