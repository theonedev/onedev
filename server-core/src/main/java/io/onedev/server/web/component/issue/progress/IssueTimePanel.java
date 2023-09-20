package io.onedev.server.web.component.issue.progress;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueWorkManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.timetracking.TimeTrackingManager;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.editablebean.IssueWorkBean;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import javax.annotation.Nullable;

import static io.onedev.server.timetracking.TimeAggregationDirection.BOTH;
import static io.onedev.server.util.DateUtils.formatWorkingPeriod;

abstract class IssueTimePanel extends Panel {

	private Page page;

	public IssueTimePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		page = getPage();
		
		var timeTrackingSetting = OneDev.getInstance(SettingManager.class).getIssueSetting().getTimeTrackingSetting();
		var estimatedTimeField = timeTrackingSetting.getEstimatedTimeField();
		var spentTimeField = timeTrackingSetting.getSpentTimeField();
		var timeAggregationLink = timeTrackingSetting.getTimeAggregationLink();
		
		boolean timeAggregation = getIssue().isAggregatingTime(timeAggregationLink);
		
		if (timeAggregation) {
			var fragment = new Fragment("content", "aggregationFrag", this);

			Integer estimatedTime = (Integer) getIssue().getFieldValue(estimatedTimeField);
			if (estimatedTime == null)
				estimatedTime = 0;
			fragment.add(new Label("estimatedTime", formatWorkingPeriod(estimatedTime)));
			fragment.add(newEstimatedTimeSyncLink("syncEstimatedTime", estimatedTimeField, timeAggregationLink));
			fragment.add(new Label("ownEstimatedTime", formatWorkingPeriod(getIssue().getOwnEstimatedTime())));
			fragment.add(newEstimatedTimeEditLink("editOwnEstimatedTime"));
			
			int aggregatedTime = getTimeTrackingManager().aggregateSourceLinkTimes(getIssue(), estimatedTimeField, timeAggregationLink);
			aggregatedTime += getTimeTrackingManager().aggregateTargetLinkTimes(getIssue(), estimatedTimeField, timeAggregationLink);
			
			fragment.add(new Label("estimatedTimeAggregationLink", timeAggregationLink));
			fragment.add(new Label("aggregatedEstimatedTime", formatWorkingPeriod(aggregatedTime)));

			Integer spentTime = (Integer) getIssue().getFieldValue(spentTimeField);
			if (spentTime == null)
				spentTime = 0;
			fragment.add(new Label("spentTime", formatWorkingPeriod(spentTime)));
			fragment.add(newSpentTimeSyncLink("syncSpentTime", spentTimeField, timeAggregationLink));
			fragment.add(new Label("ownSpentTime", 
					formatWorkingPeriod(getIssue().getWorks().stream().mapToInt(IssueWork::getMinutes).sum())));
			fragment.add(newSpentTimeAddLink("addOwnSpentTime"));
			
			aggregatedTime = getTimeTrackingManager().aggregateSourceLinkTimes(getIssue(), spentTimeField, timeAggregationLink);
			aggregatedTime += getTimeTrackingManager().aggregateTargetLinkTimes(getIssue(), spentTimeField, timeAggregationLink);
			fragment.add(new Label("spentTimeAggregationLink", timeAggregationLink));
			fragment.add(new Label("aggregatedSpentTime", formatWorkingPeriod(aggregatedTime)));
			
			add(fragment);
		} else {
			var fragment = new Fragment("content", "noAggregationFrag", this);
			
			Integer estimatedTime = (Integer) getIssue().getFieldValue(estimatedTimeField);
			if (estimatedTime == null)
				estimatedTime = 0;
			fragment.add(new Label("estimatedTime", formatWorkingPeriod(estimatedTime)));
			fragment.add(newEstimatedTimeSyncLink("syncEstimatedTime", estimatedTimeField, timeAggregationLink));
			fragment.add(newEstimatedTimeEditLink("editEstimatedTime"));

			Integer spentTime = (Integer) getIssue().getFieldValue(spentTimeField);
			if (spentTime == null)
				spentTime = 0;
			fragment.add(new Label("spentTime", formatWorkingPeriod(spentTime)));
			fragment.add(newSpentTimeSyncLink("syncSpentTime", spentTimeField, timeAggregationLink));
			fragment.add(newSpentTimeAddLink("addSpentTime"));
			
			add(fragment);
		}
	}

	private Component newEstimatedTimeSyncLink(String componentId, String estimatedTimeField, 
											   @Nullable String timeAggregationLink) {
		return new AjaxLink<Void>(componentId) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				closeDropdown();
				getTimeTrackingManager().syncEstimatedTime(getIssue(), estimatedTimeField, timeAggregationLink, BOTH);
				notifyObservablesChange(target);
			}
			
		};
	}
	
	private Component newEstimatedTimeEditLink(String componentId) {
		return new AjaxLink<Void>(componentId) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				closeDropdown();
				var bean = new EstimatedTimeEditBean();
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
		}.setVisible(SecurityUtils.canScheduleIssues(getIssue().getProject()));
	}

	private Component newSpentTimeSyncLink(String componentId, String spentTimeField,
											   @Nullable String timeAggregationLink) {
		return new AjaxLink<Void>(componentId) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				closeDropdown();
				getTimeTrackingManager().syncSpentTime(getIssue(), spentTimeField, timeAggregationLink, BOTH);
				notifyObservablesChange(target);
			}

		};
	}
	
	private Component newSpentTimeAddLink(String componentId) {
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
						work.setNote(bean.getNote());
						OneDev.getInstance(IssueWorkManager.class).create(work);
						notifyObservablesChange(target);
						close();
					}
				};
			}

		}.setVisible(SecurityUtils.canLogWorks(getIssue().getProject()));
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

	protected abstract Issue getIssue();
	
	protected abstract void closeDropdown();
	
}
