package com.gitplex.server.web.page.depot.moreinfo;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.component.accountchoice.AccountSingleChoice;
import com.gitplex.server.web.component.accountchoice.AdministrativeAccountChoiceProvider;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.BeanEditor;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;

@SuppressWarnings("serial")
abstract class ForkOptionPanel extends Panel {

	private final IModel<Depot> depotModel;
	
	private Long ownerId;
	
	public ForkOptionPanel(String id, IModel<Depot> depotModel) {
		super(id);
		this.depotModel = depotModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Depot depot = new Depot();
		depot.setForkedFrom(getDepot());
		depot.setName(getDepot().getName());
		
		BeanEditor<?> editor = BeanContext.editBean("editor", depot);
		
		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
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
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				Account owner = getOwner();
				depot.setAccount(owner);
				DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
				Depot depotWithSameName = depotManager.find(owner, depot.getName());
				if (depotWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another repository in account '" + owner.getDisplayName() + "'");
					target.add(form);
				} else {
					depotManager.fork(getDepot(), depot);
					Session.get().success("Repository forked");
					setResponsePage(DepotBlobPage.class, DepotBlobPage.paramsOf(depot));
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		add(form);
	}
	
	private Account getOwner() {
		if (ownerId == null)
			return SecurityUtils.getAccount();
		else
			return GitPlex.getInstance(AccountManager.class).load(ownerId);
	}
	
	private Depot getDepot() {
		return depotModel.getObject();
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		super.onDetach();
	}

	protected abstract void onClose(AjaxRequestTarget target);
	
}
