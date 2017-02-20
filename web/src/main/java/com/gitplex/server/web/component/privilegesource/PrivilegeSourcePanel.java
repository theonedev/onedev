package com.gitplex.server.web.component.privilegesource;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.web.component.AccountLink;
import com.gitplex.server.web.page.depot.file.DepotFilePage;
import com.gitplex.server.web.util.depotaccess.DepotAccess;
import com.gitplex.server.web.util.depotaccess.PrivilegeSource;

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
