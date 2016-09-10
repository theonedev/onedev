package com.pmease.gitplex.web.page.layout;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.web.component.accountchoice.AccountSingleChoice;
import com.pmease.gitplex.web.component.accountchoice.AdministrativeAccountChoiceProvider;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;

@SuppressWarnings("serial")
public class CreateDepotPage extends LayoutPage {

	private Long ownerId;
	
	public CreateDepotPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Depot depot = new Depot();
		
		BeanEditor<?> editor = BeanContext.editBean("editor", depot);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Account owner = getOwner();
				depot.setAccount(owner);
				DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
				Depot depotWithSameName = depotManager.find(owner, depot.getName());
				if (depotWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another repository in account '" + owner.getDisplayName() + "'");
				} else {
					depotManager.save(depot, null, null);
					Session.get().success("New repository created");
					setResponsePage(DepotFilePage.class, DepotFilePage.paramsOf(depot));
				}
			}
			
		};
		form.add(editor);
		
		IModel<Account> choiceModel = new IModel<Account>() {

			@Override
			public void detach() {
			}

			@Override
			public Account getObject() {
				return getOwner();
			}

			@Override
			public void setObject(Account object) {
				ownerId = object.getId();
			}
			
		};
		form.add(new AccountSingleChoice("owner", choiceModel, new AdministrativeAccountChoiceProvider()).setRequired(true));
		
		add(form);
	}

	private Account getOwner() {
		if (ownerId == null)
			return getLoginUser();
		else
			return GitPlex.getInstance(AccountManager.class).load(ownerId);
	}
	
}
