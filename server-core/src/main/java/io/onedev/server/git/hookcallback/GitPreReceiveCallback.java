package io.onedev.server.git.hookcallback;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
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

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.security.permission.ProjectPermission;

@SuppressWarnings("serial")
@Singleton
public class GitPreReceiveCallback extends HttpServlet {

	public static final String PATH = "/git-prereceive-callback";

	private final ProjectManager projectManager;
	
	@Inject
	public GitPreReceiveCallback(ProjectManager projectManager) {
		this.projectManager = projectManager;
	}
	
	private void error(Output output, @Nullable String refName, List<String> messages) {
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
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();

        if (!InetAddress.getByName(clientIp).isLoopbackAddress()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Git hook callbacks can only be accessed from localhost.");
            return;
        }
        
        List<String> fields = StringUtils.splitAndTrim(request.getPathInfo(), "/");
        Preconditions.checkState(fields.size() == 2);
        
        SecurityUtils.getSubject().runAs(SecurityUtils.asPrincipal(Long.valueOf(fields.get(1))));
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
            		String paramValue = request.getParameter(paramName);
            		if (StringUtils.isNotBlank(paramValue))
            			gitEnvs.put(paramName.substring("ENV_".length()), paramValue);
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
	        	
	    		User user = Preconditions.checkNotNull(SecurityUtils.getUser());

	    		if (refName.startsWith(PullRequest.REFS_PREFIX) || refName.startsWith(PullRequestUpdate.REFS_PREFIX)) {
	    			if (!user.asSubject().isPermitted(new ProjectPermission(project, new ManageProject()))) {
	    				error(output, refName, Lists.newArrayList("Only project administrators can update onedev refs."));
	    				break;
	    			}
	    		} else if (refName.startsWith(Constants.R_HEADS)) {
	    			String branchName = Preconditions.checkNotNull(GitUtils.ref2branch(refName));
	    			List<String> errorMessages = new ArrayList<>();
	    			BranchProtection protection = project.getBranchProtection(branchName, user);
					if (oldObjectId.equals(ObjectId.zeroId())) {
						if (protection.isPreventCreation())
							errorMessages.add("Can not create this branch according to branch protection setting");
					} else if (newObjectId.equals(ObjectId.zeroId())) {
						if (protection.isPreventDeletion()) 
							errorMessages.add("Can not delete this branch according to branch protection setting");
					} else if (protection.isPreventForcedPush() 
							&& !GitUtils.isMergedInto(project.getRepository(), gitEnvs, oldObjectId, newObjectId)) {
						errorMessages.add("Can not force-push to this branch according to branch protection setting");
					} else if (protection.isReviewRequiredForPush(user, project, branchName, oldObjectId, newObjectId, gitEnvs)) {
    					errorMessages.add("Review required for your change. Please submit pull request instead");
					}
	    			if (errorMessages.isEmpty() 
	    					&& !oldObjectId.equals(ObjectId.zeroId()) 
	    					&& !newObjectId.equals(ObjectId.zeroId()) 
	    					&& project.isBuildRequiredForPush(user, branchName, oldObjectId, newObjectId, gitEnvs)) {
	    				errorMessages.add("Build required for your change. Please submit pull request instead");
	    			}
	    			if (errorMessages.isEmpty() && newObjectId.equals(ObjectId.zeroId())) {
	    				try {
	    					projectManager.onDeleteBranch(project, branchName);
	    				} catch (ExplicitException e) {
	    					errorMessages.addAll(Splitter.on("\n").splitToList(e.getMessage()));
	    				}
	    			}
					if (!errorMessages.isEmpty())
						error(output, refName, errorMessages);
	    		} else if (refName.startsWith(Constants.R_TAGS)) {
	    			String tagName = Preconditions.checkNotNull(GitUtils.ref2tag(refName));
	    			List<String> errorMessages = new ArrayList<>();
	    			TagProtection protection = project.getTagProtection(tagName, user);
					if (oldObjectId.equals(ObjectId.zeroId())) {
						if (protection.isPreventCreation())
							errorMessages.add("Can not create this tag according to tag protection setting");
					} else if (newObjectId.equals(ObjectId.zeroId())) {
						if (protection.isPreventDeletion())
							errorMessages.add("Can not delete this tag according to tag protection setting");
					} else if (protection.isPreventUpdate()) {
						errorMessages.add("Can not update this tag according to tag protection setting");
					}
	    			if (errorMessages.isEmpty() && newObjectId.equals(ObjectId.zeroId())) {
	    				try {
	    					projectManager.onDeleteTag(project, tagName);
	    				} catch (ExplicitException e) {
	    					errorMessages.addAll(Splitter.on("\n").splitToList(e.getMessage()));
	    				}
	    			}
					if (!errorMessages.isEmpty())
						error(output, refName, errorMessages);
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
