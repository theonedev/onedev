package io.onedev.server.web.websocket;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;

public class ObservablesChanged implements IWebSocketPushMessage {
	
	private final Set<String> observables;
	
    public ObservablesChanged(Set<String> observables) {
        this.observables = observables;
    }

    public Collection<String> getObservables() {
        return observables;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ObservablesChanged observablesChanged)
			return new EqualsBuilder().append(observables, observablesChanged.observables).isEquals();
        else 
            return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(observables).toHashCode();
    }

}