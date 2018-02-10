package com.turbodev.server.web.component.link;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.IModel;

import com.turbodev.server.model.Project;
import com.turbodev.server.web.component.floating.FloatingPanel;
import com.turbodev.server.web.component.menu.MenuItem;
import com.turbodev.server.web.component.menu.MenuLink;
import com.turbodev.server.web.util.resource.ArchiveResource;
import com.turbodev.server.web.util.resource.ArchiveResourceReference;

@SuppressWarnings("serial")
public abstract class ArchiveMenuLink extends MenuLink {

	private final IModel<Project> projectModel;
	
	public ArchiveMenuLink(String id, IModel<Project> projectModel) {
		super(id);
		this.projectModel = projectModel;
	}

	@Override
	protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
		List<MenuItem> menuItems = new ArrayList<>();
		menuItems.add(new MenuItem() {

			@Override
			public String getLabel() {
				return "zip";
			}

			@Override
			public AbstractLink newLink(String id) {
				return new ResourceLink<Void>(id, new ArchiveResourceReference(), 
						ArchiveResource.paramsOf(projectModel.getObject(), getRevision(), ArchiveResource.FORMAT_ZIP)) {

					@Override
					protected CharSequence getOnClickScript(CharSequence url) {
						return closeBeforeClick(super.getOnClickScript(url));
					}
					
				};
			}

		});
		menuItems.add(new MenuItem() {

			@Override
			public String getLabel() {
				return "tar.gz";
			}

			@Override
			public AbstractLink newLink(String id) {
				return new ResourceLink<Void>(id, new ArchiveResourceReference(), 
						ArchiveResource.paramsOf(projectModel.getObject(), getRevision(), ArchiveResource.FORMAT_TGZ)) {

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
		projectModel.detach();
		super.onDetach();
	}

	protected abstract String getRevision();
}
