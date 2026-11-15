package io.onedev.server.web.component.user.accesstoken;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.AuditService;
import io.onedev.server.model.AccessToken;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.user.UserPage;

abstract class AccessTokenPanel extends Panel {
	
	public AccessTokenPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onEdit(target);
			}
			
		});
		add(new AjaxLink<Void>("rotate") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getRevealedValue() == null);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(
						_T("Rotate this access token and invalidate its current value?")));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				var value = OneDev.getInstance(TransactionService.class).call(() -> {
					var token = getToken();
					var newValue = token.generate();
					OneDev.getInstance(AccessTokenService.class).createOrUpdate(token);
					if (getPage() instanceof UserPage) {
						OneDev.getInstance(AuditService.class).audit(null, "regenerated access token in account \"" + token.getOwner().getName() + "\"", null, null);
					}
					return newValue;
				});
				onRegenerated(target, value);
			}
		});
		add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this access token?")));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		});
		
		add(new WebMarkupContainer("expired").setVisible(getToken().isExpired()));

		add(new Label("value", (IModel<String>) () -> {
			var value = getRevealedValue();
			return value != null ? value : "*".repeat(30);
		}));
		add(new CopyToClipboardLink("copy", this::getRevealedValue) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getRevealedValue() != null);
			}
		});
		add(new WebMarkupContainer("notice") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getRevealedValue() != null);
			}
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onBeforeRender() {
		var bean = AccessTokenEditBean.of(getToken());
		var description = Set.of("description");
		addOrReplace(BeanContext.view("description", bean, description, false));
		addOrReplace(BeanContext.view("viewer", bean, description, true));
		super.onBeforeRender();
	}

	protected abstract AccessToken getToken();
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onEdit(AjaxRequestTarget target);

	protected abstract void onRegenerated(AjaxRequestTarget target, String value);

	@Nullable
	protected abstract String getRevealedValue();
	
}
