package io.onedev.server.job;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Stack;

import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.job.match.JobMatch;
import io.onedev.server.job.match.JobMatchContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.util.WicketUtils;

public class JobAuthorizationContext {
	
	private final Project project;
	
	@Nullable
	private final ObjectId commitId;
		
	@Nullable
	private final PullRequest request;
	
	private static final ThreadLocal<Stack<JobAuthorizationContext>> stack = ThreadLocal.withInitial(Stack::new);
	
	public JobAuthorizationContext(Project project, @Nullable ObjectId commitId, @Nullable PullRequest request) {
		this.project = project;
		this.commitId = commitId;
		this.request = request;
	}
	
	public boolean isScriptAuthorized(GroovyScript script) {
		if (script.getAuthorization() != null) {
			JobMatch jobMatch = JobMatch.parse(script.getAuthorization(), true, false);
			if (request != null) {	
				if (request.getSource() != null) {
					JobMatchContext sourceContext = new JobMatchContext(request.getSourceProject(), request.getSourceBranch(), null, null);
					JobMatchContext targetContext = new JobMatchContext(request.getTargetProject(), request.getTargetBranch(), null, null);
					return jobMatch.matches(sourceContext) && jobMatch.matches(targetContext);
				} else {
					return false;					
				}
			} else {
				return jobMatch.matches(new JobMatchContext(project, null, commitId, null));
			}
		} else {
			return true;
		}
	}
	
	public Subject getSubject(@Nullable String accessTokenSecret) {
		if (accessTokenSecret != null) {
			String secretValue = getSecretValue(accessTokenSecret);
			var accessToken = OneDev.getInstance(AccessTokenService.class).findByValue(secretValue);
			if (accessToken == null)
				throw new ExplicitException(MessageFormat.format(_T("Invalid access token: {0}"), secretValue));
			return accessToken.asSubject();
		} else {
			return SecurityUtils.asAnonymous();
		}
	}

	public String getSecretValue(String secretName) {
		if (secretName.startsWith(SecretInput.LITERAL_VALUE_PREFIX)) {
			return secretName.substring(SecretInput.LITERAL_VALUE_PREFIX.length());
		} else {
			for (JobSecret secret: project.getHierarchyJobSecrets()) {
				if (secret.getName().equals(secretName)) {
					String authorization = secret.getAuthorization();
					if (authorization == null) {
						return normalizeSecretValue(secret.getValue());
					} else {
						JobMatch jobMatch = JobMatch.parse(authorization, false, false);
						if (request != null) {
							if (project.equals(request.getSourceProject())) {
								JobMatchContext sourceMatchContext = new JobMatchContext(project, request.getSourceBranch(), null, null);
								JobMatchContext targetMatchContext = new JobMatchContext(project, request.getTargetBranch(), null, null);
								if (jobMatch.matches(sourceMatchContext) && jobMatch.matches(targetMatchContext))
									return normalizeSecretValue(secret.getValue());
							} else {
								JobMatchContext matchContext = new JobMatchContext(project, null, commitId, null);
								if (jobMatch.matches(matchContext))
									return normalizeSecretValue(secret.getValue());
							}
						} else {
							JobMatchContext matchContext = new JobMatchContext(project, null, commitId, null);
							if (jobMatch.matches(matchContext))
								return normalizeSecretValue(secret.getValue());
						}
					}
				}
			}
			throw new ExplicitException(MessageFormat.format(
					_T("No authorized job secret found (project: {0}, job secret: {1})"),
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

	@org.jspecify.annotations.Nullable
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
