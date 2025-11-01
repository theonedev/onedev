package io.onedev.server.git.hook;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.persistence.annotation.Sessional;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static io.onedev.server.security.SecurityUtils.*;

@Singleton
public class GitPreReceiveCallback extends HttpServlet {

	public static final String PATH = "/git-prereceive-callback";

	private final ProjectService projectService;
	
	private final Set<GitPreReceiveChecker> preReceiveCheckers;
	
	@Inject
	public GitPreReceiveCallback(ProjectService projectService, Set<GitPreReceiveChecker> preReceiveCheckers) {
		this.projectService = projectService;
		this.preReceiveCheckers = preReceiveCheckers;
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
        List<String> fields = StringUtils.splitAndTrim(request.getPathInfo(), "/");
        Preconditions.checkState(fields.size() == 3);
        
        if (!fields.get(2).equals(HookUtils.HOOK_TOKEN)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Git hook callbacks can only be accessed by OneDev itself");
            return;
        }

		var principal = fields.get(1);
        if (!isSystem(principal)) { // not access with cluster credential
			ThreadContext.bind(asSubject(asPrincipals(principal)));
			Project project = projectService.load(Long.valueOf(fields.get(0)));
			
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
				
				User user = getUser();

				if (refName.startsWith(PullRequest.REFS_PREFIX) || refName.startsWith(PullRequestUpdate.REFS_PREFIX)) {
					if (!canManageProject(project)) {
						error(output, refName, Lists.newArrayList("Only project managers can update onedev refs."));
						break;
					}
				} else if (refName.startsWith(Constants.R_HEADS)) {
					String branchName = Preconditions.checkNotNull(GitUtils.ref2branch(refName));
					List<String> errorMessages = new ArrayList<>();
					BranchProtection protection = project.getBranchProtection(branchName, user);
					if (oldObjectId.equals(ObjectId.zeroId())) {
						if (protection.isPreventCreation()) {
							errorMessages.add("Can not create this branch according to branch protection setting");
						} else if (protection.isCommitSignatureRequired() 
								&& !project.hasValidCommitSignature(newObjectId, gitEnvs)) {
							errorMessages.add("Can not create this branch as branch protection setting "
									+ "requires valid signature on head commit");
						}
					} else if (newObjectId.equals(ObjectId.zeroId())) {
						if (protection.isPreventDeletion()) 
							errorMessages.add("Can not delete this branch according to branch protection setting");
					} else if (protection.isPreventForcedPush() 
							&& !GitUtils.isMergedInto(projectService.getRepository(project.getId()), gitEnvs, oldObjectId, newObjectId)) {
						errorMessages.add("Can not force-push to this branch according to branch protection setting");
					} else if (protection.isCommitSignatureRequired() 
							&& !project.hasValidCommitSignature(newObjectId, gitEnvs)) {
						errorMessages.add("Can not push to this branch as branch protection rule requires "
								+ "valid signature for head commit");
					} else if (protection.isReviewRequiredForPush(project, oldObjectId, newObjectId, gitEnvs)) {
						errorMessages.add("Review required for your change. Please submit pull request instead");
					}
					if (errorMessages.isEmpty() && !newObjectId.equals(ObjectId.zeroId())) {
						var commitMessageError = project.checkCommitMessages(branchName, user, oldObjectId, newObjectId, gitEnvs);
						if (commitMessageError != null)
							errorMessages.add(commitMessageError.toString());
					}
					if (errorMessages.isEmpty()) {
						var violatedFileTypes = protection.getViolatedFileTypes(project, oldObjectId, newObjectId, gitEnvs);
						if (!violatedFileTypes.isEmpty()) {
							errorMessages.add("Your push contains disallowed file type(s): " + StringUtils.join(violatedFileTypes, ", "));
						}
					}
					if (errorMessages.isEmpty() 
							&& !oldObjectId.equals(ObjectId.zeroId()) 
							&& !newObjectId.equals(ObjectId.zeroId()) 
							&& project.isBuildRequiredForPush(user, branchName, oldObjectId, newObjectId, gitEnvs)) {
						errorMessages.add("Build required for your change. Please submit pull request instead");
					}
					if (errorMessages.isEmpty()) {
						for (var preReceiveChecker : preReceiveCheckers) {
							var errorMessage = preReceiveChecker.check(project, user, refName, oldObjectId, newObjectId);
							if (errorMessage != null)
								errorMessages.add(errorMessage);
						}
					}
					if (errorMessages.isEmpty() && newObjectId.equals(ObjectId.zeroId())) {
						try {
							projectService.onDeleteBranch(project, branchName);
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
						if (protection.isPreventCreation()) {
							errorMessages.add("Can not create this tag according to tag protection setting");
						} else if (protection.isCommitSignatureRequired() 
								&& !project.hasValidTagSignature(newObjectId, gitEnvs)) {
							errorMessages.add("Can not create this tag as tag protection setting requires "
									+ "valid tag signature");
						}
					} else if (newObjectId.equals(ObjectId.zeroId())) {
						if (protection.isPreventDeletion())
							errorMessages.add("Can not delete this tag according to tag protection setting");
					} else if (protection.isPreventUpdate()) {
						errorMessages.add("Can not update this tag according to tag protection setting");
					} else if (protection.isCommitSignatureRequired() 
							&& !project.hasValidTagSignature(newObjectId, gitEnvs)) {
						errorMessages.add("Can not update this tag as tag protection setting requires "
								+ "valid tag signature");
					}
					if (errorMessages.isEmpty() && !protection.getDisallowedFileTypes().isEmpty()) {
						var violatedFileTypes = protection.getViolatedFileTypes(project, newObjectId, gitEnvs);
						if (!violatedFileTypes.isEmpty()) {
							errorMessages.add("Your push contains disallowed file type(s): " + StringUtils.join(violatedFileTypes, ", "));
						}
					}
					if (errorMessages.isEmpty()) {
						for (var preReceiveChecker : preReceiveCheckers) {
							var errorMessage = preReceiveChecker.check(project, user, refName, oldObjectId, newObjectId);
							if (errorMessage != null)
								errorMessages.add(errorMessage);
						}
					}
					if (errorMessages.isEmpty() && newObjectId.equals(ObjectId.zeroId())) {
						try {
							projectService.onDeleteTag(project, tagName);
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
        }
	}	
}
