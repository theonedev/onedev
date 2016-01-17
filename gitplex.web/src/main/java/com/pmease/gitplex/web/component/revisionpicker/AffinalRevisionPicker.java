package com.pmease.gitplex.web.component.revisionpicker;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.repopicker.RepositoryPicker;
import com.pmease.gitplex.web.model.AffinalRepositoriesModel;

@SuppressWarnings("serial")
public abstract class AffinalRevisionPicker extends Panel {

	private Long repoId;
	
	private String revision;
	
	public AffinalRevisionPicker(String id, Long repoId, String revision) {
		super(id);
		
		this.repoId = repoId;
		this.revision = revision;
	}
	
	private void newRevisionPicker(@Nullable AjaxRequestTarget target) {
		RevisionPicker revisionPicker = new RevisionPicker("revisionPicker", new LoadableDetachableModel<Repository>() {

			@Override
			protected Repository load() {
				return getRepository();
			}
			
		}, revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				AffinalRevisionPicker.this.onSelect(target, getRepository(), revision);
			}

		};
		if (target != null) {
			replace(revisionPicker);
			target.add(revisionPicker);
		} else {
			add(revisionPicker);
		}
	}
	
	private Repository getRepository() {
		return GitPlex.getInstance(Dao.class).load(Repository.class, repoId);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new RepositoryPicker("repositoryPicker", new AffinalRepositoriesModel(repoId), repoId) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Repository repository) {
				repoId = repository.getId();
				revision = repository.getDefaultBranch();
				newRevisionPicker(target);
				AffinalRevisionPicker.this.onSelect(target, repository, revision);
			}
			
		});
		newRevisionPicker(null);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				AffinalRevisionPicker.class, "revision-picker.css")));
	}

	protected abstract void onSelect(AjaxRequestTarget target, Repository repository, String revision);
	
}
