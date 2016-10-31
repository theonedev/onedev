package com.gitplex.web.page.account.overview;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.manager.DepotManager;
import com.gitplex.core.security.SecurityUtils;
import com.gitplex.web.page.account.AccountLayoutPage;
import com.gitplex.web.page.depot.file.DepotFilePage;
import com.gitplex.commons.wicket.editable.BeanContext;
import com.gitplex.commons.wicket.editable.BeanEditor;
import com.gitplex.commons.wicket.editable.PathSegment;

@SuppressWarnings("serial")
public class NewDepotPage extends AccountLayoutPage {

	public NewDepotPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
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
				
				depot.setAccount(getAccount());
				DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
				Depot depotWithSameName = depotManager.find(getAccount(), depot.getName());
				if (depotWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another repository in this account.");
				} else {
					depotManager.save(depot, null, null);
					Session.get().success("New repository created");
					setResponsePage(DepotFilePage.class, DepotFilePage.paramsOf(depot));
				}
			}
			
		};
		form.add(editor);
		add(form);
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		setResponsePage(AccountOverviewPage.class, AccountOverviewPage.paramsOf(account));
	}

}
