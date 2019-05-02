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
public class BuildsStatusPanel extends GenericPanel<List<Build>> {

	public BuildsStatusPanel(String id, IModel<List<Build>> model) {
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
				return new BuildDetailPanel(id, BuildsStatusPanel.this.getModel());
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				Collection<Build> builds = BuildsStatusPanel.this.getModelObject();
				String cssClass = "build-status fa fa-fw ";
				String title = "";
				if (hasStatus(builds, Status.IN_ERROR)) {
					cssClass += "fa-warning " + Status.IN_ERROR.name().toLowerCase();
					title = "Some builds are in error, click for details";
				} else if (hasStatus(builds, Status.FAILED)) {
					cssClass += "fa-times " + Status.FAILED.name().toLowerCase();
					title = "Some builds are failed, click for details";
				} else if (hasStatus(builds, Status.CANCELLED)) {
					cssClass += "fa-ban " + Status.CANCELLED.name().toLowerCase();
					title = "Some builds are cancelled, click for details";
				} else if (hasStatus(builds, Status.TIMED_OUT)) {
					cssClass += "fa-clock-o " + Status.TIMED_OUT.name().toLowerCase();
					title = "Some builds timed out, click for details";
				} else if (hasStatus(builds, Status.WAITING)) {
					cssClass += "fa-wait " + Status.WAITING.name().toLowerCase();
					title = "Some builds are waiting, click for details";
				} else if (hasStatus(builds, Status.QUEUEING)) {
					cssClass += "fa-hourglass-1 " + Status.QUEUEING.name().toLowerCase();
					title = "Some builds are queueing, click for details";
				} else if (hasStatus(builds, Status.RUNNING)) {
					cssClass += "fa-circle-o-notch fa-spin " + Status.RUNNING.name().toLowerCase();
					title = "Some builds are running, click for details";
				} else {
					cssClass += "fa-check-circle " + Status.SUCCESSFUL.name().toLowerCase();
					title = "Builds are successful, click for details";
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
		response.render(CssHeaderItem.forReference(new BuildCssResourceReference()));
	}
	
}
