package io.onedev.server.ee.subscription;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.util.WicketUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;

@Singleton
public class DelegateSubscriptionManager implements SubscriptionManager, Serializable {
	
	private final EESubscriptionManager subscriptionManager;
	
	@Inject
	public DelegateSubscriptionManager(EESubscriptionManager subscriptionManager) {
		this.subscriptionManager = subscriptionManager;
	}
	
	@Override
	public boolean isSubscriptionActive() {
		return subscriptionManager.isSubscriptionActive();
	}

	@Nullable
	@Override
	public String getLicensee() {
		return subscriptionManager.getLicensee();
	}
	
	@Override
	public Component renderSupportRequestLink(String componentId) {
		if (WicketUtils.isSubscriptionActive() && !"code.onedev.io".equals(getLicensee())) {
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
		return new ManagedSerializedForm(DelegateSubscriptionManager.class);
	}
	
}
