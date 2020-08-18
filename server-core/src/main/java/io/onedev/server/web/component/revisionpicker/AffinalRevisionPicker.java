package io.onedev.server.web.component.revisionpicker;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.project.ProjectPicker;

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
		
		add(new ProjectPicker("projectPicker", new LoadableDetachableModel<Collection<Project>>() {

			@Override
			protected Collection<Project> load() {
				Project project = OneDev.getInstance(Dao.class).load(Project.class, projectId);
				List<Project> affinals = project.getForkRoot().getForkChildren();
				affinals.add(0, project.getForkRoot());
				for (Iterator<Project> it = affinals.iterator(); it.hasNext();) {
					if (!SecurityUtils.canReadCode(it.next()))
						it.remove();
				}
				return affinals;
			}
			
		}) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project) {
				projectId = project.getId();
				revision = project.getDefaultBranch();
				newRevisionPicker(target);
				AffinalRevisionPicker.this.onSelect(target, project, revision);
			}

			@Override
			protected Project getCurrent() {
				return OneDev.getInstance(ProjectManager.class).load(projectId);
			}
			
		});
		newRevisionPicker(null);
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Project project, String revision);
	
}
