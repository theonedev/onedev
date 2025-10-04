package io.onedev.server.web.component.comment;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.Similarities;
import io.onedev.server.web.component.markdown.AtWhoReferenceSupport;
import io.onedev.server.web.component.markdown.MarkdownEditor;
import io.onedev.server.web.component.markdown.UserMentionSupport;

public abstract class CommentInput extends MarkdownEditor {

	public CommentInput(String id, IModel<String> model, boolean compactMode) {
		super(id, model, compactMode, null);
	}

	@Override
	protected final UserMentionSupport getUserMentionSupport() {
		return (query, count) -> {
			var cache = getUserService().cloneCache();
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
	
	private UserService getUserService() {
		return OneDev.getInstance(UserService.class);
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
				var subject = SecurityUtils.getSubject();
				if (SecurityUtils.canReadCode(subject, project)) {
					var requestQuery = new PullRequestQuery(new io.onedev.server.search.entity.pullrequest.FuzzyCriteria(query));
					return OneDev.getInstance(PullRequestService.class).query(subject, project, requestQuery, false, 0, count);
				} else {
					return new ArrayList<>();
				}
			}

			@Override
			public List<Issue> queryIssues(Project project, String query, int count) {
				var subject = SecurityUtils.getSubject();
				if (SecurityUtils.canAccessProject(subject, project)) {
					var projectScope = new ProjectScope(project, false, false);
					var issueQuery = new IssueQuery(new io.onedev.server.search.entity.issue.FuzzyCriteria(query));
					return OneDev.getInstance(IssueService.class).query(subject, projectScope, issueQuery, false, 0, count);
				} else {
					return new ArrayList<>();
				}
			}

			@Override
			public List<Build> queryBuilds(Project project, String query, int count) {
				return OneDev.getInstance(BuildService.class).query(SecurityUtils.getSubject(), project, query, count);
			}
			
		};
	}
	
	protected abstract Project getProject();
	
}
