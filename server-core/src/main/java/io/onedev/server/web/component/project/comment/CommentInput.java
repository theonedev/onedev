package io.onedev.server.web.component.project.comment;

import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.commons.utils.matchscore.MatchScoreProvider;
import io.onedev.commons.utils.matchscore.MatchScoreUtils;
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
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.component.markdown.AtWhoReferenceSupport;
import io.onedev.server.web.component.markdown.MarkdownEditor;
import io.onedev.server.web.component.markdown.UserMentionSupport;

@SuppressWarnings("serial")
public abstract class CommentInput extends MarkdownEditor {

	public CommentInput(String id, IModel<String> model, boolean compactMode) {
		super(id, model, compactMode, null);
	}

	@Override
	protected UserMentionSupport getUserMentionSupport() {
		return new UserMentionSupport() {

			@Override
			public List<User> findUsers(String query, int count) {
				List<User> users = OneDev.getInstance(UserManager.class).query();
				users.sort(Comparator.comparing(User::getDisplayName));
				
				users = MatchScoreUtils.filterAndSort(users, new MatchScoreProvider<User>() {

					@Override
					public double getMatchScore(User object) {
						return object.getMatchScore(query);
					}
					
				});
				
				if (users.size() > count)
					return users.subList(0, count);
				else
					return users;
			}
			
		};
	}

	@Override
	protected AtWhoReferenceSupport getReferenceSupport() {
		return new AtWhoReferenceSupport() {

			@Override
			public List<PullRequest> findPullRequests(Project project, String query, int count) {
				if (project == null)
					project = getProject();
				return OneDev.getInstance(PullRequestManager.class).query(project, query, count);
			}

			@Override
			public List<Issue> findIssues(Project project, String query, int count) {
				if (project == null)
					project = getProject();
				return OneDev.getInstance(IssueManager.class).query(project, query, count);
			}

			@Override
			public List<Build> findBuilds(Project project, String query, int count) {
				if (project == null)
					project = getProject();
				return OneDev.getInstance(BuildManager.class).query(project, SecurityUtils.getUser(), query, count);
			}
			
		};
	}

	protected abstract Project getProject();
	
}
