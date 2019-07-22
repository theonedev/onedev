package io.onedev.server.web.component.branch.picker;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.web.component.project.ProjectPicker;
import io.onedev.server.web.model.AffinalProjectsModel;

@SuppressWarnings("serial")
public abstract class AffinalBranchPicker extends Panel {

	private Long projectId;
	
	private String branch;
	
	public AffinalBranchPicker(String id, Long repoId, String branch) {
		super(id);
		
		this.projectId = repoId;
		this.branch = branch;
	}
	
	private void newBranchPicker(@Nullable AjaxRequestTarget target) {
		BranchPicker branchPicker = new BranchPicker("branchPicker", new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				return getProject();
			}
			
		}, branch) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String branch) {
				AffinalBranchPicker.this.onSelect(target, getProject(), branch);
			}

		};
		if (target != null) {
			replace(branchPicker);
			target.add(branchPicker);
		} else {
			add(branchPicker);
		}
	}
	
	private Project getProject() {
		return OneDev.getInstance(Dao.class).load(Project.class, projectId);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ProjectPicker("projectPicker", new AffinalProjectsModel(projectId), projectId) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project) {
				projectId = project.getId();
				branch = project.getDefaultBranch();
				newBranchPicker(target);
				AffinalBranchPicker.this.onSelect(target, project, branch);
			}
			
		});
		newBranchPicker(null);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BranchPickerResourceReference()));
	}

	protected abstract void onSelect(AjaxRequestTarget target, Project project, String branch);
	
}
