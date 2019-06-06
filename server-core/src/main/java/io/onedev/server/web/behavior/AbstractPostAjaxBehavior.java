package io.onedev.server.web.behavior;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;

public abstract class AbstractPostAjaxBehavior extends AbstractDefaultAjaxBehavior {

	private static final long serialVersionUID = 1L;

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setMethod(Method.POST);
	}
	
}
