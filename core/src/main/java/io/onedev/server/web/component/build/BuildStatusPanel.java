package io.onedev.server.web.component.build;

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
public class BuildStatusPanel extends GenericPanel<List<Build>> {

	public BuildStatusPanel(String id, IModel<List<Build>> model) {
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

		if (event.getPayload() instanceof PageDataChanged) {
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
				return new BuildDetailPanel(id, BuildStatusPanel.this.getModel());
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				Collection<Build> builds = BuildStatusPanel.this.getModelObject();
				if (hasStatus(builds, Status.ERROR)) {
					tag.put("class", "build-status error fa fa-warning");
					tag.put("title", "Some builds are in error, click for details");
				} else if (hasStatus(builds, Status.FAILURE)) {
					tag.put("class", "build-status failure fa fa-times");
					tag.put("title", "Some builds are failed, click for details");
				} else if (hasStatus(builds, Status.RUNNING)) {
					tag.put("class", "build-status running fa fa-circle");
					tag.put("title", "Some builds are running, click for details");
				} else if (hasStatus(builds, Status.SUCCESS)) {
					tag.put("class", "build-status success fa fa-check");
					tag.put("title", "Builds are successful, click for details");
				}
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
		response.render(CssHeaderItem.forReference(new BuildResourceReference()));
	}
	
}
