package com.pmease.gitplex.web.page.account.depots;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;

@SuppressWarnings("serial")
public class NewDepotPage extends AccountLayoutPage {

	private final Depot depot;
	
	public NewDepotPage(Depot depot) {
		super(paramsOf(depot.getOwner()));
		
		this.depot = depot;
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final BeanEditor<?> editor = BeanContext.editBean("editor", depot);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
				Depot repoWithSameName = depotManager.findBy(depot.getOwner(), depot.getName());
				if (repoWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another repository in this account.");
				} else {
					depotManager.save(depot, null, null);
					Session.get().success("New repository created");
					setResponsePage(DepotListPage.class, paramsOf(getAccount()));
				}
			}
			
		};
		form.add(editor);
		
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				setResponsePage(DepotListPage.class, paramsOf(getAccount()));
			}
			
		});
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				NewDepotPage.class, "depot-list.css")));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		setResponsePage(DepotListPage.class, paramsOf(account));
	}

}
