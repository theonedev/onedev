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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.listeners.RepositoryListener;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Singleton
public class GitPostReceiveCallback extends HttpServlet {

    public static final String PATH = "/git-postreceive-callback";
    
    private static final Logger logger = LoggerFactory.getLogger(GitPostReceiveCallback.class);
    
    private final Dao dao;
    
    private final BranchManager branchManager;
    
    private final UnitOfWork unitOfWork;
    
    private final Provider<Set<RepositoryListener>> listenersProvider;
    
    @Inject
    public GitPostReceiveCallback(Dao dao, BranchManager branchManager, UnitOfWork unitOfWork, 
    		Provider<Set<RepositoryListener>> listenersProvider) {
    	this.dao = dao;
        this.branchManager = branchManager;
        this.unitOfWork = unitOfWork;
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
        
        Repository repository = dao.load(Repository.class, Long.valueOf(fields.get(0)));
        
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
        	onRefUpdated(repository, refName, oldCommitHash, newCommitHash);
        	
        	field = field.substring(40);
        	if (field.length() == 0)
        		break;
        	else
        		fields.set(pos, field);
        }
    	
	}

    private void onRefUpdated(Repository repository, String refName, String oldCommitHash, final String newCommitHash) {
		String branchName = Branch.parseName(refName);
		if (branchName != null) {
			if (oldCommitHash.equals(GitUtils.NULL_SHA1)) {
				Branch branch = new Branch();
				branch.setRepository(repository);
				branch.setName(branchName);
				branch.setHeadCommitHash(newCommitHash);
				repository.getBranches().add(branch);
				if (repository.getBranches().size() == 1) { 
					repository.git().updateDefaultBranch(branchName);
					branch.setDefault(true);
				}
				branchManager.save(branch);
			} else if (newCommitHash.equals(GitUtils.NULL_SHA1)) {
				Branch branch = branchManager.findBy(repository, branchName);
				Preconditions.checkNotNull(branch);

				repository.getBranches().remove(branch);
				branchManager.delete(branch);
				if (repository.git().resolveDefaultBranch().equals(branchName) && !repository.getBranches().isEmpty()) 
						repository.git().updateDefaultBranch(repository.getBranches().iterator().next().getName());
			} else {
				Branch branch = branchManager.findBy(repository, branchName);
				Preconditions.checkNotNull(branch);

				branch.setHeadCommitHash(newCommitHash);
				branchManager.save(branch);
			}
		}
		
		if (!newCommitHash.equals(GitUtils.NULL_SHA1)) {
			final Long repositoryId = repository.getId();
			unitOfWork.asyncCall(new Runnable() {

				@Override
				public void run() {			
					Repository repository = dao.load(Repository.class, repositoryId);
					try {
						for (RepositoryListener listener: listenersProvider.get())
							listener.commitReceived(repository, newCommitHash);
					} catch (Exception e) {
						logger.error("Error notifying commit of repository '" + repository + "'", e);
					}
				}
				
			});
		}
    }
}
