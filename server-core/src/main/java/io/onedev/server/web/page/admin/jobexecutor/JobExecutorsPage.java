package io.onedev.server.web.page.admin.jobexecutor;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class JobExecutorsPage extends AdministrationPage {

	private List<JobExecutor> executors;
	
	private WebMarkupContainer container;
	
	public JobExecutorsPage(PageParameters params) {
		super(params);
		executors = getSettingManager().getJobExecutors();
	}

	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		container = new WebMarkupContainer("jobExecutorSetting");
		container.setOutputMarkupId(true);
		add(container);
		container.add(new ListView<JobExecutor>("executors", new AbstractReadOnlyModel<List<JobExecutor>>() {

			@Override
			public List<JobExecutor> getObject() {
				return getSettingManager().getJobExecutors();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<JobExecutor> item) {
				item.add(new JobExecutorPanel("executor", executors, item.getIndex()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						executors.remove(item.getIndex());
						getSettingManager().saveJobExecutors(executors);
						target.add(container);
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						getSettingManager().saveJobExecutors(executors);
					}
					
				});
			}
			
		});
		
		container.add(new SortBehavior() {
			
			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				JobExecutor executor = executors.get(from.getItemIndex());
				executors.set(from.getItemIndex(), executors.set(to.getItemIndex(), executor));
				getSettingManager().saveJobExecutors(executors);
				
				target.add(container);
			}
			
		}.items("li.executor").handle(".card-header"));
		
		container.add(newAddNewFrag());
	}

	private Component newAddNewFrag() {
		Fragment fragment = new Fragment("newExecutor", "addNewLinkFrag", this); 
		fragment.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("newExecutor", "editNewFrag", getPage());
				fragment.setOutputMarkupId(true);
				fragment.add(new JobExecutorEditPanel("editor", executors, -1) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						getSettingManager().saveJobExecutors(executors);
						container.replace(newAddNewFrag());
						target.add(container);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						Component newAddNewFrag = newAddNewFrag();
						container.replace(newAddNewFrag);
						target.add(newAddNewFrag);
					}
					
				});
				container.replace(fragment);
				target.add(fragment);
			}
			
		});
		fragment.setOutputMarkupId(true);
		return fragment;
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Job Executors");
	}

}
