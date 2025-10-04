package io.onedev.server.web.page.project.setting.code.tagprotection;

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
import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

public class TagProtectionsPage extends ProjectSettingPage {

	private WebMarkupContainer container;
	
	public TagProtectionsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		container = new WebMarkupContainer("tagProtectionSetting");
		container.setOutputMarkupId(true);
		add(container);
		container.add(new ListView<>("protections", new AbstractReadOnlyModel<List<TagProtection>>() {

			@Override
			public List<TagProtection> getObject() {
				return getProject().getTagProtections();
			}
		}) {

			@Override
			protected void populateItem(final ListItem<TagProtection> item) {
				item.add(new TagProtectionPanel("protection", item.getModelObject()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						var protection = getProject().getTagProtections().remove(item.getIndex());
						var oldAuditContent = VersionedXmlDoc.fromBean(protection).toXML();
						getProjectService().update(getProject());
						auditService.audit(getProject(), "deleted tag protection rule", oldAuditContent, null);
						target.add(container);
					}

					@Override
					protected void onSave(AjaxRequestTarget target, TagProtection protection) {
						var oldProtection = getProject().getTagProtections().set(item.getIndex(), protection);
						var oldAuditContent = VersionedXmlDoc.fromBean(oldProtection).toXML();
						var newAuditContent = VersionedXmlDoc.fromBean(protection).toXML();
						getProjectService().update(getProject());
						auditService.audit(getProject(), "changed tag protection rule", oldAuditContent, newAuditContent);
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
				List<TagProtection> protections = getProject().getTagProtections();
				var oldAuditContent = VersionedXmlDoc.fromBean(protections).toXML();
				CollectionUtils.move(protections, from.getItemIndex(), to.getItemIndex());
				var newAuditContent = VersionedXmlDoc.fromBean(protections).toXML();
				getProjectService().update(getProject());
				auditService.audit(getProject(), "reordered tag protection rules", oldAuditContent, newAuditContent);
				
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
				fragment.add(new TagProtectionEditPanel("editor", new TagProtection()) {

					@Override
					protected void onSave(AjaxRequestTarget target, TagProtection protection) {
						getProject().getTagProtections().add(protection);
						var newAuditContent = VersionedXmlDoc.fromBean(protection).toXML();
						getProjectService().update(getProject());
						auditService.audit(getProject(), "added tag protection rule", null, newAuditContent);
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
		return new Label(componentId, _T("Tag Protection"));
	}
	
}
