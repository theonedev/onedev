package io.onedev.server.web.component.issue.progress;

import com.google.common.collect.Sets;
import io.onedev.server.model.Issue;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.CompletionRateBehavior;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.util.WicketUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;

import java.util.Collection;

public abstract class IssueProgressLink extends DropdownLink {
	
	public IssueProgressLink(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new CompletionRateBehavior() {
			@Override
			protected long getTotal() {
				return getIssue().getTotalEstimatedTime();
			}

			@Override
			protected long getCompleted() {
				return getIssue().getTotalSpentTime();
			}
		});
		
		add(AttributeAppender.append("class", "issue-progress d-inline-block"));
		add(AttributeAppender.append("title", "Estimated/Spent time. Click for details"));
		
		add(new ChangeObserver() {
			@Override
			protected Collection<String> findObservables() {
				return Sets.newHashSet(Issue.getDetailChangeObservable(getIssue().getId()));
			}
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getIssue().getProject().isTimeTracking() && WicketUtils.isSubscriptionActive());
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new IssueTimePanel(id) {
			@Override
			protected Issue getIssue() {
				return IssueProgressLink.this.getIssue();
			}

			@Override
			protected void closeDropdown() {
				dropdown.close();
			}

		};
	}

	protected abstract Issue getIssue();
	
}
