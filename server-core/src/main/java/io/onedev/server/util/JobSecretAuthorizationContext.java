package io.onedev.server.util;

import java.util.Stack;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.util.WicketUtils;

public class JobSecretAuthorizationContext {

	private final Project project;
	
	private final ObjectId commitId;
	
	private final PullRequest pullRequest;
	
	public JobSecretAuthorizationContext(Project project, ObjectId commitId, @Nullable PullRequest pullRequest) {
		this.project = project;
		this.commitId = commitId;
		this.pullRequest = pullRequest;
	}
	
	public Project getProject() {
		return project;
	}

	public ObjectId getCommitId() {
		return commitId;
	}

	@Nullable
	public PullRequest getPullRequest() {
		return pullRequest;
	}
	
	public boolean isOnBranches(@Nullable String branches) {
		if (branches == null)
			branches = "**";
		if (project.isCommitOnBranches(getCommitId(), branches)) {
			return true;
		} else if (pullRequest != null) {
			PatternSet patternSet = PatternSet.parse(branches);
			PathMatcher matcher = new PathMatcher();
			return project.equals(pullRequest.getSourceProject()) 
					&& patternSet.matches(matcher, pullRequest.getTargetBranch())
					&& patternSet.matches(matcher, pullRequest.getSourceBranch());
		} else {
			return false;
		}
	}
	
	public String getSecretValue(String secretName) {
		if (secretName.startsWith(SecretInput.LITERAL_VALUE_PREFIX)) {
			return secretName.substring(SecretInput.LITERAL_VALUE_PREFIX.length());
		} else {
			for (JobSecret secret: project.getHierarchyJobSecrets()) {
				if (secret.getName().equals(secretName)) {
					if (isOnBranches(secret.getAuthorizedBranches())) {			
						return secret.getValue().replace("\r\n", "\n");
					} else {
						throw new ExplicitException(String.format(
								"Job secret not authorized (project: %s, job secret: %s)", 
								project.getPath(), secretName));
					}
				}
			}
			throw new ExplicitException(String.format(
					"Job secret not found (project: %s, job secret: %s)", 
					project.getPath(), secretName));
		}
	}
	
	public String getSecretValue(String branchName, String secretName) {
		if (secretName.startsWith(SecretInput.LITERAL_VALUE_PREFIX)) {
			return secretName.substring(SecretInput.LITERAL_VALUE_PREFIX.length());
		} else {
			JobSecret secret = null;
			for (JobSecret each: project.getHierarchyJobSecrets()) {
				if (each.getName().equals(secretName)) { 
					secret = each;
					break;
				}
			}
			if (secret == null) {
				throw new ExplicitException(String.format(
						"Job secret not found (project: %s, job secret: %s)", 
						project.getPath(), secretName));
			}

			String authorizedBranches = secret.getAuthorizedBranches();
			if (authorizedBranches == null)
				authorizedBranches = "**";
			
			PatternSet patternSet = PatternSet.parse(authorizedBranches);
			PathMatcher matcher = new PathMatcher();
			if (!patternSet.matches(matcher, branchName)) {
				throw new ExplicitException(String.format(
						"Job secret not authorized (project: %s, job secret: %s)", 
						project.getPath(), secretName));
			}
			return secret.getValue().replace("\r\n", "\n");
		}
	}
	
	private static ThreadLocal<Stack<JobSecretAuthorizationContext>> stack =  
			new ThreadLocal<Stack<JobSecretAuthorizationContext>>() {

		@Override
		protected Stack<JobSecretAuthorizationContext> initialValue() {
			return new Stack<>();
		}
	
	};
	
	public static void push(JobSecretAuthorizationContext authorizationContext) {
		stack.get().push(authorizationContext);
	}

	public static void pop() {
		stack.get().pop();
	}
	
	@Nullable
	public static JobSecretAuthorizationContext get() {
		if (!stack.get().isEmpty()) { 
			return stack.get().peek();
		} else {
			ComponentContext componentContext = ComponentContext.get();
			if (componentContext != null) {
				JobSecretAuthorizationContextAware jobSecretAuthorizationContextAware = WicketUtils.findInnermost(
						componentContext.getComponent(), 
						JobSecretAuthorizationContextAware.class);
				if (jobSecretAuthorizationContextAware != null) 
					return jobSecretAuthorizationContextAware.getJobSecretAuthorizationContext();
			}
			return null;
		}
	}
	
}
