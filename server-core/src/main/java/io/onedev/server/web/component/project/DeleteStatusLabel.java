package io.onedev.server.web.component.project;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.behavior.ChangeObserver;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.LoadableDetachableModel;

import javax.annotation.Nullable;
import java.util.Collection;

public class DeleteStatusLabel extends WebComponent {
	
	private final Long projectId;
	
	public DeleteStatusLabel(String id, Long projectId) {
		super(id);
		this.projectId = projectId;
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
		String label;
		var project = getProject();
		if (project != null) {
			if (project.isPendingDelete())
				label = "(pending delete)";
			else 
				label = "";
		} else {
			label = "(deleted)";
		}
		replaceComponentTagBody(markupStream, openTag, label);
	}

	@Nullable
	private ProjectFacade getProject() {
		return OneDev.getInstance(ProjectManager.class).findFacadeById(projectId);	
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ChangeObserver() {
			@Override
			protected Collection<String> findObservables() {
				return Sets.newHashSet(Project.getDeleteChangeObservable(projectId));
			}
			
		});
		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				var project = getProject();
				if (project != null && !project.isPendingDelete())
					return "d-none";
				else 
					return "";
			}
		}));
	}
}
