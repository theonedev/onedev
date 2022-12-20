package io.onedev.server.git.hook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.SecurityUtils;

@SuppressWarnings("serial")
@Singleton
public class GitPostReceiveCallback extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(GitPostReceiveCallback.class);
	
    public static final String PATH = "/git-postreceive-callback";
    
    private final ProjectManager projectManager;
    
    private final UrlManager urlManager;

    private final SessionManager sessionManager;
    
    private final ListenerRegistry listenerRegistry;
    
    @Inject
    public GitPostReceiveCallback(ProjectManager projectManager, UrlManager urlManager, 
    		SessionManager sessionManager, ListenerRegistry listenerRegistry) {
    	this.projectManager = projectManager;
    	this.urlManager = urlManager;
    	this.sessionManager = sessionManager;
        this.listenerRegistry = listenerRegistry;
    }

    @Sessional
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<String> fields = StringUtils.splitAndTrim(request.getPathInfo(), "/");
        Preconditions.checkState(fields.size() == 3);
        
        if (!fields.get(2).equals(HookUtils.HOOK_TOKEN)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Git hook callbacks can only be accessed by OneDev itself");
            return;
        }
        Long userId = Long.valueOf(fields.get(1));
        Long projectId = Long.valueOf(fields.get(0));
        
        ThreadContext.bind(SecurityUtils.asSubject(userId));

        String refUpdateInfo = null;
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
        	String paramName = paramNames.nextElement();
        	if (paramName.contains(" ")) {
        		refUpdateInfo = paramName;
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
        
        fields.clear();
        fields.addAll(StringUtils.splitAndTrim(refUpdateInfo, " "));

        List<Triple<String, ObjectId, ObjectId>> updateInfos = new ArrayList<>();
        
        int pos = 0;
        while (true) {
        	String refName = StringUtils.reverse(fields.get(pos));
        	pos++;
        	ObjectId newObjectId = ObjectId.fromString(StringUtils.reverse(fields.get(pos)));
        	pos++;
        	String field = fields.get(pos);
        	ObjectId oldObjectId = ObjectId.fromString(StringUtils.reverse(field.substring(0, 40)));
        	
        	Repository repository = projectManager.getRepository(projectId);
        	String branch = GitUtils.ref2branch(refName);
        	String defaultBranch = GitUtils.getDefaultBranch(repository);
        	if (branch != null && defaultBranch == null) {
        		RefUpdate refUpdate = GitUtils.getRefUpdate(repository, "HEAD");
        		GitUtils.linkRef(refUpdate, refName);
        	}

        	if (branch != null && defaultBranch != null && !branch.equals(defaultBranch) 
        			&& !userId.equals(User.SYSTEM_ID)) {
        		showPullRequestLink(output, projectId, branch, defaultBranch);
        	}
        	
        	try (RevWalk revWalk = new RevWalk(repository)) {
            	if (!oldObjectId.equals(ObjectId.zeroId())) 
            		oldObjectId = revWalk.parseCommit(oldObjectId).copy();
            	if (!newObjectId.equals(ObjectId.zeroId())) 
            		newObjectId = revWalk.parseCommit(newObjectId).copy();
        	}
        	
        	updateInfos.add(new ImmutableTriple<>(refName, oldObjectId, newObjectId));
    		
        	field = field.substring(40);
        	if (field.length() == 0)
        		break;
        	else
        		fields.set(pos, field);
        }
        
        sessionManager.runAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				Project project = projectManager.load(projectId);
		        try {
		            for (var updateInfo: updateInfos) {
		            	RefUpdated event = new RefUpdated(project, updateInfo.getLeft(), 
		            			updateInfo.getMiddle(), updateInfo.getRight());
			        	listenerRegistry.invokeListeners(event);
		            }
		        } catch (Exception e) {
		        	logger.error("Error posting ref updated event", e);
				}
			}
        	
        });
	}

	private void showPullRequestLink(Output output, Long projectId, String branch, String defaultBranch) {
    	output.writeLine();
    	output.writeLine("Create a pull request for '"+ branch +"' by visiting:");
		output.writeLine("    " + urlManager.urlForProject(projectId) 
				+"/~pulls/new?target=" 
				+ projectId 
				+ ":" 
				+ defaultBranch 
				+ "&source=" 
				+ projectId
				+ ":"
				+ branch);
		output.writeLine();
	}
	
}
