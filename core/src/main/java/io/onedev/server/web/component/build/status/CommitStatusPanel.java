package io.onedev.server.web.component.build.status;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.model.EntityModel;

@SuppressWarnings("serial")
public class CommitStatusPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final ObjectId commitId;
	
	private final IModel<Status> statusModel;
	
	public CommitStatusPanel(String id, Project project, ObjectId commitId) {
		super(id);
		
		this.projectModel = new EntityModel<Project>(project);
		this.commitId = commitId;
		
		statusModel = new LoadableDetachableModel<Status>() {

			@Override
			protected Status load() {
				return Status.getOverallStatus(getProject().getCommitStatus(commitId).values());
			}
			
		};		
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}

	private BuildManager getBuildManager() {
		return OneDev.getInstance(BuildManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new DropdownLink("status") {

			@Override
			protected Component newContent(String id, FloatingPanel floating) {
				return new StatusListPanel(id, new LoadableDetachableModel<Collection<Build>>() {

					@Override
					protected Collection<Build> load() {
						return getBuildManager().query(getProject(), commitId);
					}
					
				});
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				String cssClass = "fa fa-fw overall-build-status build-status build-status-";
				String title = "";

				Status status = Preconditions.checkNotNull(statusModel.getObject());
				cssClass += status.name().toLowerCase();
				if (status == Status.RUNNING)
					cssClass += " fa-spin";
				if (status != Status.SUCCESSFUL)
					title = "Some builds are "; 
				else
					title = "Builds are "; 
				title += status.getDisplayName().toLowerCase() + ", click for details";
				tag.put("class", cssClass);
				tag.put("title", title);
			}
			
		});
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, String observable) {
				handler.add(component);
			}
			
			@Override
			public void onConnectionOpened(IPartialPageRequestHandler handler) {
				/*
				 *  Do not refresh on connection as otherwise project commit status cache will not take
				 *  effect on commit list page 
				 */
				// handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				return Lists.newArrayList("commit-status:" + getProject().getId() + ":" + commitId.name());
			}
			
		});
		
		setOutputMarkupPlaceholderTag(true);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(statusModel.getObject() != null);
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		statusModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildStatusCssResourceReference()));
	}
	
}
