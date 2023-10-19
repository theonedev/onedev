package io.onedev.server.ee.subscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.license.ServerInfo;
import io.onedev.license.SiteInfo;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

public class SupportRequestPanel extends Panel {
	
	private final ModalPanel modal;
	
	public SupportRequestPanel(String id, ModalPanel modal) {
		super(id);
		this.modal = modal;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("contactEmail") {
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				var primaryEmailAddress = SecurityUtils.getUser().getPrimaryEmailAddress();
				if (primaryEmailAddress != null)
					tag.put("value", primaryEmailAddress.getValue());
			}
		});
		add(new WebMarkupContainer("contactName") {
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("value", SecurityUtils.getUser().getFullName());
			}
		});

		var siteInfo = newSiteInfo();
		
		add(new WebMarkupContainer("siteInfo") {
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				try {
					tag.put("value", OneDev.getInstance(ObjectMapper.class).writeValueAsString(siteInfo));
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			}
		});
		add(new Label("productVersion", siteInfo.getProductVersion()));
		add(new Label("agentCount", siteInfo.getAgentCount()));
		add(new Label("licenseUserCount", siteInfo.getUserCount()));
		
		if (siteInfo.isTrialSubscription()) {
			add(new Label("remainingUserMonthsOrTrialExpirationDateLabel", "Trial Expiration Date"));
			add(new Label(
					"remainingUserMonthsOrTrialExpirationDate", 
					DateUtils.formatDate(new Date(siteInfo.getRemainingUserMonthsOrTrialExpirationDate()))));
		} else {
			add(new Label("remainingUserMonthsOrTrialExpirationDateLabel", "Remaining User Months"));
			add(new Label(
					"remainingUserMonthsOrTrialExpirationDate", 
					siteInfo.getRemainingUserMonthsOrTrialExpirationDate()));
		}

		add(BeanContext.view("serversAndSubscriptionKeys", siteInfo, 
				Sets.newHashSet("servers", "subscriptionKeys"), false));
		
		add(newCloseLink("cancel", modal));
		add(newCloseLink("close", modal));
	}

	private Component newCloseLink(String componentId, ModalPanel modal) {
		return new AjaxLink<Void>(componentId) {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(modal));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				modal.close();
			}

		};
	}

	private SiteInfo newSiteInfo() {
		var subscriptionSettingSetting = SubscriptionSetting.load();
		var subscription = subscriptionSettingSetting.getSubscription();
		
		var siteInfo = new SiteInfo();
		siteInfo.setProductVersion(AppLoader.getProduct().getVersion());
		siteInfo.setAgentCount(OneDev.getInstance(AgentManager.class).getOnlineAgents().size());
		siteInfo.setUserCount(subscription.countUsers());
		siteInfo.setTrialSubscription(subscription.isTrial());
		if (subscription.isTrial()) {
			var expirationDate = subscription.getExpirationDate(siteInfo.getUserCount());
			if (expirationDate != null)
				siteInfo.setRemainingUserMonthsOrTrialExpirationDate(expirationDate.getTime());
			else 
				siteInfo.setRemainingUserMonthsOrTrialExpirationDate(new Date().getTime());
		} else {
			siteInfo.setRemainingUserMonthsOrTrialExpirationDate(subscription.getUserDays() / 31);
		}

		siteInfo.setSubscriptionKeys(StringUtils.join(subscriptionSettingSetting.getUsedSubscriptionKeyUUIDs(), "\n"));

		siteInfo.getServers().addAll(OneDev.getInstance(ClusterManager.class).runOnAllServers(() -> {
			var serverInfo = new ServerInfo();
			serverInfo.setSystemDate(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z").print(new DateTime()));
			serverInfo.setJvm(System.getProperty("java.vm.name") + " " + System.getProperty("java.version") + ", " + System.getProperty("java.vm.vendor"));
			serverInfo.setOs(System.getProperty("os.name") + " " + System.getProperty("os.version") + ", " + System.getProperty("os.arch"));
			serverInfo.setTotalMemory(FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory()));
			serverInfo.setUsedMemory(FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
			if (OneDev.getK8sService() != null)
				serverInfo.setInstallType("Kubernetes");
			else if (Bootstrap.isInDocker())
				serverInfo.setInstallType("Docker");
			else
				serverInfo.setInstallType("Bare Metal");
			return serverInfo;
		}).values());

		return siteInfo;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new SupportRequestResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.supportRequest.onDomReady();"));
	}
	
}
