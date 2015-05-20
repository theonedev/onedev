package com.pmease.gitplex.web.page.account.repositories;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class NewAccountRepoPage extends AccountPage {

	private final Repository repository;
	
	public NewAccountRepoPage(Repository repository) {
		super(paramsOf(repository.getOwner()));
		
		this.repository = repository;
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final BeanEditor<?> editor = BeanContext.editBean("editor", repository);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				RepositoryManager repositoryManager = GitPlex.getInstance(RepositoryManager.class);
				Repository repoWithSameName = repositoryManager.findBy(repository.getOwner(), repository.getName());
				if (repoWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another repository in this account.");
				} else {
					repositoryManager.save(repository);
					Session.get().success("New repository created");
					setResponsePage(AccountReposPage.class, paramsOf(getAccount()));
				}
			}
			
		};
		form.add(editor);
		
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				setResponsePage(AccountReposPage.class, paramsOf(getAccount()));
			}
			
		});
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(AccountReposPage.class, "account-repos.css")));
	}

}
