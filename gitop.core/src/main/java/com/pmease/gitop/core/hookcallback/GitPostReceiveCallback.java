package com.pmease.gitop.core.hookcallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;

@SuppressWarnings("serial")
@Singleton
public class GitPostReceiveCallback extends HttpServlet {

    public static final String PATH = "/git-postreceive-callback";
    
    private static final Logger logger = LoggerFactory.getLogger(GitPostReceiveCallback.class);

    private final RepositoryManager projectManager;
    
    private final BranchManager branchManager;
    
    private final UserManager userManager;
    
    private final UnitOfWork unitOfWork;
    
    private final Executor executor;
    
    @Inject
    public GitPostReceiveCallback(RepositoryManager projectManager, 
    		BranchManager branchManager, UserManager userManager,
    		UnitOfWork unitOfWork, Executor executor) {
    	this.projectManager = projectManager;
        this.branchManager = branchManager;
        this.userManager = userManager;
        this.unitOfWork = unitOfWork;
        this.executor = executor;
    }

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();

        if (!InetAddress.getByName(clientIp).isLoopbackAddress()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Git hook callbacks can only be accessed from localhost.");
            return;
        }

        List<String> fields = StringUtils.splitAndTrim(request.getPathInfo(), "/");
        Preconditions.checkState(fields.size() == 2);
        
        Repository project = projectManager.load(Long.valueOf(fields.get(0)));
        
        SecurityUtils.getSubject().runAs(User.asPrincipal(Long.valueOf(fields.get(1))));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(request.getInputStream(), baos);
        
        /*
         * If multiple refs are updated, the hook stdin will put each ref update info into
         * a separate line, however the line breaks is omitted when forward the hook stdin
         * to curl via "@-", below logic is used to parse these info correctly even 
         * without line breaks.  
         */
        String callbackData = new String(baos.toByteArray());
        callbackData = StringUtils.reverse(StringUtils.remove(callbackData, '\n'));
        fields = StringUtils.splitAndTrim(callbackData, " ");
        
        int pos = 0;
        while (true) {
        	String refName = StringUtils.reverse(fields.get(pos));
        	pos++;
        	String newCommitHash = StringUtils.reverse(fields.get(pos));
        	pos++;
        	String field = fields.get(pos);
        	String oldCommitHash = StringUtils.reverse(field.substring(0, 40));
        	onRefUpdated(project, refName, oldCommitHash, newCommitHash);
        	
        	field = field.substring(40);
        	if (field.length() == 0)
        		break;
        	else
        		fields.set(pos, field);
        }
    	
	}

    private void onRefUpdated(Repository project, String refName, String oldCommitHash, String newCommitHash) {
		String branchName = Branch.getName(refName);
		if (branchName != null) {
			if (oldCommitHash.equals(Commit.ZERO_HASH)) {
				Branch branch = new Branch();
				branch.setProject(project);
				branch.setName(branchName);
				branch.setCreator(userManager.getCurrent());
				project.getBranches().add(branch);
				branchManager.save(branch);
				if (project.getBranches().size() == 1) 
					project.git().updateDefaultBranch(branchName);
			} else if (newCommitHash.equals(Commit.ZERO_HASH)) {
				Branch branch = branchManager.findBy(project, branchName);
				Preconditions.checkNotNull(branch);
				project.getBranches().remove(branch);
				branchManager.delete(branch);
				if (project.git().resolveDefaultBranch().equals(branchName) && !project.getBranches().isEmpty()) 
						project.git().updateDefaultBranch(project.getBranches().iterator().next().getName());
			} else {
				logger.debug("Executing post-receive hook against branch {}...", branchName);
				
				final Branch branch = branchManager.findBy(project, branchName);
				Preconditions.checkNotNull(branch);
				
				final Long branchId = branch.getId();
				final Long userId = User.getCurrentId();
				
				executor.execute(new Runnable() {

					@Override
					public void run() {
						unitOfWork.call(new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								Branch branch = branchManager.load(branchId);
								User user = userManager.load(userId);
								branchManager.onBranchRefUpdate(branch, user);
								return null;
							}
							
						});
					}
					
				});
			}
		}
    }
}
