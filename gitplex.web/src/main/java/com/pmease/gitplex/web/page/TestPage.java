package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.repository.RepositoryChoice;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private Long repoId;
	
	public TestPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.add(new RepositoryChoice("repository", new IModel<Repository>() {

			@Override
			public void detach() {
			}

			@Override
			public Repository getObject() {
				if (repoId != null)
					return GitPlex.getInstance(Dao.class).load(Repository.class, repoId);
				else
					return null;
			}

			@Override
			public void setObject(Repository object) {
				repoId = object.getId();
			}
			
		}, null));
		
		add(form);
	}

}
