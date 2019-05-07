package io.onedev.server.web.component.build.status;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
public class OverallStatusPanel extends GenericPanel<List<Build>> {

	public OverallStatusPanel(String id, IModel<List<Build>> model) {
		super(id, model);
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
				return new StatusListPanel(id, OverallStatusPanel.this.getModel());
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				Collection<Build> builds = OverallStatusPanel.this.getModelObject();
				String cssClass = "fa fa-fw overall-build-status build-status build-status-";
				String title = "";
				
				for (Build.Status status: Build.Status.values()) {
					if (hasStatus(builds, status)) {
						cssClass += status.name().toLowerCase();
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
	
}
