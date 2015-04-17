package gitplex.product;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

import com.pmease.commons.git.LastCommitInfo;
import com.pmease.commons.git.LastCommitsOfChildren;

public class Test {

	@org.junit.Test
	public void test() throws IOException, NoHeadException, GitAPIException {
		Repository repo = new FileRepository(new File("w:\\linux\\.git"));
		LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(repo, repo.resolve("master"), "Documentation");
		for (Map.Entry<String, LastCommitInfo> entry: lastCommits.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue().getId());
		}
		repo.close();
	}	
	
}