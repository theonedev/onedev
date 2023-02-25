package io.onedev.server.job;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.job.match.BranchCriteria;
import io.onedev.server.job.match.JobMatch;
import io.onedev.server.job.match.JobMatchContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.util.WicketUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class JobAuthorizationContext {

	private final Project project;
	
	@Nullable
	private final ObjectId commitId;
	
	@Nullable
	private final User user;
	
	@Nullable
	private final PullRequest request;
	
	private static final ThreadLocal<Stack<JobAuthorizationContext>> stack = ThreadLocal.withInitial(Stack::new);
	
	public JobAuthorizationContext(Project project, @Nullable ObjectId commitId, 
								   @Nullable User user, @Nullable PullRequest request) {
		this.project = project;
		this.commitId = commitId;
		this.user = user;
		this.request = request;
	}
	
	public boolean isScriptAuthorized(GroovyScript script) {
		if (script.getAuthorization() != null) {
			JobMatch jobMatch = JobMatch.parse(script.getAuthorization(), true, false);
			if (request != null && request.getSource() != null) {	
				JobMatchContext sourceContext = new JobMatchContext(request.getSourceProject(), request.getSourceBranch(), null, request.getSubmitter(), null);
				JobMatchContext targetContext = new JobMatchContext(request.getTargetProject(), request.getTargetBranch(), null, request.getSubmitter(), null);
				return jobMatch.matches(sourceContext) && jobMatch.matches(targetContext);
			} else {
				return jobMatch.matches(new JobMatchContext(project, null, commitId, user, null));
			}
		} else {
			return true;
		}
	}

	public String getSecretValue(String secretName) {
		if (secretName.startsWith(SecretInput.LITERAL_VALUE_PREFIX)) {
			return secretName.substring(SecretInput.LITERAL_VALUE_PREFIX.length());
		} else {
			for (JobSecret secret: project.getHierarchyJobSecrets()) {
				if (secret.getName().equals(secretName)) {
					String authorization = secret.getAuthorization();
					if (authorization == null)
						authorization = new BranchCriteria("**").toString();
					JobMatch jobMatch = JobMatch.parse(authorization, false, false);
					if (request != null) {
						if (project.equals(request.getSourceProject())) {
							JobMatchContext sourceMatchContext = new JobMatchContext(project, request.getSourceBranch(), null, request.getSubmitter(), null);
							JobMatchContext targetMatchContext = new JobMatchContext(project, request.getTargetBranch(), null, request.getSubmitter(), null);
							if (jobMatch.matches(sourceMatchContext) && jobMatch.matches(targetMatchContext))
								return normalizeSecretValue(secret.getValue());
						} else {
							JobMatchContext matchContext = new JobMatchContext(project, null, commitId, request.getSubmitter(), null);
							if (jobMatch.matches(matchContext))
								return normalizeSecretValue(secret.getValue());
						}
					} else {
						JobMatchContext matchContext = new JobMatchContext(project, null, commitId, user, null);
						if (jobMatch.matches(matchContext)) 
							return normalizeSecretValue(secret.getValue());
					}
					throw new ExplicitException(String.format(
							"Job secret not authorized (project: %s, job secret: %s)",
							project.getPath(), secretName));
				}
			}
			throw new ExplicitException(String.format(
					"Job secret not found (project: %s, job secret: %s)",
					project.getPath(), secretName));
		}
	}
	
	private String normalizeSecretValue(String secretValue) {
		return secretValue.replace("\r\n", "\n");
	}

	public static void push(JobAuthorizationContext jobAuthorizationContext) {
		stack.get().push(jobAuthorizationContext);
	}

	public static void pop() {
		stack.get().pop();
	}

	@javax.annotation.Nullable
	public static JobAuthorizationContext get() {
		if (!stack.get().isEmpty()) {
			return stack.get().peek();
		} else {
			ComponentContext componentContext = ComponentContext.get();
			if (componentContext != null) {
				JobAuthorizationContextAware jobAuthorizationContextAware = WicketUtils.findInnermost(
						componentContext.getComponent(),
						JobAuthorizationContextAware.class);
				if (jobAuthorizationContextAware != null)
					return jobAuthorizationContextAware.getJobAuthorizationContext();
			}
			return null;
		}
	}

}
