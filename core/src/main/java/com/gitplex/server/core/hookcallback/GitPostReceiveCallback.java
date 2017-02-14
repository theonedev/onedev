package com.gitplex.server.core.hookcallback;

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
import org.apache.shiro.util.ThreadContext;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.calla.loader.ListenerRegistry;
import com.gitplex.calla.loader.LoaderUtils;
import com.gitplex.commons.git.GitUtils;
import com.gitplex.commons.hibernate.UnitOfWork;
import com.gitplex.commons.util.StringUtils;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.event.RefUpdated;
import com.gitplex.server.core.manager.DepotManager;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
@Singleton
public class GitPostReceiveCallback extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(GitPostReceiveCallback.class);
	
    public static final String PATH = "/git-postreceive-callback";
    
    private final DepotManager depotManager;

    private final ListenerRegistry listenerRegistry;
    
    private final UnitOfWork unitOfWork;
    
    @Inject
    public GitPostReceiveCallback(DepotManager depotManager, UnitOfWork unitOfWork, ListenerRegistry listenerRegistry) {
    	this.depotManager = depotManager;
    	this.unitOfWork = unitOfWork;
        this.listenerRegistry = listenerRegistry;
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

        List<String> fields = LoaderUtils.splitAndTrim(request.getPathInfo(), "/");
        Preconditions.checkState(fields.size() == 2);
        
        Long depotId = Long.valueOf(fields.get(0));
        Long userId = Long.valueOf(fields.get(1));
        
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
        
        fields.clear();
        fields.addAll(LoaderUtils.splitAndTrim(callbackData, " "));
        
        unitOfWork.doAsync(new Runnable() {

			@Override
			public void run() {
				ThreadContext.bind(Account.asSubject(userId));
		        try {
		            Depot depot = depotManager.load(depotId);
		            
			        int pos = 0;
			        while (true) {
			        	String refName = StringUtils.reverse(fields.get(pos));
			        	pos++;
			        	ObjectId newObjectId = ObjectId.fromString(StringUtils.reverse(fields.get(pos)));
			        	pos++;
			        	String field = fields.get(pos);
			        	ObjectId oldObjectId = ObjectId.fromString(StringUtils.reverse(field.substring(0, 40)));
			        	
			        	if (!newObjectId.equals(ObjectId.zeroId())) {
			        		depot.cacheObjectId(refName, newObjectId);
			        	} else {
			        		newObjectId = ObjectId.zeroId();
			        		depot.cacheObjectId(refName, null);
			        	}
			        	
			        	String branch = GitUtils.ref2branch(refName);
			        	if (branch != null && depot.getDefaultBranch() == null) {
			        		RefUpdate refUpdate = depot.updateRef("HEAD");
			        		GitUtils.linkRef(refUpdate, refName);
			        	}
			        	
			        	listenerRegistry.post(new RefUpdated(depot, refName, oldObjectId, newObjectId));
			    		
			        	field = field.substring(40);
			        	if (field.length() == 0)
			        		break;
			        	else
			        		fields.set(pos, field);
			        }
		        } catch (Exception e) {
		        	logger.error("Error executing post-receive callback", e);
				} finally {
		        	ThreadContext.unbindSubject();
		        }
			}
        	
        });
	}

}
