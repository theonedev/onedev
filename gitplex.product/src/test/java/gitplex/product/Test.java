package gitplex.product;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Value;

public class Test {

	@org.junit.Test
	public void test() throws IOException, NoHeadException, GitAPIException {
		Repository repo = new FileRepository(new File("w:\\linux\\.git"));
		String dir = "test";
		String rev = "master";
		for (Map.Entry<String, Value> entry: new LastCommitsOfChildren(repo, repo.resolve(rev), dir).entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue().getId().name());
		}
		repo.close();
	}
	
}