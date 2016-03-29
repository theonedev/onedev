package com.pmease.gitplex.web.component.privilegesource;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.depotaccess.DepotAccess;
import com.pmease.gitplex.web.depotaccess.PrivilegeSource;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;

@SuppressWarnings("serial")
public class PrivilegeSourcePanel extends Panel {

	private final IModel<Account> userModel;
	
	private final IModel<Depot> depotModel;
	
	public PrivilegeSourcePanel(String id, IModel<Account> userModel, IModel<Depot> depotModel) {
		super(id);
		
		this.userModel = userModel;
		this.depotModel = depotModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AccountLink("userLink", userModel.getObject()));
		
		DepotAccess access = new DepotAccess(userModel.getObject(), depotModel.getObject());

		add(new Label("privilege", access.getGreatestPrivilege()));
		
		Link<Void> depotLink = new BookmarkablePageLink<Void>("depotLink", 
				DepotFilePage.class, DepotFilePage.paramsOf(depotModel.getObject()));
		depotLink.add(new Label("name", depotModel.getObject().getName()));
		add(depotLink);
		
		RepeatingView sourcesView = new RepeatingView("sources");
		for (PrivilegeSource source: access.getPrivilegeSources()) {
			if (source.getPrivilege() == access.getGreatestPrivilege()) {
				sourcesView.add(source.render(sourcesView.newChildId()));
			}
		}
		add(sourcesView);
	}
	
	@Override
	protected void onDetach() {
		userModel.detach();
		depotModel.detach();
		
		super.onDetach();
	}
	
}
