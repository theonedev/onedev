package io.onedev.server.web.component.issue.progress;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.IssueWorkService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.StopwatchService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.Stopwatch;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.timetracking.TimeTrackingService;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.editbean.IssueWorkBean;

abstract class TimingDetailPanel extends Panel {

	private Page page;

	public TimingDetailPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		page = getPage();
		
		var timeTrackingSetting = OneDev.getInstance(SettingService.class).getIssueSetting().getTimeTrackingSetting();
		var timeAggregationLink = timeTrackingSetting.getAggregationLink();
		
		boolean timeAggregation = getIssue().isAggregatingTime(timeAggregationLink);
		if (timeAggregation) {
			var fragment = new Fragment("content", "aggregationFrag", this);

			int estimatedTime = getIssue().getTotalEstimatedTime();
			fragment.add(new Label("estimatedTime", timeTrackingSetting.formatWorkingPeriod(estimatedTime, true)));
			fragment.add(new Label("ownEstimatedTime", timeTrackingSetting.formatWorkingPeriod(getIssue().getOwnEstimatedTime(), true)));
			fragment.add(newEstimatedTimeEditLink("editOwnEstimatedTime"));
			
			int aggregatedTime = getTimeTrackingService().aggregateSourceLinkEstimatedTime(getIssue(), timeAggregationLink);
			aggregatedTime += getTimeTrackingService().aggregateTargetLinkEstimatedTime(getIssue(), timeAggregationLink);
			
			fragment.add(new Label("estimatedTimeAggregationLink", timeAggregationLink));
			fragment.add(new Label("aggregatedEstimatedTime", timeTrackingSetting.formatWorkingPeriod(aggregatedTime, true)));

			int spentTime = getIssue().getTotalSpentTime();
			fragment.add(new Label("spentTime", timeTrackingSetting.formatWorkingPeriod(spentTime, true)));
			fragment.add(new Label("ownSpentTime", timeTrackingSetting.formatWorkingPeriod(getIssue().getOwnSpentTime(), true)));
			fragment.add(newLogWorkLink("logWork"));
			fragment.add(newStartWorkLink("startWork"));
			
			aggregatedTime = getTimeTrackingService().aggregateSourceLinkSpentTime(getIssue(), timeAggregationLink);
			aggregatedTime += getTimeTrackingService().aggregateTargetLinkSpentTime(getIssue(), timeAggregationLink);
			fragment.add(new Label("spentTimeAggregationLink", timeAggregationLink));
			fragment.add(new Label("aggregatedSpentTime", timeTrackingSetting.formatWorkingPeriod(aggregatedTime, true)));
			
			add(fragment);
		} else {
			var fragment = new Fragment("content", "noAggregationFrag", this);
			
			int estimatedTime = getIssue().getTotalEstimatedTime();
			fragment.add(new Label("estimatedTime", timeTrackingSetting.formatWorkingPeriod(estimatedTime, true)));
			fragment.add(newEstimatedTimeEditLink("editEstimatedTime"));

			int spentTime = getIssue().getTotalSpentTime();
			fragment.add(new Label("spentTime", timeTrackingSetting.formatWorkingPeriod(spentTime, true)));
			fragment.add(newLogWorkLink("logWork"));
			fragment.add(newStartWorkLink("startWork"));
			
			add(fragment);
		}
	}

	private Component newEstimatedTimeEditLink(String componentId) {
		return new AjaxLink<Void>(componentId) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				closeDropdown();
				var bean = new EstimatedTimeEditBean();
				if (getIssue().getOwnEstimatedTime() != 0)
					bean.setEstimatedTime(getIssue().getOwnEstimatedTime());
				new BeanEditModalPanel<>(target, bean) {

					@Override
					protected String onSave(AjaxRequestTarget target, EstimatedTimeEditBean bean) {
						getIssueChangeService().changeOwnEstimatedTime(SecurityUtils.getUser(), getIssue(), bean.getEstimatedTime());
						notifyObservablesChange(target);
						close();
						return null;
					}
				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getAuthUser() != null);
			}
		};
	}
	
	private Component newLogWorkLink(String componentId) {
		return new AjaxLink<Void>(componentId) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				closeDropdown();
				var bean = new IssueWorkBean();
				new BeanEditModalPanel<>(target, bean) {

					@Override
					protected String onSave(AjaxRequestTarget target, IssueWorkBean bean) {
						IssueWork work = new IssueWork();
						work.setIssue(getIssue());
						work.setUser(SecurityUtils.getAuthUser());
						work.setMinutes(bean.getSpentTime());
						work.setDate(bean.getStartAt());
						work.setNote(bean.getNote());
						OneDev.getInstance(IssueWorkService.class).createOrUpdate(work);
						notifyObservablesChange(target);
						close();
						return null;
					}

					@Override
					protected boolean isDirtyAware() {
						return false;
					}
				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				var user = SecurityUtils.getAuthUser();
				setVisible(user != null && getStopWatchManager().find(user, getIssue()) == null);
			}
		};
	}

	private Component newStartWorkLink(String componentId) {
		return new AjaxLink<Void>(componentId) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				closeDropdown();
				var stopwatch = getStopWatchManager().startWork(SecurityUtils.getAuthUser(), getIssue());
				onWorkStarted(target, stopwatch);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				var user = SecurityUtils.getAuthUser();
				setVisible(user != null && getStopWatchManager().find(user, getIssue()) == null);
			}
		};
	}
	
	private StopwatchService getStopWatchManager() {
		return OneDev.getInstance(StopwatchService.class);
	}
	
	private TimeTrackingService getTimeTrackingService() {
		return OneDev.getInstance(TimeTrackingService.class);
	}
	
	private IssueChangeService getIssueChangeService() {
		return OneDev.getInstance(IssueChangeService.class);
	}
	
	private void notifyObservablesChange(AjaxRequestTarget target) {
		((BasePage)page).notifyObservablesChange(target, Sets.newHashSet(Issue.getDetailChangeObservable(getIssue().getId())));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ProgressResourceReference()));
	}
	
	protected abstract Issue getIssue();
	
	protected abstract void onWorkStarted(AjaxRequestTarget target, Stopwatch stopwatch);
	
	protected abstract void closeDropdown();
	
}
