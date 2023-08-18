package io.onedev.server.ee;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.FeatureManager;
import io.onedev.server.ee.subscription.SubscriptionManager;
import io.onedev.server.ee.subscription.SupportRequestPanel;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;

@Singleton
public class EEFeatureManager implements FeatureManager, Serializable {
	
	private final SubscriptionManager subscriptionManager;
	
	@Inject
	public EEFeatureManager(SubscriptionManager subscriptionManager) {
		this.subscriptionManager = subscriptionManager;
	}
	
	@Override
	public boolean isEEAvailable() {
		return true;
	}

	@Override
	public boolean isEEActivated() {
		return subscriptionManager.isActive();
	}

	@Override
	public Component renderSupportRequestLink(String componentId) {
		if (isEEActivated() && !"code.onedev.io".equals(subscriptionManager.getLicensee())) {
			return new ModalLink(componentId) {

				@Override
				protected Component newContent(String id, ModalPanel modal) {
					return new SupportRequestPanel(id, modal);
				}
				
			};
		} else {
			return new WebMarkupContainer(componentId).setVisible(false);
		}
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(EEFeatureManager.class);
	}
	
}
