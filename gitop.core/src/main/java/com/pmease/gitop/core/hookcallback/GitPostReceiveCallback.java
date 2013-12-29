package com.pmease.gitop.core.hookcallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

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
import com.google.common.eventbus.EventBus;
import com.pmease.commons.git.Commit;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.event.BranchRefUpdateEvent;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.User;

@SuppressWarnings("serial")
@Singleton
public class GitPostReceiveCallback extends HttpServlet {

    public static final String PATH = "/git-postreceive-callback";
    
    private static final Logger logger = LoggerFactory.getLogger(GitPostReceiveCallback.class);

    private final ProjectManager projectManager;
    
    private final BranchManager branchManager;
    
    private final EventBus eventBus;
    
    @Inject
    public GitPostReceiveCallback(ProjectManager projectManager, BranchManager branchManager, EventBus eventBus) {
    	this.projectManager = projectManager;
        this.branchManager = branchManager;
        this.eventBus = eventBus;
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
        
        Project project = projectManager.load(Long.valueOf(fields.get(0)));
        
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

    private void onRefUpdated(Project project, String refName, String oldCommitHash, String newCommitHash) {
		String branchName = Branch.getName(refName);
		if (branchName != null) {
			if (oldCommitHash.equals(Commit.ZERO_HASH)) {
				Branch branch = new Branch();
				branch.setProject(project);
				branch.setName(branchName);
				branchManager.save(branch);
			} else if (newCommitHash.equals(Commit.ZERO_HASH)) {
				Branch branch = branchManager.findBy(project, branchName);
				Preconditions.checkNotNull(branch);
				branchManager.delete(branch);
			} else {
				logger.info("Executing post-receive hook against branch {}...", branchName);
				
				Branch branch = branchManager.findBy(project, branchName);
				Preconditions.checkNotNull(branch);
				eventBus.post(new BranchRefUpdateEvent(branch));
			}
		}
    }
}
