package io.onedev.server.web.component.build.status;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
public abstract class CommitStatusPanel extends GenericPanel<Collection<Build>> {

	public CommitStatusPanel(String id) {
		super(id);
		setModel(new LoadableDetachableModel<Collection<Build>>() {

			@Override
			protected Collection<Build> load() {
				return OneDev.getInstance(BuildManager.class).query(getProject(), getCommitId().name());
			}
			
		});		
	}

	private boolean hasStatus(Collection<Build> builds, Build.Status status) {
		for (Build build: builds) {
			if (build.getStatus() == status)
				return true;
		}
		return false;
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PageDataChanged && isVisibleInHierarchy()) {
			PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
			pageDataChanged.getHandler().add(this);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new DropdownLink("status") {

			@Override
			protected Component newContent(String id, FloatingPanel floating) {
				return new StatusListPanel(id, CommitStatusPanel.this.getModel());
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				Collection<Build> builds = CommitStatusPanel.this.getModelObject();
				String cssClass = "fa fa-fw overall-build-status build-status build-status-";
				String title = "";
				
				for (Build.Status status: Build.Status.values()) {
					if (hasStatus(builds, status)) {
						cssClass += status.name().toLowerCase();
						if (status == Status.RUNNING)
							cssClass += " fa-spin";
						if (status != Status.SUCCESSFUL)
							title = "Some builds are "; 
						else
							title = "Builds are "; 
						title += status.getDisplayName().toLowerCase() + ", click for details";
						break;
					}
				}
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
				handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				return Lists.newArrayList("commit-status:" + getCommitId().name());
			}
			
		});
		
		setOutputMarkupPlaceholderTag(true);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!getModelObject().isEmpty());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildStatusCssResourceReference()));
	}
	
	protected abstract Project getProject();
	
	protected abstract ObjectId getCommitId();
	
}
