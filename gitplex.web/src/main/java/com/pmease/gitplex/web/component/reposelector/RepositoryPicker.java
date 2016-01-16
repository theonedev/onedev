package com.pmease.gitplex.web.component.reposelector;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class RepositoryPicker extends DropdownLink {

	private final IModel<Collection<Repository>> reposModel; 
	
	private final IModel<Repository> currentRepoModel;
	
	public RepositoryPicker(String id, IModel<Collection<Repository>> reposModel, IModel<Repository> currentRepoModel) {
		super(id);
	
		this.reposModel = reposModel;
		this.currentRepoModel = currentRepoModel;
	}

	@Override
	protected Component newContent(String id) {
		return new RepositorySelector(id, reposModel, currentRepoModel) {

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
		return Model.of(String.format("<i class='fa fa-ext fa-repo'></i> <span>%s</span> <i class='fa fa-caret-down'></i>", currentRepoModel.getObject().getFQN()));
	}

	@Override
	protected void onDetach() {
		currentRepoModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, Repository repository);
}
