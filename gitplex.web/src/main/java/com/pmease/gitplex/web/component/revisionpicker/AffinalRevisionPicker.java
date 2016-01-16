package com.pmease.gitplex.web.component.revisionpicker;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.repochoice.AffinalRepositoryChoice;

@SuppressWarnings("serial")
public abstract class AffinalRevisionPicker extends Panel {

	private final IModel<Repository> repoModel;
	
	private String revision;
	
	public AffinalRevisionPicker(String id, IModel<Repository> repoModel, String revision) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = revision;
	}
	
	private void newRevisionPicker(@Nullable AjaxRequestTarget target) {
		RevisionPicker revisionPicker = new RevisionPicker("revisionPicker", repoModel, revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				AffinalRevisionPicker.this.onSelect(target, revision);
			}

		};
		if (target != null) {
			replace(revisionPicker);
			target.add(revisionPicker);
		} else {
			add(revisionPicker);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new AffinalRepositoryChoice("repositoryChoice", repoModel, repoModel).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				revision = repoModel.getObject().getDefaultBranch();
				newRevisionPicker(target);
				onSelect(target, revision);
			}

		}));
		
		newRevisionPicker(null);
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, String revision);
	
	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}
	
}
