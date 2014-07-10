package com.pmease.gitop.web.component.branch;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class BranchLink extends Panel {

	public BranchLink(String id, IModel<Branch> model) {
		super(id, model);
	}

	private Branch getBranch() {
		return (Branch) getDefaultModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<Void> link = new Link<Void>("link") {

			@Override
			public void onClick() {
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(SecurityUtils.getSubject().isPermitted(
						ObjectPermission.ofRepositoryRead(getBranch().getRepository())));
			}
			
		};
		add(link);
		
		link.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				RepositoryPage page = (RepositoryPage) getPage();
				if (page.getRepository().equals(getBranch().getRepository())) {
					return getBranch().getName();
				} else {
					return getBranch().getRepository().toString() + ":" + getBranch().getName();
				}
			}
			
		}));
		
	}

}
