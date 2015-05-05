package gitplex.product;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.SymbolicRef;

public class Test {

	@org.junit.Test
	public void test() throws IOException, NoHeadException, GitAPIException {
		Repository repo = new FileRepository(new File("W:\\temp\\test\\.git"));
		SymbolicRef ref = (SymbolicRef) repo.getRef("HEAD");
		System.out.println(ref.getTarget().getName());
		repo.close();
	}
	
}