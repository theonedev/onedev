package com.pmease.gitplex.web.page.repository.setting.general;

import java.io.Serializable;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteRepoModal;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteRepoModalBehavior;
import com.pmease.gitplex.web.page.account.repositories.AccountReposPage;
import com.pmease.gitplex.web.page.repository.setting.RepoSettingPage;

@SuppressWarnings("serial")
public class GeneralSettingPage extends RepoSettingPage {

	private BeanEditor<?> editor;
	
	public GeneralSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		editor = BeanContext.editModel("editor", new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getRepository();
			}

			@Override
			public void setObject(Serializable object) {
				editor.getBeanDescriptor().copyProperties(object, getRepository());
			}
			
		});
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Repository repository = getRepository();
				RepositoryManager repositoryManager = GitPlex.getInstance(RepositoryManager.class);
				Repository repoWithSameName = repositoryManager.findBy(getAccount(), repository.getName());
				if (repoWithSameName != null && !repoWithSameName.equals(repository)) {
					String errorMessage = "This name has already been used by another repository in account " 
							+ getAccount().getName() + "."; 
					editor.getErrorContext(new PathSegment.Property("name")).addError(errorMessage);
				} else {
					repositoryManager.save(repository);
					Session.get().success("General setting has been updated");
					setResponsePage(GeneralSettingPage.class, paramsOf(repository));
					backToPrevPage();
				}
			}
			
		};
		form.add(editor);
		form.add(new SubmitLink("save"));

		ConfirmDeleteRepoModal confirmDeleteDlg = new ConfirmDeleteRepoModal("confirmDeleteDlg") {

			@Override
			protected void onDeleted(AjaxRequestTarget target) {
				setResponsePage(AccountReposPage.class, paramsOf(getAccount()));
			}
			
		};
		form.add(confirmDeleteDlg);
		form.add(new WebMarkupContainer("delete").add(new ConfirmDeleteRepoModalBehavior(confirmDeleteDlg) {

			@Override
			protected Repository getRepository() {
				return GeneralSettingPage.this.getRepository();
			}
			
		}));
		
		add(form);
	}

	@Override
	protected String getPageTitle() {
		return "General Setting - " + getRepository();
	}

}
