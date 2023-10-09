package io.onedev.server.web.component.issue.progress;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.manager.StopWatchManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.StopWatch;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.CompletionRateBehavior;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.util.WicketUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.Collection;

public abstract class IssueProgressPanel extends Panel {

	private final IModel<StopWatch> stopWatchModel = new LoadableDetachableModel<>() {
		@Override
		protected StopWatch load() {
			var user = SecurityUtils.getUser();
			if (user != null)
				return getStopWatchManager().find(user, getIssue());
			else
				return null;
		}
	};
	
	public IssueProgressPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new DropdownLink("completionRate") {
			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new TimingDetailPanel(id) {
					@Override
					protected Issue getIssue() {
						return IssueProgressPanel.this.getIssue();
					}

					@Override
					protected void onWorkStarted(AjaxRequestTarget target, StopWatch stopWatch) {
						stopWatchModel.setObject(stopWatch);
						target.add(IssueProgressPanel.this);
					}

					@Override
					protected void closeDropdown() {
						dropdown.close();
					}

				};
			}
		}.add(new CompletionRateBehavior() {
			@Override
			protected long getTotal() {
				return getIssue().getTotalEstimatedTime();
			}

			@Override
			protected long getCompleted() {
				return getIssue().getTotalSpentTime();
			}
		}));
		
		add(new WebMarkupContainer("workingTime") {
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				var script = String.format("onedev.server.issueProgress.onWorkingTimeDomReady('%s', %d);", 
						getMarkupId(), System.currentTimeMillis() - getStopWatch().getDate().getTime());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getStopWatch() != null);
			}
		}.setOutputMarkupId(true));
		
		add(new AjaxLink<Void>("stopWork") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				getStopWatchManager().stopWork(getStopWatch());
				stopWatchModel.setObject(null);
				target.add(IssueProgressPanel.this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getStopWatch() != null);
			}
		});

		add(new ChangeObserver() {
			@Override
			protected Collection<String> findObservables() {
				return Sets.newHashSet(Issue.getDetailChangeObservable(getIssue().getId()));
			}
		});
		
	}

	private StopWatch getStopWatch() {
		return stopWatchModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		stopWatchModel.detach();
		super.onDetach();
	}

	private StopWatchManager getStopWatchManager() {
		return OneDev.getInstance(StopWatchManager.class);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getIssue().getProject().isTimeTracking() && WicketUtils.isSubscriptionActive());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ProgressResourceReference()));
	}

	protected abstract Issue getIssue();
	
}
