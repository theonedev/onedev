package io.onedev.server.model.support.pullrequest.changedata;

import java.util.List;

import org.apache.wicket.Component;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.util.diff.DiffSupport;
import io.onedev.server.web.page.project.pullrequests.detail.activities.activity.DiffAndCommentAwarePanel;

public class PullRequestTitleChangeData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldTitle;
	
	private final String newTitle;
	
	public PullRequestTitleChangeData(String oldTitle, String newTitle) {
		this.oldTitle = oldTitle;
		this.newTitle = newTitle;
	}
	
	@Override
	public String getActivity(PullRequest withRequest) {
		String activity = "changed title";
		if (withRequest != null)
			activity += " of pull request " + withRequest.describe();
		return activity;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		Long changeId = change.getId();
		return new DiffAndCommentAwarePanel(componentId) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected PullRequestChange getChange() {
				return OneDev.getInstance(PullRequestChangeManager.class).load(changeId);
			}

			@Override
			protected DiffSupport getDiffSupport() {
				return new DiffSupport() {

					private static final long serialVersionUID = 1L;

					@Override
					public List<String> getOldLines() {
						return Lists.newArrayList(oldTitle);
					}

					@Override
					public List<String> getNewLines() {
						return Lists.newArrayList(newTitle);
					}

					@Override
					public String getOldFileName() {
						return "a.txt";
					}

					@Override
					public String getNewFileName() {
						return "b.txt";
					}
					
				};
			}
		};
	}
	
	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}

}
