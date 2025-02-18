package io.onedev.server.web.component.comment;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

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
			var cache = getUserManager().cloneCache();
			var participants = getParticipants();
			var otherUsers = new ArrayList<>(cache.getUsers());
			otherUsers.removeAll(participants);
			
			var similarities = new Similarities<>(participants) {

				@Override
				public double getSimilarScore(User object) {
					return cache.getSimilarScore(object, query);
				}

			};
			if (similarities.size() < count) {
				similarities.addAll(new Similarities<>(otherUsers) {

					@Override
					public double getSimilarScore(User object) {
						return cache.getSimilarScore(object, query);
					}

				});
			}
			
			if (similarities.size() > count)
				return similarities.subList(0, count);
			else 
				return similarities;
		};
	}
	
	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
	protected List<User> getParticipants() {
		return new ArrayList<>();
	}
	
	@Override
	protected final AtWhoReferenceSupport getReferenceSupport() {
		return new AtWhoReferenceSupport() {

			@Override
			public Project getCurrentProject() {
				return getProject();
			}

			@Override
			public List<PullRequest> queryPullRequests(Project project, String query, int count) {
				if (SecurityUtils.canReadCode(project))
					return OneDev.getInstance(PullRequestManager.class).query(project, query, count);
				else
					return new ArrayList<>();
			}

			@Override
			public List<Issue> queryIssues(Project project, String query, int count) {
				if (SecurityUtils.canAccessProject(project)) 
					return OneDev.getInstance(IssueManager.class).query(null, project, query, count);
				else
					return new ArrayList<>();
			}

			@Override
			public List<Build> queryBuilds(Project project, String query, int count) {
				return OneDev.getInstance(BuildManager.class).query(project, query, count);
			}
			
		};
	}
	
	protected abstract Project getProject();
	
}
