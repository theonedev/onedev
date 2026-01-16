package io.onedev.server.web.component.job;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.web.component.modal.ModalPanel;

public abstract class BuildOptionModalPanel extends ModalPanel implements InputContext {

	private final List<String> refNames;
	
	private final Serializable paramBean;
	
	public BuildOptionModalPanel(AjaxRequestTarget target, List<String> refNames, Serializable paramBean) {
		super(target);
		this.refNames = refNames;
		this.paramBean = paramBean;
	}
			
	@Override
	protected Component newContent(String id) {
		return new BuildOptionContentPanel(id, refNames, paramBean) {
			
			@Override
			protected void onSave(AjaxRequestTarget target, Collection<String> selectedRefNames, 
					Serializable populatedParamBean) {
				BuildOptionModalPanel.this.onSave(target, selectedRefNames, populatedParamBean);
			}
			
			@Override
			protected void onCancel(AjaxRequestTarget target) {
				close();
				BuildOptionModalPanel.this.onCancel(target);
			}

			@Override
			public ProjectScopedCommit getProjectScopedCommit() {
				return BuildOptionModalPanel.this.getProjectScopedCommit();
			}

		};
	}

	protected abstract void onSave(AjaxRequestTarget target, Collection<String> selectedRefNames, 
			Serializable populatedParamBean);
	
	protected void onCancel(AjaxRequestTarget target) {
	}
	
	protected abstract ProjectScopedCommit getProjectScopedCommit();
	
}
