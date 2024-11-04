package io.onedev.server.web.component.user.accesstoken;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AccessTokenManager;
import io.onedev.server.model.AccessToken;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.editable.BeanContext;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
abstract class AccessTokenPanel extends Panel {
	
	public AccessTokenPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var token = getToken();
		add(new Label("name", token.getName()));
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onEdit(target);
			}
			
		});
		add(new AjaxLink<Void>("regenerate") {
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener("This will invalidate current access token and " +
						"generate a new one. Do you really want to continue?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				var token = getToken();
				token.setValue(CryptoUtils.generateSecret());
				OneDev.getInstance(AccessTokenManager.class).createOrUpdate(token);
				target.add(AccessTokenPanel.this);
				Session.get().success("Access token regenerated successfully");
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
		
		add(new WebMarkupContainer("expired").setVisible(getToken().isExpired()));
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onBeforeRender() {
		addOrReplace(BeanContext.view("viewer", AccessTokenEditBean.of(getToken()), Sets.newHashSet("name"), true));
		super.onBeforeRender();
	}

	protected abstract AccessToken getToken();
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onEdit(AjaxRequestTarget target);
	
}
