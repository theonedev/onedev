package io.onedev.server.git.hookcallback;

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
import org.apache.shiro.util.ThreadContext;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.loader.ListenerRegistry;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.SecurityUtils;

@SuppressWarnings("serial")
@Singleton
public class GitPostReceiveCallback extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(GitPostReceiveCallback.class);
	
    public static final String PATH = "/git-postreceive-callback";
    
    private final ProjectManager projectManager;

    private final ListenerRegistry listenerRegistry;
    
    private final SessionManager sessionManager;
    
    @Inject
    public GitPostReceiveCallback(ProjectManager projectManager, SessionManager sessionManager, ListenerRegistry listenerRegistry) {
    	this.projectManager = projectManager;
    	this.sessionManager = sessionManager;
        this.listenerRegistry = listenerRegistry;
    }

    @Sessional
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<String> fields = StringUtils.splitAndTrim(request.getPathInfo(), "/");
        Preconditions.checkState(fields.size() == 3);
        
        if (!fields.get(2).equals(GitUtils.HOOK_TOKEN)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Git hook callbacks can only be accessed by OneDev itself");
            return;
        }
        
        Project project = projectManager.load(Long.valueOf(fields.get(0)));
        Long userId = Long.valueOf(fields.get(1));
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

        List<ImmutableTriple<String, ObjectId, ObjectId>> eventData = new ArrayList<>();
        
        int pos = 0;
        while (true) {
        	String refName = StringUtils.reverse(fields.get(pos));
        	pos++;
        	ObjectId newObjectId = ObjectId.fromString(StringUtils.reverse(fields.get(pos)));
        	pos++;
        	String field = fields.get(pos);
        	ObjectId oldObjectId = ObjectId.fromString(StringUtils.reverse(field.substring(0, 40)));
        	
        	String branch = GitUtils.ref2branch(refName);
        	if (branch != null && project.getDefaultBranch() == null) {
        		RefUpdate refUpdate = GitUtils.getRefUpdate(project.getRepository(), "HEAD");
        		GitUtils.linkRef(refUpdate, refName);
        	}

        	if (branch != null && project.getDefaultBranch() != null && !branch.equals(project.getDefaultBranch()))
        		showPullRequestLink(output, project, branch);
        	
        	eventData.add(new ImmutableTriple<>(refName, oldObjectId, newObjectId));
    		
        	field = field.substring(40);
        	if (field.length() == 0)
        		break;
        	else
        		fields.set(pos, field);
        }
        
        Long projectId = project.getId();
        
        sessionManager.runAsync(new Runnable() {

			@Override
			public void run() {
		        try {
		        	// We can not use a hibernate entity safely in a different thread. Let's reload it 
		            Project project = projectManager.load(projectId);

		            for (ImmutableTriple<String, ObjectId, ObjectId> each: eventData) {
		            	String refName = each.getLeft();
		            	ObjectId oldObjectId = each.getMiddle();
		            	ObjectId newObjectId = each.getRight();
			        	if (!newObjectId.equals(ObjectId.zeroId()))
			        		project.cacheObjectId(refName, newObjectId);
			        	else 
			        		project.cacheObjectId(refName, null);
		            	
			        	listenerRegistry.post(new RefUpdated(project, refName, oldObjectId, newObjectId));
		            }
		        } catch (Exception e) {
		        	logger.error("Error posting ref updated event", e);
				}
			}
        	
        });
	}

	private void showPullRequestLink(Output output, Project project, String branch) {
    	output.writeLine();
    	output.writeLine("Create a pull request for '"+ branch +"' by visiting:");
		output.writeLine("    " + project.getUrl() 
				+"/pulls/new?target=" 
				+ project.getId() 
				+ ":" 
				+ project.getDefaultBranch() 
				+ "&source=" 
				+ project.getId()
				+ ":"
				+ branch);
		output.writeLine();
	}
	
}
