package io.onedev.server.search.commit;

import static org.junit.Assert.*;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.junit.Test;

import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.AbstractGitTest;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public class CommitQueryUtilsTest extends AbstractGitTest {

	@Test
	public void testMatches() throws Exception {
		addFileAndCommit("initial", "", "initial");
		git.checkout().setCreateBranch(true).setName("dev").call();
		addFileAndCommit("dev/file1", "", "dev1");
		addFileAndCommit("dev/file2", "", "dev2");
		git.checkout().setName("master").call();
		
		CommitQueryUtilsTest.this.user = new PersonIdent("bar", "bar@example.com");
		
		addFileAndCommit("master/file1", "", "master1");
		addFileAndCommit("master/file2", "", "master2");
		
		Project project = new Project() {

			private static final long serialVersionUID = 1L;

			@Override
			public Repository getRepository() {
				return git.getRepository();
			}

		};
		ObjectId newCommitId = git.getRepository().resolve("master");
		ObjectId oldCommitId = git.getRepository().resolve("master~1");
		RefUpdated event = new RefUpdated(project, "refs/heads/master", oldCommitId, newCommitId);
		
		User user = new User();
		user.setEmail(CommitQueryUtilsTest.this.user.getEmailAddress());
		assertTrue(CommitQueryUtils.matches(event, user, null));
		assertFalse(CommitQueryUtils.matches(event, user, "branch(dev)"));
		assertTrue(CommitQueryUtils.matches(event, user, "default-branch"));
		assertTrue(CommitQueryUtils.matches(event, user, "branch(master) path(master/*)"));
		assertFalse(CommitQueryUtils.matches(event, user, "path(master/file)"));
		assertTrue(CommitQueryUtils.matches(event, user, "path(master/file) path(master/file*)"));
		assertTrue(CommitQueryUtils.matches(event, user, "message(dev) message(master)"));
		assertTrue(CommitQueryUtils.matches(event, user, "message(dev) message(m*s)"));
		assertTrue(CommitQueryUtils.matches(event, user, "authored-by-me author(foo@example.com)"));
		assertFalse(CommitQueryUtils.matches(event, user, "authored-by-me committer(foo@example.com)"));
		assertTrue(CommitQueryUtils.matches(event, user, "authored-by-me committer(bar@example.com)"));
		assertTrue(CommitQueryUtils.matches(event, user, "after(2 days ago) after(1 hour ago)"));
		assertFalse(CommitQueryUtils.matches(event, user, "before(1 hour ago) after(1 day ago)"));
	}

}
