package io.onedev.server.web.page.admin.workspaceprovisioner;

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
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.page.admin.AdministrationPage;

public class WorkspaceProvisionersPage extends AdministrationPage {

	private List<WorkspaceProvisioner> provisioners;

	private WebMarkupContainer container;

	public WorkspaceProvisionersPage(PageParameters params) {
		super(params);
		provisioners = getSettingService().getWorkspaceProvisioners();
	}

	private SettingService getSettingService() {
		return OneDev.getInstance(SettingService.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		container = new WebMarkupContainer("workspaceProvisionerSetting");
		container.setOutputMarkupId(true);
		add(container);

		container.add(new ListView<>("provisioners", new AbstractReadOnlyModel<List<WorkspaceProvisioner>>() {

			@Override
			public List<WorkspaceProvisioner> getObject() {
				return getSettingService().getWorkspaceProvisioners();
			}

		}) {

			@Override
			protected void populateItem(ListItem<WorkspaceProvisioner> item) {
				var oldAuditContent = VersionedXmlDoc.fromBean(item.getModelObject()).toXML();
				item.add(new WorkspaceProvisionerPanel("provisioner", provisioners, item.getIndex()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						var provisioner = provisioners.remove(item.getIndex());
						var oldAuditContent = VersionedXmlDoc.fromBean(provisioner).toXML();
						getSettingService().saveWorkspaceProvisioners(provisioners);
						auditService.audit(null, "deleted workspace provisioner \"" + provisioner.getName() + "\"", oldAuditContent, null);
						target.add(container);
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						var provisioner = provisioners.get(item.getIndex());
						var newAuditContent = VersionedXmlDoc.fromBean(provisioner).toXML();
						auditService.audit(null, "changed workspace provisioner \"" + provisioner.getName() + "\"", oldAuditContent, newAuditContent);
						getSettingService().saveWorkspaceProvisioners(provisioners);
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
				var oldAuditContent = VersionedXmlDoc.fromBean(provisioners).toXML();
				CollectionUtils.move(provisioners, from.getItemIndex(), to.getItemIndex());
				var newAuditContent = VersionedXmlDoc.fromBean(provisioners).toXML();
				getSettingService().saveWorkspaceProvisioners(provisioners);
				auditService.audit(null, "changed order of workspace provisioners", oldAuditContent, newAuditContent);
				target.add(container);
			}

		}.items("li.provisioner").handle(".card-header"));

		container.add(newAddNewFrag());
		container.add(new WebMarkupContainer("noProvisioners") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getSettingService().getWorkspaceProvisioners().isEmpty());
			}

		});
	}

	private Component newAddNewFrag() {
		Fragment fragment = new Fragment("newProvisioner", "addNewLinkFrag", this);
		fragment.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("newProvisioner", "editNewFrag", getPage());
				fragment.setOutputMarkupId(true);
				fragment.add(new WorkspaceProvisionerEditPanel("editor", provisioners, -1) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						getSettingService().saveWorkspaceProvisioners(provisioners);
						container.replace(newAddNewFrag());
						var provisioner = provisioners.get(provisioners.size() - 1);
						var newAuditContent = VersionedXmlDoc.fromBean(provisioner).toXML();
						auditService.audit(null, "added workspace provisioner \"" + provisioner.getName() + "\"", null, newAuditContent);
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
		return new Label(componentId, _T("Workspace Provisioners"));
	}

}
