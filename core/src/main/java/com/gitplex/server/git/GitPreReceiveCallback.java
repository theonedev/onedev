package com.gitplex.server.git;

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
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.launcher.loader.LoaderUtils;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.manager.ReviewManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.security.ObjectPermission;
import com.gitplex.server.util.StringUtils;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
@Singleton
public class GitPreReceiveCallback extends HttpServlet {

	public static final String PATH = "/git-prereceive-callback";

	private final DepotManager depotManager;
	
	private final AccountManager userManager;
	
	private final ReviewManager reviewManager;
	
	@Inject
	public GitPreReceiveCallback(DepotManager depotManager, AccountManager userManager, ReviewManager reviewManager) {
		this.depotManager = depotManager;
		this.userManager = userManager;
		this.reviewManager = reviewManager;
	}
	
	private void error(Output output, String refName, String... messages) {
		output.markError();
		output.writeLine();
		output.writeLine("*******************************************************");
		output.writeLine("*");
		output.writeLine("*  ERROR PUSHING REF: " + refName);
		output.writeLine("-------------------------------------------------------");
		for (String message: messages)
			output.writeLine("*  " + message);
		output.writeLine("*");
		output.writeLine("*******************************************************");
		output.writeLine();
	}
	
	@Transactional
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();

        if (!InetAddress.getByName(clientIp).isLoopbackAddress()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Git hook callbacks can only be accessed from localhost.");
            return;
        }

        List<String> fields = LoaderUtils.splitAndTrim(request.getPathInfo(), "/");
        Preconditions.checkState(fields.size() == 2);
        
        SecurityUtils.getSubject().runAs(Account.asPrincipal(Long.valueOf(fields.get(1))));
        try {
            Depot depot = depotManager.load(Long.valueOf(fields.get(0)));
            
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        IOUtils.copy(request.getInputStream(), baos);
	        
	        Output output = new Output(response.getOutputStream());
	        
	        /*
	         * If multiple refs are updated, the hook stdin will put each ref update info into
	         * a separate line, however the line breaks is omitted when forward the hook stdin
	         * to curl via "@-", below logic is used to parse these info correctly even 
	         * without line breaks.  
	         */
	        String callbackData = new String(baos.toByteArray());
	        callbackData = StringUtils.reverse(StringUtils.remove(callbackData, '\n'));
	        fields = LoaderUtils.splitAndTrim(callbackData, " ");
	        
	        int pos = 0;
	        while (true) {
	        	String refName = StringUtils.reverse(fields.get(pos));
	        	pos++;
	        	ObjectId newObjectId = ObjectId.fromString(StringUtils.reverse(fields.get(pos)));
	        	pos++;
	        	String field = fields.get(pos);
	        	ObjectId oldObjectId = ObjectId.fromString(StringUtils.reverse(field.substring(0, 40)));
	        	
	    		Account user = userManager.getCurrent();
	    		Preconditions.checkNotNull(user);

	    		if (refName.startsWith(PullRequest.REFS_PREFIX) || refName.startsWith(PullRequestUpdate.REFS_PREFIX)) {
	    			if (!user.asSubject().isPermitted(ObjectPermission.ofDepotAdmin(depot))) {
	    				error(output, refName, "Only repository administrators can update gitplex refs.");
	    				break;
	    			}
	    		} else if (refName.startsWith(Constants.R_HEADS)) {
	    			String branchName = Preconditions.checkNotNull(GitUtils.branch2ref(refName));

	    			if (!oldObjectId.equals(ObjectId.zeroId())) {
		    			if (newObjectId.equals(ObjectId.zeroId())) {
		    				BranchProtection protection = depot.getBranchProtection(branchName);
		    				if (protection != null && protection.isNoDeletion())
		    					error(output, refName, "Can not delete this branch according to branch protection setting");
		    			} else if (!GitUtils.isMergedInto(depot.getRepository(), oldObjectId, newObjectId)) {
		    				BranchProtection protection = depot.getBranchProtection(branchName);
		    				if (protection != null && protection.isNoForcedPush())
			    				error(output, refName, "Can not force-push to this branch according to branch protection setting");
		    			} else {
		    				if (!reviewManager.canPush(user, depot, refName, oldObjectId, newObjectId)) {
		    					error(output, refName, 
		    							"Your changes need to be reviewed. Please submit pull request instead");
		    				}
		    			}
	    			}
	    		} else if (refName.startsWith(Constants.R_TAGS)) {
	    			String tagName = Preconditions.checkNotNull(GitUtils.ref2tag(refName));
	    			String errorMessage = null;
	    			TagProtection protection = depot.getTagProtection(tagName);
	    			if (protection != null) {
    					if (oldObjectId.equals(ObjectId.zeroId())) {
    						errorMessage = protection.getTagCreator().getNotMatchMessage(depot, user);
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
