package io.onedev.server.web.component.user.accesstoken;

import io.onedev.server.model.support.AccessToken;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.util.Date;

import static io.onedev.server.util.DateUtils.formatDate;

@SuppressWarnings("serial")
abstract class AccessTokenPanel extends Panel {

	private final AccessToken accessToken;
	
	public AccessTokenPanel(String id, AccessToken accessToken) {
		super(id);
		this.accessToken = accessToken;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("value", accessToken.getMaskedValue()));
		add(new CopyToClipboardLink("copy", Model.of(accessToken.getValue())));
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onEdit(target);
			}
			
		});
		
		add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this access token?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		});
		
		if (accessToken.getDescription() != null)
			add(new MultilineLabel("description", accessToken.getDescription()));
		else
			add(new Label("description", "<i>No description</i>").setEscapeModelStrings(false));
			
		add(new Label("createdAt", formatDate(accessToken.getCreateDate())));
		
		var expireDate = accessToken.getExpireDate();
		if (expireDate != null) {
			if (expireDate.before(new Date())) {
				add(new Label("expiresAt", formatDate(expireDate))
						.add(AttributeAppender.append("class", "text-danger")));
			} else {
				add(new Label("expiresAt", formatDate(expireDate)));
			}
		} else {
			add(new Label("expiresAt", "<i>Never expires</i>").setEscapeModelStrings(false));
		}
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onEdit(AjaxRequestTarget target);
	
}
