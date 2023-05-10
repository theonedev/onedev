package io.onedev.server.web.component.comment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.model.IModel;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Similarities;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.web.component.markdown.AtWhoReferenceSupport;
import io.onedev.server.web.component.markdown.MarkdownEditor;
import io.onedev.server.web.component.markdown.UserMentionSupport;

@SuppressWarnings("serial")
public abstract class CommentInput extends MarkdownEditor {

	public CommentInput(String id, IModel<String> model, boolean compactMode) {
		super(id, model, compactMode, null);
	}

	@Override
	protected final UserMentionSupport getUserMentionSupport() {
		return (query, count) -> {
			List<User> mentionables = getMentionables();
			UserCache cache = getUserManager().cloneCache();
			
			List<User> similarities = new Similarities<User>(mentionables) {

				@Override
				public double getSimilarScore(User object) {
					return cache.getSimilarScore(object, query);
				}
				
			};
			
			if (similarities.size() > count)
				return similarities.subList(0, count);
			else
				return similarities;
		};
	}
	
	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
	protected List<User> getMentionables() {
		UserCache cache = getUserManager().cloneCache();
		List<User> users = new ArrayList<>(cache.getUsers());
		users.sort(cache.comparingDisplayName(Sets.newHashSet()));
		return users;
	}

	@Override
	protected final AtWhoReferenceSupport getReferenceSupport() {
		return new AtWhoReferenceSupport() {

			@Override
			public List<PullRequest> findPullRequests(@Nullable Project project, String query, int count) {
				if (project == null)
					project = getProject();
				if (SecurityUtils.canReadCode(project))
					return OneDev.getInstance(PullRequestManager.class).query(project, query, count);
				else
					return new ArrayList<>();
			}

			@Override
			public List<Issue> findIssues(@Nullable Project project, String query, int count) {
				if (project == null) 
					project = getProject();
				if (SecurityUtils.canAccess(project)) 
					return OneDev.getInstance(IssueManager.class).query(null, project, query, count);
				else
					return new ArrayList<>();
			}

			@Override
			public List<Build> findBuilds(@Nullable Project project, String query, int count) {
				if (project == null)
					project = getProject();
				return OneDev.getInstance(BuildManager.class).query(project, query, count);
			}
			
		};
	}
	
	protected abstract Project getProject();
	
}
