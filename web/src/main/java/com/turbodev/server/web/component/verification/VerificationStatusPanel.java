package com.turbodev.server.web.component.verification;

import java.util.Collection;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.turbodev.server.util.Verification;
import com.turbodev.server.util.Verification.Status;
import com.turbodev.server.web.component.floating.FloatingPanel;
import com.turbodev.server.web.component.link.DropdownLink;
import com.turbodev.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
public class VerificationStatusPanel extends GenericPanel<Map<String, Verification>> {

	public VerificationStatusPanel(String id, IModel<Map<String, Verification>> model) {
		super(id, model);
	}

	private boolean hasStatus(Collection<Verification> verifications, Verification.Status status) {
		for (Verification verification: verifications) {
			if (verification.getStatus() == status)
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
				return new VerificationDetailPanel(id, VerificationStatusPanel.this.getModel());
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				Collection<Verification> verifications = VerificationStatusPanel.this.getModelObject().values();
				if (hasStatus(verifications, Status.ERROR)) {
					tag.put("class", "verification-status error fa fa-warning");
					tag.put("title", "Some verifications are in error, click for details");
				} else if (hasStatus(verifications, Status.FAILURE)) {
					tag.put("class", "verification-status failure fa fa-times");
					tag.put("title", "Some verifications are failed, click for details");
				} else if (hasStatus(verifications, Status.RUNNING)) {
					tag.put("class", "verification-status running fa fa-circle");
					tag.put("title", "Some verifications are running, click for details");
				} else if (hasStatus(verifications, Status.SUCCESS)) {
					tag.put("class", "verification-status success fa fa-check");
					tag.put("title", "Verifications are successful, click for details");
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
		response.render(CssHeaderItem.forReference(new VerificationResourceReference()));
	}
	
}
