package io.onedev.server.search.commit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.junit.Test;

import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.AbstractGitTest;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;


public class CommitQueryTest extends AbstractGitTest {

	@Test
	public void testMatches() throws Exception {
		addFileAndCommit("initial", "", "initial");
		git.checkout().setCreateBranch(true).setName("dev").call();
		addFileAndCommit("dev/file1", "", "dev1");
		addFileAndCommit("dev/file2", "", "dev2");
		git.checkout().setName("master").call();
		
		CommitQueryTest.this.user = new PersonIdent("bar", "bar@example.com");
		
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
		user.setEmail(CommitQueryTest.this.user.getEmailAddress());
		User.push(user);
		try {
			assertTrue(CommitQuery.parse(project, null).matches(event));
			assertFalse(CommitQuery.parse(project, "branch(dev)").matches(event));
			assertTrue(CommitQuery.parse(project, "default-branch").matches(event));
			assertTrue(CommitQuery.parse(project, "branch(master) path(master/*)").matches(event));
			assertFalse(CommitQuery.parse(project, "path(master/file)").matches(event));
			assertTrue(CommitQuery.parse(project, "path(master/)").matches(event));
			assertTrue(CommitQuery.parse(project, "path(master/file) path(master/file*)").matches(event));
			assertTrue(CommitQuery.parse(project, "message(dev) message(master)").matches(event));
			assertTrue(CommitQuery.parse(project, "message(dev) message(m*s)").matches(event));
			assertTrue(CommitQuery.parse(project, "authored-by-me author(foo@example.com)").matches(event));
			assertFalse(CommitQuery.parse(project, "authored-by-me committer(foo@example.com)").matches(event));
			assertTrue(CommitQuery.parse(project, "authored-by-me committer(bar@example.com)").matches(event));
			assertTrue(CommitQuery.parse(project, "after(2 days ago) after(1 hour ago)").matches(event));
			assertFalse(CommitQuery.parse(project, "before(1 hour ago) after(1 day ago)").matches(event));
		} finally {
			User.pop();
		}
	}

}
