package io.onedev.server.web.page.project.setting.workspacespec;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

public class WorkspaceSpecsPage extends ProjectSettingPage {

	private WebMarkupContainer container;

	public WorkspaceSpecsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		container = new WebMarkupContainer("workspaceSpecSetting");
		container.setOutputMarkupId(true);
		add(container);
		container.add(new ListView<WorkspaceSpec>("specs", new AbstractReadOnlyModel<List<WorkspaceSpec>>() {

			@Override
			public List<WorkspaceSpec> getObject() {
				return getProject().getWorkspaceSpecs();
			}
		}) {

			@Override
			protected void populateItem(final ListItem<WorkspaceSpec> item) {
				item.add(new WorkspaceSpecPanel("spec", item.getModelObject()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						var spec = getProject().getWorkspaceSpecs().remove(item.getIndex());
						var oldAuditContent = VersionedXmlDoc.fromBean(spec).toXML();
						getProjectService().update(getProject());
						auditService.audit(getProject(), "deleted workspace spec \"" + spec.getName() + "\"", oldAuditContent, null);
						target.add(container);
					}

					@Override
					protected String onSave(AjaxRequestTarget target, WorkspaceSpec spec) {
						for (int i = 0; i < getProject().getWorkspaceSpecs().size(); i++) {
							if (i != item.getIndex() && getProject().getWorkspaceSpecs().get(i).getName().equals(spec.getName()))
								return _T("Another workspace spec with the same name already exists");
						}
						var oldSpec = getProject().getWorkspaceSpecs().set(item.getIndex(), spec);
						var oldAuditContent = VersionedXmlDoc.fromBean(oldSpec).toXML();
						var newAuditContent = VersionedXmlDoc.fromBean(spec).toXML();
						getProjectService().update(getProject());
						auditService.audit(getProject(), "changed workspace spec \"" + spec.getName() + "\"", oldAuditContent, newAuditContent);
						target.add(container);
						return null;
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
				List<WorkspaceSpec> specs = getProject().getWorkspaceSpecs();
				var oldAuditContent = VersionedXmlDoc.fromBean(specs).toXML();
				CollectionUtils.move(specs, from.getItemIndex(), to.getItemIndex());
				var newAuditContent = VersionedXmlDoc.fromBean(specs).toXML();
				getProjectService().update(getProject());
				auditService.audit(getProject(), "reordered workspace specs", oldAuditContent, newAuditContent);
				target.add(container);
			}

		}.items("li.spec").handle(".card-header"));

		container.add(newAddNewLinksFrag());
	}

	private void newWorkspaceSpecEditPanel(AjaxRequestTarget target, WorkspaceSpec spec) {
		Fragment fragment = new Fragment("newSpec", "editNewFrag", getPage());
		fragment.setOutputMarkupId(true);

		fragment.add(new WorkspaceSpecEditPanel("editor", spec) {

			@Override
			protected String onSave(AjaxRequestTarget target, WorkspaceSpec spec) {
				for (WorkspaceSpec existing : getProject().getWorkspaceSpecs()) {
					if (existing.getName().equals(spec.getName()))
						return _T("Another workspace spec with the same name already exists");
				}
				getProject().getWorkspaceSpecs().add(spec);
				var newAuditContent = VersionedXmlDoc.fromBean(spec).toXML();
				getProjectService().update(getProject());
				auditService.audit(getProject(), "created workspace spec \"" + spec.getName() + "\"", null, newAuditContent);
				container.replace(newAddNewLinksFrag());
				target.add(container);
				return null;
			}

			@Override
			protected void onCancel(AjaxRequestTarget target) {
				Component newAddNewFrag = newAddNewLinksFrag();
				container.replace(newAddNewFrag);
				target.add(newAddNewFrag);
			}

		});
		container.replace(fragment);
		target.add(fragment);
	}

	private Component newAddNewLinksFrag() {
		Fragment fragment = new Fragment("newSpec", "addNewLinksFrag", this);
		fragment.add(new AjaxLink<Void>("createNew") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				newWorkspaceSpecEditPanel(target, new WorkspaceSpec());
			}

		});
		fragment.add(new AjaxLink<Void>("createFromTemplate") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				new BeanEditModalPanel<WorkspaceSpecTemplateBean>(target, new WorkspaceSpecTemplateBean()) {

				@Override
					protected String onSave(AjaxRequestTarget target, WorkspaceSpecTemplateBean bean) {
						var spec = bean.getTemplate().createWorkspaceSpec();
						for (WorkspaceSpec existing : getProject().getWorkspaceSpecs()) {
							if (existing.getName().equals(spec.getName()))
								return _T("Another workspace spec with the same name already exists");
						}
						close();
						getProject().getWorkspaceSpecs().add(spec);
						var newAuditContent = VersionedXmlDoc.fromBean(spec).toXML();
						getProjectService().update(getProject());
						auditService.audit(getProject(), "created workspace spec \"" + spec.getName() + "\"", null, newAuditContent);
						target.add(container);
						return null;
					}
					
				};
			}

		});		
		fragment.setOutputMarkupId(true);
		return fragment;
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>" + _T("Workspace Specs") + "</span>").setEscapeModelStrings(false);
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (SecurityUtils.canManageProject(project))
			return new ViewStateAwarePageLink<Void>(componentId, WorkspaceSpecsPage.class, paramsOf(project.getId()));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectPage.paramsOf(project.getId()));
	}

}
