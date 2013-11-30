package com.pmease.gitop.core.hookcallback;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;

@SuppressWarnings("serial")
@Singleton
public class PostReceiveServlet extends CallbackServlet {

    public static final String PATH = "/git-post-receive";
    
    private static final Logger logger = LoggerFactory.getLogger(PostReceiveServlet.class);

    private final BranchManager branchManager;
    
    private final PullRequestManager pullRequestManager;
    
    @Inject
    public PostReceiveServlet(ProjectManager projectManager, BranchManager branchManager, 
    		PullRequestManager pullRequestManager) {
        super(projectManager);
        
        this.branchManager = branchManager;
        this.pullRequestManager = pullRequestManager;
    }

    @Override
    protected void callback(Project project, String callbackData, Output output) {
		List<String> splitted = StringUtils.splitAndTrim(callbackData, " ");

		String oldCommitHash = splitted.get(0);
		String branchName = splitted.get(2);

		// User with write permission can create new branch
		if (oldCommitHash.equals(Commit.ZERO_HASH)) {
			Branch branch = new Branch();
			branch.setProject(project);
			branch.setName(branchName);
			branchManager.save(branch);
			return;
		}
		
		if (branchName.startsWith("refs/heads/"))
			branchName = branchName.substring("refs/heads/".length());

		logger.info("Executing post-receive hook against branch {}...", branchName);

		Branch branch = branchManager.find(project, branchName, true);

		User user = Gitop.getInstance(UserManager.class).getCurrent();
		Preconditions.checkNotNull(user, "User pushing commits is unknown.");

		PullRequest request = pullRequestManager.findOpen(branch, null, user);
		if (request != null) {
			boolean voted = false;
			for (PullRequestUpdate update: request.getUpdates()) {
				if (!update.getVotes().isEmpty())
					voted = true;
			}
			if (voted) {
//				request.setStatus(PullRequest.Status.MERGED);
				pullRequestManager.save(request);
			} else {
				// Delete merged request without votes to reduce number of merge requests
				// in system
				pullRequestManager.delete(request);
			}
		}
    	
    }
    
}
