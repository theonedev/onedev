package com.gitplex.server.product;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.FS;

public class Test {

	@org.junit.Test
	public void test() {
		Repository repo;
		try {
			repo = RepositoryCache.open(FileKey.exact(new File("w:\\chromium\\.git"), FS.DETECTED));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try (RevWalk revWalk = new RevWalk(repo)) {
			revWalk.markStart(revWalk.parseCommit(repo.resolve("trunk")));
			RevCommit commit = revWalk.next();
			while (commit != null) {
				commit = revWalk.next();
			}
		} catch (RevisionSyntaxException | IOException e) {
			throw new RuntimeException(e);
		}

		long time = System.currentTimeMillis();
		try (RevWalk revWalk = new RevWalk(repo)) {
			revWalk.markStart(revWalk.parseCommit(repo.resolve("trunk")));
			RevCommit commit = revWalk.next();
			while (commit != null) {
				commit = revWalk.next();
			}
		} catch (RevisionSyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println(System.currentTimeMillis()-time);
	}

}