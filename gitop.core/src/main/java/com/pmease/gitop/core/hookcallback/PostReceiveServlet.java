package com.pmease.gitop.core.hookcallback;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.MergeRequestManager;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Branch;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;

@SuppressWarnings("serial")
@Singleton
public class PostReceiveServlet extends CallbackServlet {

    public static final String PATH = "/git-post-receive";
    
    private static final Logger logger = LoggerFactory.getLogger(PostReceiveServlet.class);

    private final BranchManager branchManager;
    
    private final MergeRequestManager mergeRequestManager;
    
    @Inject
    public PostReceiveServlet(ProjectManager projectManager, BranchManager branchManager, 
    		MergeRequestManager mergeRequestManager) {
        super(projectManager);
        
        this.branchManager = branchManager;
        this.mergeRequestManager = mergeRequestManager;
    }

    @Override
    protected void callback(Project project, String callbackData, Output output) {
		List<String> splitted = StringUtils.splitAndTrim(callbackData);

		// String oldCommitHash = splitted.get(0);
		String branchName = splitted.get(2);

		logger.info("Executing post-receive hook against branch {}...", branchName);

		Branch branch = branchManager.find(project, branchName, true);

		User user = User.getCurrent();
		Preconditions.checkNotNull(user, "User pushing commits is unknown.");

		MergeRequest request = mergeRequestManager.findOpened(branch, null, user);
		if (request != null) {
			request.setStatus(MergeRequest.Status.MERGED);
			mergeRequestManager.save(request);
		}
    	
    }
    
}
