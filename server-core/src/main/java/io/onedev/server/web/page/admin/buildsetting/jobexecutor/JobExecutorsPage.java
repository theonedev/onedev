package io.onedev.server.web.page.admin.buildsetting.jobexecutor;

import static io.onedev.server.web.translation.Translation._T;

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
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.page.admin.AdministrationPage;

public class JobExecutorsPage extends AdministrationPage {

	private List<JobExecutor> executors;
	
	private WebMarkupContainer container;
	
	public JobExecutorsPage(PageParameters params) {
		super(params);
		executors = getSettingService().getJobExecutors();
	}

	private SettingService getSettingService() {
		return OneDev.getInstance(SettingService.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		container = new WebMarkupContainer("jobExecutorSetting");
		container.setOutputMarkupId(true);
		add(container);
		container.add(new ListView<>("executors", new AbstractReadOnlyModel<List<JobExecutor>>() {

			@Override
			public List<JobExecutor> getObject() {
				return getSettingService().getJobExecutors();
			}

		}) {

			@Override
			protected void populateItem(ListItem<JobExecutor> item) {
				var oldAuditContent = VersionedXmlDoc.fromBean(item.getModelObject()).toXML();
				item.add(new JobExecutorPanel("executor", executors, item.getIndex()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						var executor = executors.remove(item.getIndex());
						var oldAuditContent = VersionedXmlDoc.fromBean(executor).toXML();
						getSettingService().saveJobExecutors(executors);
						auditService.audit(null, "deleted job executor \"" + executor.getName() + "\"", oldAuditContent, null);
						target.add(container);
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						var executor = executors.get(item.getIndex());
						var newAuditContent = VersionedXmlDoc.fromBean(executor).toXML();
						auditService.audit(null, "changed job executor \"" + executor.getName() + "\"", oldAuditContent, newAuditContent);
						getSettingService().saveJobExecutors(executors);
						target.add(container);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						target.add(container);
					}

				});
			}

		});
		
		container.add(new SortBehavior() {
			
			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				var oldAuditContent = VersionedXmlDoc.fromBean(executors).toXML();
				CollectionUtils.move(executors, from.getItemIndex(), to.getItemIndex());
				var newAuditContent = VersionedXmlDoc.fromBean(executors).toXML();
				getSettingService().saveJobExecutors(executors);
				auditService.audit(null, "changed order of job executors", oldAuditContent, newAuditContent);			
				target.add(container);
			}
			
		}.items("li.executor").handle(".card-header"));
		
		container.add(newAddNewFrag());
		container.add(new WebMarkupContainer("noExecutors") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getSettingService().getJobExecutors().isEmpty());
			}
		});
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
						getSettingService().saveJobExecutors(executors);
						container.replace(newAddNewFrag());
						var executor = executors.get(executors.size() - 1);
						var newAuditContent = VersionedXmlDoc.fromBean(executor).toXML();
						auditService.audit(null, "added job executor \"" + executor.getName() + "\"", null, newAuditContent);
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
		return new Label(componentId, _T("Job Executors"));
	}

}
