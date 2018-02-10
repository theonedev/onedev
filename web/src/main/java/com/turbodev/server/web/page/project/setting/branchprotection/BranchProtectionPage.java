package com.turbodev.server.web.page.project.setting.branchprotection;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.ProjectManager;
import com.turbodev.server.model.support.BranchProtection;
import com.turbodev.server.web.behavior.sortable.SortBehavior;
import com.turbodev.server.web.behavior.sortable.SortPosition;
import com.turbodev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class BranchProtectionPage extends ProjectSettingPage {

	private WebMarkupContainer container;
	
	public BranchProtectionPage(PageParameters params) {
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
						getProject().getBranchProtections().remove(item.getIndex());
						TurboDev.getInstance(ProjectManager.class).save(getProject(), null);
						target.add(container);
					}

					@Override
					protected void onSave(AjaxRequestTarget target, BranchProtection protection) {
						getProject().getBranchProtections().set(item.getIndex(), protection);
						TurboDev.getInstance(ProjectManager.class).save(getProject(), null);
					}
					
				});
			}
			
		});
		
		container.add(new SortBehavior() {
			
			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				List<BranchProtection> protections = getProject().getBranchProtections();
				BranchProtection protection = protections.get(from.getItemIndex());
				protections.set(from.getItemIndex(), protections.set(to.getItemIndex(), protection));
				TurboDev.getInstance(ProjectManager.class).save(getProject(), null);
				
				target.add(container);
			}
			
		}.handle(".drag-handle").items("li.protection"));
		
		container.add(newAddNewFrag());
	}

	private Component newAddNewFrag() {
		Fragment fragment = new Fragment("newProtection", "addNewLinkFrag", this); 
		fragment.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("newProtection", "editNewFrag", getPage());
				fragment.setOutputMarkupId(true);
				fragment.add(new BranchProtectionEditor("editor", new BranchProtection()) {

					@Override
					protected void onSave(AjaxRequestTarget target, BranchProtection protection) {
						getProject().getBranchProtections().add(protection);
						TurboDev.getInstance(ProjectManager.class).save(getProject(), null);
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
	
}
