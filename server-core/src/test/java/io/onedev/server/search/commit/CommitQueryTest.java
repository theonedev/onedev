package io.onedev.server.search.commit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

import io.onedev.server.OneDev;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.AbstractGitTest;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public class CommitQueryTest extends AbstractGitTest {

	@Test
	public void testMatches() throws Exception {
		addFileAndCommit("initial", "", "initial");
		git.checkout().setCreateBranch(true).setName("dev").call();
		addFileAndCommit("dev/file1", "", "dev1");
		addFileAndCommit("dev/file2", "", "dev2");
		git.checkout().setName("main").call();
		
		user = new PersonIdent("bar", "bar@example.com");
		
		addFileAndCommit("main/file1", "", "main1");
		addFileAndCommit("main/file2", "", "main2");
		
		GitService gitService = mock(GitService.class);
		when(OneDev.getInstance(GitService.class)).thenReturn(gitService);
		when(gitService.getChangedFiles(any(), any(), any(), any())).thenAnswer(new Answer<Collection<String>>() {

			@Override
			public Collection<String> answer(InvocationOnMock invocation) throws Throwable {
				return GitUtils.getChangedFiles(git.getRepository(), invocation.getArgument(1), invocation.getArgument(2));
			}
			
		});
		
		Project project = new Project() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getDefaultBranch() {
				return "main";
			}

			@Override
			public RefFacade getRef(String revision) {
				try (RevWalk revWalk = new RevWalk(git.getRepository())) {
					Ref ref = git.getRepository().getRefDatabase().findRef(revision);
					return new RefFacade(revWalk, ref);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public RevCommit getRevCommit(ObjectId revId, boolean mustExist) {
				try (RevWalk revWalk = new RevWalk(git.getRepository())) {
					return GitUtils.parseCommit(revWalk, revId);
				}
			}
			
		};
		ObjectId newCommitId = git.getRepository().resolve("main");
		ObjectId oldCommitId = git.getRepository().resolve("main~1");
		RefUpdated event = new RefUpdated(project, "refs/heads/main", oldCommitId, newCommitId) {

			private static final long serialVersionUID = 1L;

			@Override
			public Project getProject() {
				return project;
			}
			
		};
		
		User user = new User();
		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setGit(true);
		emailAddress.setPrimary(true);
		emailAddress.setOwner(user);
		emailAddress.setVerificationCode(null);
		emailAddress.setValue(CommitQueryTest.this.user.getEmailAddress());
		user.getEmailAddresses().add(emailAddress);
		
		User.push(user);
		try {
			assertTrue(CommitQuery.parse(project, null, true).matches(event));
			assertFalse(CommitQuery.parse(project, "branch(dev)", true).matches(event));
			assertTrue(CommitQuery.parse(project, "default-branch", true).matches(event));
			assertTrue(CommitQuery.parse(project, "branch(main) path(main/*)", true).matches(event));
			assertFalse(CommitQuery.parse(project, "path(main/file)", true).matches(event));
			assertTrue(CommitQuery.parse(project, "path(main/)", true).matches(event));
			assertTrue(CommitQuery.parse(project, "path(main/file) path(main/file*)", true).matches(event));
			assertTrue(CommitQuery.parse(project, "message(dev) message(main)", true).matches(event));
			assertTrue(CommitQuery.parse(project, "message(dev) message(m*n)", true).matches(event));
			assertTrue(CommitQuery.parse(project, "authored-by-me author(foo@example.com)", true).matches(event));
			assertFalse(CommitQuery.parse(project, "authored-by-me committer(foo@example.com)", true).matches(event));
			assertTrue(CommitQuery.parse(project, "authored-by-me committer(bar@example.com)", true).matches(event));
			assertTrue(CommitQuery.parse(project, "after(2 days ago) after(1 hour ago)", true).matches(event));
			assertFalse(CommitQuery.parse(project, "before(1 hour ago) after(1 day ago)", true).matches(event));
		} finally {
			User.pop();
		}
	}

}
