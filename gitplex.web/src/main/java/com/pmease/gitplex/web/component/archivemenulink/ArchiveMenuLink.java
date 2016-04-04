package com.pmease.gitplex.web.component.archivemenulink;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.web.resource.ArchiveResource;
import com.pmease.gitplex.web.resource.ArchiveResourceReference;

@SuppressWarnings("serial")
public abstract class ArchiveMenuLink extends MenuLink {

	private final IModel<Depot> depotModel;
	
	public ArchiveMenuLink(String id, IModel<Depot> depotModel) {
		super(id);
		this.depotModel = depotModel;
	}

	@Override
	protected List<MenuItem> getMenuItems() {
		List<MenuItem> menuItems = new ArrayList<>();
		menuItems.add(new MenuItem() {

			@Override
			public String getIconClass() {
				return null;
			}

			@Override
			public String getLabel() {
				return "zip";
			}

			@Override
			public AbstractLink newLink(String id) {
				return new ResourceLink<Void>(id, new ArchiveResourceReference(), 
						ArchiveResource.paramsOf(depotModel.getObject(), getRevision(), ArchiveResource.FORMAT_ZIP)) {

					@Override
					protected CharSequence getOnClickScript(CharSequence url) {
						return closeBeforeClick(super.getOnClickScript(url));
					}
					
				};
			}

		});
		menuItems.add(new MenuItem() {

			@Override
			public String getIconClass() {
				return null;
			}

			@Override
			public String getLabel() {
				return "tar.gz";
			}

			@Override
			public AbstractLink newLink(String id) {
				return new ResourceLink<Void>(id, new ArchiveResourceReference(), 
						ArchiveResource.paramsOf(depotModel.getObject(), getRevision(), ArchiveResource.FORMAT_TGZ)) {

					@Override
					protected CharSequence getOnClickScript(CharSequence url) {
						return closeBeforeClick(super.getOnClickScript(url));
					}
					
				};
			}

		});		
		return menuItems;
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		super.onDetach();
	}

	protected abstract String getRevision();
}
