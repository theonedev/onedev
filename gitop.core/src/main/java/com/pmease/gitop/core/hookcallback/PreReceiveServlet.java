package com.pmease.gitop.core.hookcallback;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.gatekeeper.checkresult.Blocked;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.gatekeeper.checkresult.Pending;
import com.pmease.gitop.core.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.core.manager.StorageManager;
import com.pmease.gitop.core.model.Branch;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.PullRequest;
import com.pmease.gitop.core.model.PullRequestUpdate;
import com.pmease.gitop.core.model.User;

@SuppressWarnings("serial")
@Singleton
public class PreReceiveServlet extends CallbackServlet {

	private static final Logger logger = LoggerFactory.getLogger(PreReceiveServlet.class);

	public static final String PATH = "/git-pre-receive";

	private final BranchManager branchManager;

	private final StorageManager storageManager;

	private final PullRequestManager pullRequestManager;

	private final PullRequestUpdateManager pullRequestUpdateManager;

	private final Gitop gitop;

	@Inject
	public PreReceiveServlet(ProjectManager projectManager, BranchManager branchManager,
			StorageManager storageManager, PullRequestManager pullRequestManager,
			PullRequestUpdateManager pullRequestUpdateManager, Gitop gitop) {
		super(projectManager);
		this.branchManager = branchManager;
		this.storageManager = storageManager;
		this.pullRequestManager = pullRequestManager;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.gitop = gitop;
	}

	@Override
	protected void callback(Project project, String callbackData, Output output) {
		List<String> splitted = StringUtils.splitAndTrim(callbackData, " ");

		String oldCommitHash = splitted.get(0);
		
		// User with write permission can create new branch
		if (oldCommitHash.equals(Commit.ZERO_HASH))
			return;
		
		String newCommitHash = splitted.get(1);
		String branchName = splitted.get(2);
		
		if (branchName.startsWith("refs/heads/"))
			branchName = branchName.substring("refs/heads/".length());

		logger.info("Executing pre-receive hook against branch {}...", branchName);

		Branch branch = branchManager.find(project, branchName, true);

		User user = User.getCurrent();
		Preconditions.checkNotNull(user, "User pushing commits is unknown.");

		PullRequest request = pullRequestManager.findOpen(branch, null, user);
		if (request == null) {
			request = new PullRequest();
			request.setAutoCreated(true);
			request.setAutoMerge(true);
			request.setTarget(branch);
			request.setSubmitter(user);

			pullRequestManager.save(request);
		} 

		if (!request.getLatestUpdate().getHeadCommit().equals(newCommitHash)) {
			
			Git git = new Git(storageManager.getStorage(project).ofCode());
	
			PullRequestUpdate update = new PullRequestUpdate();
			update.setRequest(request);
			Commit commit = git.resolveRevision(newCommitHash);
			update.setSubject(commit.getSummary());
			request.getUpdates().add(update);
			
			pullRequestUpdateManager.save(update);
		}
		
		request.refresh();
		CheckResult checkResult = request.getCheckResult(); 

		if (checkResult instanceof Rejected) {
			output.markError();
			for (String each : checkResult.getReasons()) {
				output.writeLine(each);
			}
			
			if (request.getUpdates().size() == 1) {
				pullRequestManager.delete(request);
			}
		} else if (checkResult instanceof Pending || checkResult instanceof Blocked) {
			output.markError();
			output.writeLine("!!!! Your pushed commit is subject to review before accepted. "
					+ "For details, please visit: !!!!");
			output.writeLine(gitop.guessServerUrl() + "/mr/" + request.getId());
		}
	}

}
