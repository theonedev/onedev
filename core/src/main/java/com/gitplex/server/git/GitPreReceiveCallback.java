package com.gitplex.server.git;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.model.User;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.permission.ProjectPermission;
import com.gitplex.utils.StringUtils;
import com.gitplex.utils.license.LicenseDetail;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
@Singleton
public class GitPreReceiveCallback extends HttpServlet {

	public static final String PATH = "/git-prereceive-callback";

	private final ProjectManager projectManager;
	
	private final UserManager userManager;
	
	private final ConfigManager configManager;
	
	@Inject
	public GitPreReceiveCallback(ProjectManager projectManager, UserManager userManager, ConfigManager configManager) {
		this.projectManager = projectManager;
		this.userManager = userManager;
		this.configManager = configManager;
	}
	
	private void error(Output output, @Nullable String refName, String... messages) {
		output.markError();
		output.writeLine();
		output.writeLine("*******************************************************");
		output.writeLine("*");
		if (refName != null)
			output.writeLine("*  ERROR PUSHING REF: " + refName);
		else
			output.writeLine("*  ERROR PUSHING");
		output.writeLine("-------------------------------------------------------");
		for (String message: messages)
			output.writeLine("*  " + message);
		output.writeLine("*");
		output.writeLine("*******************************************************");
		output.writeLine();
	}
	
	@Sessional
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int userCount = userManager.count(EntityCriteria.of(User.class));
		int licenseLimit = LicenseDetail.FREE_LICENSE_USERS;
		LicenseDetail license = configManager.getLicense();
		if (license != null && license.getRemainingDays() >= 0) {
			licenseLimit += license.getLicensedUsers();
		} 
		if (userCount > licenseLimit) {
			String message = String.format("Push is disabled as number of users in system exceeds license limit");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, message);
			return;
		}
		
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();

        if (!InetAddress.getByName(clientIp).isLoopbackAddress()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Git hook callbacks can only be accessed from localhost.");
            return;
        }
        
        List<String> fields = StringUtils.splitAndTrim(request.getPathInfo(), "/");
        Preconditions.checkState(fields.size() == 2);
        
        SecurityUtils.getSubject().runAs(User.asPrincipal(Long.valueOf(fields.get(1))));
        try {
            Project project = projectManager.load(Long.valueOf(fields.get(0)));
            
            String refUpdateInfo = null;
            
            /*
             * Since git 2.11, pushed commits will be placed in to a QUARANTINE directory when pre-receive hook 
             * is fired. Current version of jgit does not pick up objects in this directory so we should call 
             * native git instead with various environments passed from pre-receive hook   
             */
            Map<String, String> gitEnvs = new HashMap<>();
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
            	String paramName = paramNames.nextElement();
            	if (paramName.contains(" ")) {
            		refUpdateInfo = paramName;
            	} else if (paramName.startsWith("ENV_")) {
            		gitEnvs.put(paramName.substring("ENV_".length()), request.getParameter(paramName));
            	}
            }
            
            Preconditions.checkState(refUpdateInfo != null, "Git ref update information is not available");
            
	        Output output = new Output(response.getOutputStream());
	        
	        /*
	         * If multiple refs are updated, the hook stdin will put each ref update info into
	         * a separate line, however the line breaks is omitted when forward the hook stdin
	         * to curl via "@-", below logic is used to parse these info correctly even 
	         * without line breaks.  
	         */
	        refUpdateInfo = StringUtils.reverse(StringUtils.remove(refUpdateInfo, '\n'));
	        fields = StringUtils.splitAndTrim(refUpdateInfo, " ");
	        
	        int pos = 0;
	        while (true) {
	        	String refName = StringUtils.reverse(fields.get(pos));
	        	pos++;
	        	ObjectId newObjectId = ObjectId.fromString(StringUtils.reverse(fields.get(pos)));
	        	pos++;
	        	String field = fields.get(pos);
	        	ObjectId oldObjectId = ObjectId.fromString(StringUtils.reverse(field.substring(0, 40)));
	        	
	    		User user = userManager.getCurrent();
	    		Preconditions.checkNotNull(user);

	    		if (refName.startsWith(PullRequest.REFS_PREFIX) || refName.startsWith(PullRequestUpdate.REFS_PREFIX)) {
	    			if (!user.asSubject().isPermitted(
	    					new ProjectPermission(project.getFacade(), ProjectPrivilege.ADMIN))) {
	    				error(output, refName, "Only project administrators can update gitplex refs.");
	    				break;
	    			}
	    		} else if (refName.startsWith(Constants.R_HEADS)) {
	    			String branchName = Preconditions.checkNotNull(GitUtils.ref2branch(refName));

	    			if (!oldObjectId.equals(ObjectId.zeroId())) {
		    			if (newObjectId.equals(ObjectId.zeroId())) {
		    				BranchProtection protection = project.getBranchProtection(branchName);
		    				if (protection != null && protection.isNoDeletion())
		    					error(output, refName, "Can not delete this branch according to branch protection setting");
		    			} else if (!GitUtils.isMergedInto(project.getGitDir(), gitEnvs, oldObjectId.name(), newObjectId.name())) {
		    				BranchProtection protection = project.getBranchProtection(branchName);
		    				if (protection != null && protection.isNoForcedPush())
			    				error(output, refName, "Can not force-push to this branch according to branch protection setting");
		    			} else {
		    				if (projectManager.isPushNeedsQualityCheck(user, project, branchName, oldObjectId, newObjectId, gitEnvs)) {
		    					error(output, refName, 
		    							"Your changes need to be reviewed/verified. Please submit pull request instead");
		    				}
		    			}
	    			}
	    		} else if (refName.startsWith(Constants.R_TAGS)) {
	    			String tagName = Preconditions.checkNotNull(GitUtils.ref2tag(refName));
	    			String errorMessage = null;
	    			TagProtection protection = project.getTagProtection(tagName);
	    			if (protection != null) {
    					if (oldObjectId.equals(ObjectId.zeroId())) {
    						errorMessage = protection.getTagCreator().getNotMatchMessage(project, user);
    						if (errorMessage != null)
    							errorMessage = "Unable to create protected tag: " + errorMessage;
    					} else if (newObjectId.equals(ObjectId.zeroId())) {
    						if (protection.isNoDeletion())
    							errorMessage = "Can not delete this tag according to tag protection setting";
    					} else if (protection.isNoUpdate()) {
							errorMessage = "Can not update this tag according to tag protection setting";
    					}
	    			}
					if (errorMessage != null)
						error(output, refName, errorMessage);
	    		}
	    		
	        	field = field.substring(40);
	        	if (field.length() == 0)
	        		break;
	        	else
	        		fields.set(pos, field);
	        }
        } finally {
        	SecurityUtils.getSubject().releaseRunAs();
        }		
	}	
}
