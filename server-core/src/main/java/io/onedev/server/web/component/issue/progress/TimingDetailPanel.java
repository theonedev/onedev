package io.onedev.server.web.component.issue.progress;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueWorkManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.StopwatchManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.Stopwatch;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.timetracking.TimeTrackingManager;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.editablebean.IssueWorkBean;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import static io.onedev.server.util.DateUtils.formatWorkingPeriod;

abstract class TimingDetailPanel extends Panel {

	private Page page;

	public TimingDetailPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		page = getPage();
		
		var timeTrackingSetting = OneDev.getInstance(SettingManager.class).getIssueSetting().getTimeTrackingSetting();
		var timeAggregationLink = timeTrackingSetting.getAggregationLink();
		
		boolean timeAggregation = getIssue().isAggregatingTime(timeAggregationLink);
		
		if (timeAggregation) {
			var fragment = new Fragment("content", "aggregationFrag", this);

			int estimatedTime = getIssue().getTotalEstimatedTime();
			fragment.add(new Label("estimatedTime", formatWorkingPeriod(estimatedTime)));
			fragment.add(new Label("ownEstimatedTime", formatWorkingPeriod(getIssue().getOwnEstimatedTime())));
			fragment.add(newEstimatedTimeEditLink("editOwnEstimatedTime"));
			
			int aggregatedTime = getTimeTrackingManager().aggregateSourceLinkEstimatedTime(getIssue(), timeAggregationLink);
			aggregatedTime += getTimeTrackingManager().aggregateTargetLinkEstimatedTime(getIssue(), timeAggregationLink);
			
			fragment.add(new Label("estimatedTimeAggregationLink", timeAggregationLink));
			fragment.add(new Label("aggregatedEstimatedTime", formatWorkingPeriod(aggregatedTime)));

			int spentTime = getIssue().getTotalSpentTime();
			fragment.add(new Label("spentTime", formatWorkingPeriod(spentTime)));
			fragment.add(new Label("ownSpentTime", formatWorkingPeriod(getIssue().getOwnSpentTime())));
			fragment.add(newLogWorkLink("logWork"));
			fragment.add(newStartWorkLink("startWork"));
			
			aggregatedTime = getTimeTrackingManager().aggregateSourceLinkSpentTime(getIssue(), timeAggregationLink);
			aggregatedTime += getTimeTrackingManager().aggregateTargetLinkSpentTime(getIssue(), timeAggregationLink);
			fragment.add(new Label("spentTimeAggregationLink", timeAggregationLink));
			fragment.add(new Label("aggregatedSpentTime", formatWorkingPeriod(aggregatedTime)));
			
			add(fragment);
		} else {
			var fragment = new Fragment("content", "noAggregationFrag", this);
			
			int estimatedTime = getIssue().getTotalEstimatedTime();
			fragment.add(new Label("estimatedTime", formatWorkingPeriod(estimatedTime)));
			fragment.add(newEstimatedTimeEditLink("editEstimatedTime"));

			int spentTime = getIssue().getTotalSpentTime();
			fragment.add(new Label("spentTime", formatWorkingPeriod(spentTime)));
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
					protected void onSave(AjaxRequestTarget target, EstimatedTimeEditBean bean) {
						getIssueChangeManager().changeOwnEstimatedTime(getIssue(), bean.getEstimatedTime());
						notifyObservablesChange(target);
						close();
					}
				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getUser() != null);
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
					protected void onSave(AjaxRequestTarget target, IssueWorkBean bean) {
						IssueWork work = new IssueWork();
						work.setIssue(getIssue());
						work.setUser(SecurityUtils.getUser());
						work.setMinutes(bean.getSpentTime());
						work.setDate(bean.getStartAt());
						work.setDay(DateUtils.toLocalDate(bean.getStartAt()).toEpochDay());
						work.setNote(bean.getNote());
						OneDev.getInstance(IssueWorkManager.class).createOrUpdate(work);
						notifyObservablesChange(target);
						close();
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
				var user = SecurityUtils.getUser();
				setVisible(user != null && !user.isEffectiveGuest() && getStopWatchManager().find(user, getIssue()) == null);
			}
		};
	}

	private Component newStartWorkLink(String componentId) {
		return new AjaxLink<Void>(componentId) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				closeDropdown();
				var stopwatch = getStopWatchManager().startWork(SecurityUtils.getUser(), getIssue());
				onWorkStarted(target, stopwatch);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				var user = SecurityUtils.getUser();
				setVisible(user != null && !user.isEffectiveGuest() && getStopWatchManager().find(user, getIssue()) == null);
			}
		};
	}
	
	private StopwatchManager getStopWatchManager() {
		return OneDev.getInstance(StopwatchManager.class);
	}
	
	private TimeTrackingManager getTimeTrackingManager() {
		return OneDev.getInstance(TimeTrackingManager.class);
	}
	
	private IssueChangeManager getIssueChangeManager() {
		return OneDev.getInstance(IssueChangeManager.class);
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
