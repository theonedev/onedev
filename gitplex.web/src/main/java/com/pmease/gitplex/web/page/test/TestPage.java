package com.pmease.gitplex.web.page.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.web.page.base.BasePage;
import com.pmease.gitplex.web.resource.BlobResource;
import com.pmease.gitplex.web.resource.BlobResourceReference;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new MenuLink("test") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				menuItems.add(new MenuItem() {

					@Override
					public String getIconClass() {
						return "fa fa-check";
					}

					@Override
					public String getLabel() {
						return "Hello";
					}

					@Override
					public AbstractLink newLink(String id) {
						return new Link<Void>(id) {

							@Override
							public void onClick() {
								close();
								System.out.println(getLabel());
							}
							
						};
					}
					
				});
				menuItems.add(null);
				menuItems.add(new MenuItem() {

					@Override
					public String getIconClass() {
						return "fa fa-check";
					}

					@Override
					public String getLabel() {
						return "World";
					}

					@Override
					public AbstractLink newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								close();
								System.out.println(getLabel());
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
						return "Raw";
					}

					@Override
					public AbstractLink newLink(String id) {
						Depot depot = GitPlex.getInstance(DepotManager.class).load(2L);
						BlobIdent blobIdent = new BlobIdent("6.1.x", "system/license.txt", FileMode.REGULAR_FILE.getBits());
						return new ResourceLink<Void>(id, new BlobResourceReference(), BlobResource.paramsOf(depot, blobIdent)) {

							@Override
							public void onClick() {
								super.onClick();
								close();
							}

							@Override
							protected CharSequence getOnClickScript(CharSequence url) {
								return closeBeforeClick(super.getOnClickScript(url));
							}
							
						};
					}
					
				});
				return menuItems;
			}
			
		});
	}

}
