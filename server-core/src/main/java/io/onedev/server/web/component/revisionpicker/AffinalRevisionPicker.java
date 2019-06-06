package io.onedev.server.web.component.revisionpicker;

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
public abstract class AffinalRevisionPicker extends Panel {

	private Long projectId;
	
	private String revision;
	
	public AffinalRevisionPicker(String id, Long repoId, String revision) {
		super(id);
		
		this.projectId = repoId;
		this.revision = revision;
	}
	
	private void newRevisionPicker(@Nullable AjaxRequestTarget target) {
		RevisionPicker revisionPicker = new RevisionPicker("revisionPicker", new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				return getProject();
			}
			
		}, revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				AffinalRevisionPicker.this.onSelect(target, getProject(), revision);
			}

		};
		if (target != null) {
			replace(revisionPicker);
			target.add(revisionPicker);
		} else {
			add(revisionPicker);
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
				revision = project.getDefaultBranch();
				newRevisionPicker(target);
				AffinalRevisionPicker.this.onSelect(target, project, revision);
			}
			
		});
		newRevisionPicker(null);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new RevisionPickerResourceReference()));
	}

	protected abstract void onSelect(AjaxRequestTarget target, Project project, String revision);
	
}
