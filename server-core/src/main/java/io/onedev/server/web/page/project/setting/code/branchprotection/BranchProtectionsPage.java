package io.onedev.server.web.page.project.setting.code.branchprotection;

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

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

public class BranchProtectionsPage extends ProjectSettingPage {

	private WebMarkupContainer container;
	
	public BranchProtectionsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		container = new WebMarkupContainer("branchProtectionSetting");
		container.setOutputMarkupId(true);
		add(container);
		container.add(new ListView<BranchProtection>("protections", new AbstractReadOnlyModel<List<BranchProtection>>() {

			@Override
			public List<BranchProtection> getObject() {
				return getProject().getBranchProtections();
			}
		}) {

			@Override
			protected void populateItem(final ListItem<BranchProtection> item) {
				item.add(new BranchProtectionPanel("protection", item.getModelObject()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						var protection = getProject().getBranchProtections().remove(item.getIndex());
						var oldAuditContent = VersionedXmlDoc.fromBean(protection).toXML();
						getProjectService().update(getProject());
						auditService.audit(getProject(), "deleted branch protection rule", oldAuditContent, null);
						target.add(container);
					}

					@Override
					protected void onSave(AjaxRequestTarget target, BranchProtection protection) {
						var oldProtection = getProject().getBranchProtections().set(item.getIndex(), protection);
						var oldAuditContent = VersionedXmlDoc.fromBean(oldProtection).toXML();
						var newAuditContent = VersionedXmlDoc.fromBean(protection).toXML();
						getProjectService().update(getProject());
						auditService.audit(getProject(), "changed branch protection rule", oldAuditContent, newAuditContent);
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
				List<BranchProtection> protections = getProject().getBranchProtections();
				var oldAuditContent = VersionedXmlDoc.fromBean(protections).toXML();
				CollectionUtils.move(protections, from.getItemIndex(), to.getItemIndex());
				var newAuditContent = VersionedXmlDoc.fromBean(protections).toXML();
				getProjectService().update(getProject());
				auditService.audit(getProject(), "reordered branch protection rules", oldAuditContent, newAuditContent);
				
				target.add(container);
			}
			
		}.items("li.protection").handle(".card-header"));
		
		container.add(newAddNewFrag());
	}

	private Component newAddNewFrag() {
		Fragment fragment = new Fragment("newProtection", "addNewLinkFrag", this); 
		fragment.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("newProtection", "editNewFrag", getPage());
				fragment.setOutputMarkupId(true);
				fragment.add(new BranchProtectionEditPanel("editor", new BranchProtection()) {

					@Override
					protected void onSave(AjaxRequestTarget target, BranchProtection protection) {
						getProject().getBranchProtections().add(protection);
						var newAuditContent = VersionedXmlDoc.fromBean(protection).toXML();
						getProjectService().update(getProject());
						auditService.audit(getProject(), "created branch protection rule", null, newAuditContent);
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
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Branch Protection"));
	}
	
}
