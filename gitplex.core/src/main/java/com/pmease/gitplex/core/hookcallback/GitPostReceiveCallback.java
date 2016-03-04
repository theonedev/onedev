package com.pmease.gitplex.core.hookcallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.extensionpoint.RefListener;

@SuppressWarnings("serial")
@Singleton
public class GitPostReceiveCallback extends HttpServlet {

    public static final String PATH = "/git-postreceive-callback";
    
    private final Dao dao;
    
    private final Provider<Set<RefListener>> listenersProvider;
    
    @Inject
    public GitPostReceiveCallback(Dao dao, Provider<Set<RefListener>> listenersProvider) {
    	this.dao = dao;
        this.listenersProvider = listenersProvider;
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
        
        Depot depot = dao.load(Depot.class, Long.valueOf(fields.get(0)));
        
        SecurityUtils.getSubject().runAs(Account.asPrincipal(Long.valueOf(fields.get(1))));
        
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
        	ObjectId newCommit = ObjectId.fromString(StringUtils.reverse(fields.get(pos)));
        	pos++;
        	String field = fields.get(pos);
        	ObjectId oldCommit = ObjectId.fromString(StringUtils.reverse(field.substring(0, 40)));
        	
        	if (!newCommit.equals(ObjectId.zeroId())) {
        		depot.cacheObjectId(refName, newCommit);
        	} else {
        		newCommit = ObjectId.zeroId();
        		depot.cacheObjectId(refName, null);
        	}
        	
        	String branch = GitUtils.ref2branch(refName);
        	if (branch != null && depot.getObjectId(depot.getDefaultBranch(), false) == null) {
        		depot.git().updateDefaultBranch(branch);
        	}
    		for (RefListener listener: listenersProvider.get())
    			listener.onRefUpdate(depot, refName, oldCommit, newCommit);
    		
        	field = field.substring(40);
        	if (field.length() == 0)
        		break;
        	else
        		fields.set(pos, field);
        }
    	
	}

}
