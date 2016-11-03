package com.gitplex.core.hookcallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.gatekeeper.GateKeeper;
import com.gitplex.core.gatekeeper.checkresult.Failed;
import com.gitplex.core.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.core.manager.AccountManager;
import com.gitplex.core.manager.DepotManager;
import com.gitplex.core.security.ObjectPermission;
import com.google.common.base.Preconditions;
import com.gitplex.commons.git.GitUtils;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.util.StringUtils;

@SuppressWarnings("serial")
@Singleton
public class GitPreReceiveCallback extends HttpServlet {

	public static final String PATH = "/git-prereceive-callback";

	private final DepotManager depotManager;
	
	private final AccountManager userManager;
	
	@Inject
	public GitPreReceiveCallback(DepotManager depotManager, AccountManager userManager) {
		this.depotManager = depotManager;
		this.userManager = userManager;
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

        List<String> fields = StringUtils.splitAndTrim(request.getPathInfo(), "/");
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
	        fields = StringUtils.splitAndTrim(callbackData, " ");
	        
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

	    		if (refName.startsWith(Depot.REFS_GITPLEX)) {
	    			if (!user.asSubject().isPermitted(ObjectPermission.ofDepotAdmin(depot))) {
	    				error(output, refName, "Only repository administrators can update gitplex refs.");
	    				break;
	    			}
	    		} else {
	    			GateKeeper gateKeeper = depot.getGateKeeper();
	    			GateCheckResult checkResult = gateKeeper.checkPush(user, depot, refName, oldObjectId, newObjectId);
	    			if (!checkResult.isPassedOrIgnored()) {
	    				List<String> messages = new ArrayList<>();
	    				for (String each: checkResult.getReasons())
	    					messages.add(each);
	    				if (GitUtils.ref2branch(refName) != null 
	    						&& !oldObjectId.equals(ObjectId.zeroId()) 
	    						&& !newObjectId.equals(ObjectId.zeroId()) 
	    						&& !(checkResult instanceof Failed)) {
	    					messages.add("");
	    					messages.add("----------------------------------------------------");
	    					messages.add("You may submit a pull request instead.");
	    				}
	    				error(output, refName, messages.toArray(new String[messages.size()]));
	    				break;
	    			}
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
