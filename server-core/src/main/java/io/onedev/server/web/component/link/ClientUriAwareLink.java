package io.onedev.server.web.component.link;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * When behind a reverse proxy, the proxy may not forward protocol
 * information correctly, so we use this class to get browser url 
 * directly  
 * 
 * @author robin
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class ClientUriAwareLink<T> extends AjaxLink<T> {

	public ClientUriAwareLink(String id) {
		super(id);
	}
	
	public ClientUriAwareLink(String id, IModel<T> model) {
		super(id, model);
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);

		attributes.setMethod(Method.POST);
		attributes.getDynamicExtraParameters().add("return {clientUri: window.location.href};");
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		String uriString = RequestCycle.get().getRequest().getPostParameters()
				.getParameterValue("clientUri").toString();
		try {
			onClick(target, new URI(uriString));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract void onClick(AjaxRequestTarget target, URI clientUri);
	
}
