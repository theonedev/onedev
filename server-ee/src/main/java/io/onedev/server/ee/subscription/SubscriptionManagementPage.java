package io.onedev.server.ee.subscription;

import io.onedev.license.LicensePayload;
import io.onedev.license.LicenseeUpdate;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.page.admin.AdministrationPage;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import static com.google.common.base.Preconditions.checkState;
import static io.onedev.server.ee.subscription.SubscriptionSetting.ENCRYPTION_KEY;
import static io.onedev.server.ee.subscription.SubscriptionSetting.cipherService;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

@SuppressWarnings("serial")
public class SubscriptionManagementPage extends AdministrationPage {

	public SubscriptionManagementPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var subscriptionSetting = SubscriptionSetting.load();
		var subscription = subscriptionSetting.getSubscription();
		if (subscription == null) {
			var fragment = new Fragment("content", "noSubscriptionFrag", this);
			fragment.add(newSubscriptionKeyInstallLink("installSubscriptionKey", subscriptionSetting));

			if (subscriptionSetting.getUsedSubscriptionKeyUUIDs().isEmpty()) {
				var firstTrialSubscriptionRequestFragment = new Fragment("requestTrialSubscription",
						"firstTrialSubscriptionRequestFrag", SubscriptionManagementPage.this);
				firstTrialSubscriptionRequestFragment.add(new WebMarkupContainer("subscriptionKeyUUID") {
					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						var systemUUID = OneDev.getInstance(SettingManager.class).getSystemUUID();
						tag.put("value", encodeBase64String(cipherService.encrypt(systemUUID.getBytes(), decodeBase64(ENCRYPTION_KEY)).getBytes()));
					}
				});
				fragment.add(firstTrialSubscriptionRequestFragment);
			} else {
				fragment.add(new Fragment("requestTrialSubscription",
						"moreTrialSubscriptionRequestFrag", SubscriptionManagementPage.this));
			}
			fragment.add(new FencedFeedbackPanel("feedback", fragment));
			add(fragment);
		} else if (subscription.isTrial()) {
			var fragment = new Fragment("content", "trialSubscriptionFrag", this);

			var detail = new WebMarkupContainer("detail");
			fragment.add(detail);

			detail.add(new Label("licensee", subscription.getLicensee()));
			var licenseGroup = subscription.getLicenseGroup();
			detail.add(new Label("licenseGroup", licenseGroup)
					.setVisible(licenseGroup != null));
			detail.add(new WebMarkupContainer("licenseGroupNotFound")
					.setVisible(licenseGroup != null && getGroupManager().find(licenseGroup) == null));

			var expirationDate = subscription.getExpirationDate(subscription.countUsers());
			if (expirationDate != null) {
				detail.add(new Label("title", "This installation has a trial subscription and runs as enterprise edition"));
				detail.add(new Label("expirationDate", DateUtils.formatDate(expirationDate)));
				detail.add(new WebMarkupContainer("alert").setVisible(false));
				detail.add(AttributeAppender.append("class", "alert-light-warning"));
			} else {
				detail.add(new Label("title", "This installation has an expired trial subscription and runs as community edition"));
				detail.add(new WebMarkupContainer("expirationDate").setVisible(false));
				detail.add(new Label("alert", "** Enterprise edition is disabled as the trial " +
						"subscription was expired, order subscription to enable or contact support@onedev.io " +
						"if you need to extend your trial **"));
				detail.add(AttributeAppender.append("class", "alert-light-danger"));
			}

			fragment.add(newSubscriptionKeyInstallLink("installSubscriptionKey", subscriptionSetting));
			fragment.add(newSubscriptionDeactivateLink("deactivateSubscription", subscriptionSetting));
			add(fragment);
		} else {
			var fragment = new Fragment("content", "subscriptionFrag", this);
			var detail = new WebMarkupContainer("detail");
			fragment.add(detail);

			if (subscription.isExpired()) {
				detail.add(new Label("title", "This installation has an expired subscription, and runs as community edition"));
				var alertMessage = "** Enterprise edition is disabled as there is no remaining user months. " +
						"Order more to enable **";
				detail.add(AttributeAppender.append("class", "alert-light-danger"));
				detail.add(new Label("alert", alertMessage));
			} else {
				detail.add(new Label("title", "This installation has an active subscription and runs as enterprise edition"));
				detail.add(AttributeAppender.append("class", "alert-light-success"));
				detail.add(new WebMarkupContainer("alert").setVisible(false));
			}

			detail.add(new Label("licensee", subscription.getLicensee()));
			var licenseGroup = subscription.getLicenseGroup();
			detail.add(new Label("licenseGroup", licenseGroup)
					.setVisible(licenseGroup != null));
			detail.add(new WebMarkupContainer("licenseGroupNotFound")
					.setVisible(licenseGroup != null && getGroupManager().find(licenseGroup) == null));
			detail.add(new Label("userMonths", Math.ceil(subscription.getUserDays()/31.0)));
			
			var userCount = subscription.countUsers();
			var expirationDate = subscription.getExpirationDate(userCount);
			if (expirationDate != null) {
				String message;
				if (licenseGroup != null && getGroupManager().find(licenseGroup) != null)
					message = "With current number of users (" + userCount + ") in group '" + licenseGroup + "', ";
				else
					message = "With current number of non-guest users (" + userCount + "), ";

				message += "the subscription will be active until <b>" + DateUtils.formatDate(expirationDate) + "</b>";
				detail.add(new Label("expirationInfo", message).setEscapeModelStrings(false));
			} else {
				detail.add(new WebMarkupContainer("expirationInfo").setVisible(false));
			}

			fragment.add(newSubscriptionKeyInstallLink("installSubscriptionKey", subscriptionSetting));
			fragment.add(newSubscriptionDeactivateLink("deactivateSubscription", subscriptionSetting));
			add(fragment);
		}
	}

	private Component newSubscriptionKeyInstallLink(String componentId, SubscriptionSetting subscriptionSetting) {
		return new AjaxLink<Void>(componentId) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				var bean = new SubscriptionKeyEditBean();
				new BeanEditModalPanel<>(target, bean) {

					@Override
					protected void onSave(AjaxRequestTarget target, SubscriptionKeyEditBean bean) {
						checkState(subscriptionSetting.install(bean.getSubscriptionKey()) == null);
						var payload = LicensePayload.verifyLicense(bean.getSubscriptionKey());
						subscriptionSetting.save();
						if (!(payload instanceof LicenseeUpdate)) {
							var settingManager = OneDev.getInstance(SettingManager.class);
							var alertSetting = settingManager.getAlertSetting();
							alertSetting.setAlerted(false);
							settingManager.saveAlertSetting(alertSetting);
						}
						setResponsePage(SubscriptionManagementPage.class);
						Session.get().success("Subscription key installed successfully");
					}

				};
			}
		};
	}

	private Component newSubscriptionDeactivateLink(String componentId, SubscriptionSetting subscriptionSetting) {
		return new AjaxLink<Void>(componentId) {
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener("This will deactivate current subscription and all enterprise features will be disabled, do you want to continue?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				subscriptionSetting.setSubscription(null);
				subscriptionSetting.save();
				setResponsePage(SubscriptionManagementPage.class);
			}
		};
	}

	private GroupManager getGroupManager() {
		return OneDev.getInstance(GroupManager.class);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new org.apache.wicket.markup.html.basic.Label(componentId, "Subscription Management");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SupportRequestResourceReference()));
	}
}
