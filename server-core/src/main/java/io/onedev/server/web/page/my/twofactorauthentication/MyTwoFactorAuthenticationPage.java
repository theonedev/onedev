package io.onedev.server.web.page.my.twofactorauthentication;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.user.twofactorauthentication.TwoFactorAuthenticationSetupPanel;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MyTwoFactorAuthenticationPage extends MyPage {

	public MyTwoFactorAuthenticationPage(PageParameters params) {
		super(params);
	}
	
	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (getLoginUser().getSsoConnector() != null) {
			add(new Label("content", "You are currently authenticated via SSO provider '" 
					+ getLoginUser().getSsoConnector() + "', "
					+ "and two-factor authentication should be configured there")
					.add(AttributeAppender.append("class", "alert alert-light-warning alert-notice mb-0")));
		} else if (getLoginUser().getTwoFactorAuthentication() != null) {
			Fragment fragment = new Fragment("content", "enabledFrag", this);
			fragment.add(new Label("message", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					if (getLoginUser().isEnforce2FA() && !SecurityUtils.isAdministrator()) {
						return "Two-factor authentication is enforced for your account, "
								+ "and can only be disabled by administrator";
					} else {
						return "Two-factor authentication is enabled for your account";
					}
				}
				
			}));
			fragment.add(new AjaxLink<Void>("disable") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmClickListener(
							"Do you really want to disable two-factor authentication?"));
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					getLoginUser().setTwoFactorAuthentication(null);
					getUserManager().update(getLoginUser(), null);
					Session.get().success("Two-factor authentication disabled");
					setResponsePage(MyTwoFactorAuthenticationPage.class);
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.isAdministrator() || !getLoginUser().isEnforce2FA());
				}
				
			});
			add(fragment);
		} else {
			Fragment fragment = new Fragment("content", "disabledFrag", this);
			fragment.add(new ModalLink("enable") {

				@Override
				protected Component newContent(String id, ModalPanel modal) {
					return new TwoFactorAuthenticationSetupPanel(id) {
						
						@Override
						protected void onEnabled(AjaxRequestTarget target) {
							modal.close();
							setResponsePage(MyTwoFactorAuthenticationPage.class);
						}

						@Override
						protected void onCancelled(AjaxRequestTarget target) {
							modal.close();
						}

						@Override
						protected User getUser() {
							return getLoginUser();
						}
						
					};
				}

			});
			add(fragment);
		}
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Two Factor Authentication");
	}

}
