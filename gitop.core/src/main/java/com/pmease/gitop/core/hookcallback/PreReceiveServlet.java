package com.pmease.gitop.core.hookcallback;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.MergePrediction;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;

@SuppressWarnings("serial")
@Singleton
public class PreReceiveServlet extends CallbackServlet {

	private static final Logger logger = LoggerFactory.getLogger(PreReceiveServlet.class);

	public static final String PATH = "/git-pre-receive";

	private final BranchManager branchManager;

	private final UserManager userManager;
	
	@Inject
	public PreReceiveServlet(ProjectManager projectManager, 
			BranchManager branchManager, UserManager userManager) {
		super(projectManager);
		this.branchManager = branchManager;
		this.userManager = userManager;
	}

	@Override
	protected void callback(Project project, String callbackData, Output output) {
		List<String> splitted = StringUtils.splitAndTrim(callbackData, " ");

		String oldCommitHash = splitted.get(0);
		
		// User with write permission can create new branch
		if (oldCommitHash.equals(Commit.ZERO_HASH))
			return;
		
		String newCommitHash = splitted.get(1);
		String branchName = Branch.getName(splitted.get(2));
		
		logger.info("Executing pre-receive hook against branch {}...", branchName);
		
		Branch branch = branchManager.findBy(project, branchName, true);

		User user = userManager.getCurrent();
		Preconditions.checkNotNull(user, "User pushing commits is unknown.");

		PullRequest request = new PullRequest();
		request.setTarget(branch);
		request.setSource(new Branch());
		request.getSource().setProject(new Project());
		request.getSource().getProject().setOwner(user);
		request.setTitle("Faked pull request to check against push gatekeeper");
		request.setMergePrediction(new MergePrediction(oldCommitHash, newCommitHash, newCommitHash));
		
		PullRequestUpdate update = new PullRequestUpdate();
		update.setRequest(request);
		update.setHeadCommit(newCommitHash);
		request.getUpdates().add(update);

		GateKeeper gateKeeper = project.getCompositeGateKeeper();
		CheckResult checkResult = gateKeeper.check(request);

		if (!(checkResult instanceof Accepted)) {
			output.markError();
			output.writeLine();
			output.writeLine("*******************************************************");
			output.writeLine("*");
			for (String each: checkResult.getReasons()) {
				output.writeLine("*  " + each);
			}
			if (!(checkResult instanceof Rejected)) {
				output.writeLine("*");
				output.writeLine("*  ----------------------------------------------------");
				output.writeLine("*  You may submit a pull request instead.");
			}
			output.writeLine("*");
			output.writeLine("*******************************************************");
			output.writeLine();
		}
	}

}
