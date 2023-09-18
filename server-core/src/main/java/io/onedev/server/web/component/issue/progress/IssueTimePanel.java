package io.onedev.server.web.component.issue.progress;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueWorkManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Day;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.page.base.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.HashMap;

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
		
		Integer estimatedTime = (Integer) getIssue().getFieldValue(timeTrackingSetting.getEstimatedTimeField());
		if (estimatedTime == null)
			add(new Label("estimatedTime", "<i>Not specified</i>").setEscapeModelStrings(false));
		else
			add(new Label("estimatedTime", DateUtils.formatWorkingPeriod(estimatedTime)));			
		
		add(new AjaxLink<Void>("editEstimation") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				closeDropdown();
				var bean = new EstimationTimeEditBean();
				if (estimatedTime != null)
					bean.setEstimationTime(estimatedTime);
				new BeanEditModalPanel<>(target, bean) {

					@Override
					protected void onSave(AjaxRequestTarget target, EstimationTimeEditBean bean) {
						var fieldValues = new HashMap<String, Object>();
						fieldValues.put(timeTrackingSetting.getEstimatedTimeField(), bean.getEstimationTime());
						getIssueChangeManager().changeFields(getIssue(), fieldValues);
						notifyObservablesChange(target);
						close();
					}
				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canScheduleIssues(getIssue().getProject()));
			}
		});

		Integer spentTime = (Integer) getIssue().getFieldValue(timeTrackingSetting.getSpentTimeField());
		if (spentTime == null)
			add(new Label("spentTime", "<i>Not specified</i>").setEscapeModelStrings(false));
		else
			add(new Label("spentTime", DateUtils.formatWorkingPeriod(spentTime)));

		add(new AjaxLink<Void>("addSpent") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				closeDropdown();
				var bean = new IssueWorkEditBean();
				new BeanEditModalPanel<>(target, bean) {

					@Override
					protected void onSave(AjaxRequestTarget target, IssueWorkEditBean bean) {
						IssueWork work = new IssueWork();
						work.setIssue(getIssue());
						work.setUser(SecurityUtils.getUser());
						work.setMinutes(bean.getSpentTime());
						work.setDay(new Day(bean.getStartAt()).getValue());
						work.setNote(bean.getNote());
						OneDev.getInstance(IssueWorkManager.class).create(work);
						notifyObservablesChange(target);
						close();
					}
				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canLogWorks(getIssue().getProject()));
			}
		});
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
