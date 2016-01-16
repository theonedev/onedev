package com.pmease.gitplex.web.component.repopicker;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class RepositoryPicker extends DropdownLink {

	private final IModel<List<Repository>> reposModel; 
	
	private Long currentRepoId;
	
	public RepositoryPicker(String id, IModel<List<Repository>> reposModel, Long currentRepoId) {
		super(id);
	
		this.reposModel = reposModel;
		this.currentRepoId = currentRepoId;
	}

	@Override
	protected Component newContent(String id) {
		return new RepositorySelector(id, reposModel, currentRepoId) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Repository repository) {
				close(target);
				target.add(RepositoryPicker.this);
				RepositoryPicker.this.onSelect(target, repository);
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setEscapeModelStrings(false);
	}

	@Override
	public IModel<?> getBody() {
		Repository currentRepo = GitPlex.getInstance(Dao.class).load(Repository.class, currentRepoId);
		return Model.of(String.format("<i class='fa fa-ext fa-repo'></i> <span>%s</span> <i class='fa fa-caret-down'></i>", currentRepo.getFQN()));
	}

	@Override
	protected void onDetach() {
		reposModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, Repository repository);
}
