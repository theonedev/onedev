package gitplex.product;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.FS;

public class Test {

	private Repository open() {
		try {
			return RepositoryCache.open(FileKey.exact(new File("W:\\temp\\gitplex_storage\\repositories\\1"), FS.DETECTED), true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Nullable
	public RevObject getRevObject(AnyObjectId revId) {
		try (	Repository repository = open();
				RevWalk revWalk = new RevWalk(repository);) {
			return revWalk.parseAny(revId);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Nullable
	public RevCommit getRevCommit(RevObject revObject) {
		try (	Repository repository = open();
				RevWalk revWalk = new RevWalk(repository);) {
			RevObject peeled = revWalk.peel(revObject);
			if (peeled instanceof RevCommit)
				return (RevCommit) peeled;
			else
				return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@org.junit.Test
	public void test() {
		Map<String, Ref> refs;
		try (Repository repo = open()) {
			RepositoryCache.register(repo);
			refs = repo.getAllRefs();
		}
		for (Ref ref: refs.values()) {
			getRevCommit(getRevObject(ref.getObjectId()));
		}
	}
}