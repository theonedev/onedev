package io.onedev.server.web.component.link;

import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.ajaxlistener.TrackViewStateListener;

@SuppressWarnings("serial")
public abstract class ViewStateAwareAjaxLink<T> extends PreventDefaultAjaxLink<T> {

	private final boolean carryOver;
	
	public ViewStateAwareAjaxLink(String id) {
		this(id, false);
	}
	
	public ViewStateAwareAjaxLink(String id, IModel<T> model) {
		this(id, model, false);
	}
	
	public ViewStateAwareAjaxLink(String id, boolean carryOver) {
		super(id);
		this.carryOver = carryOver;
	}

	public ViewStateAwareAjaxLink(String id, IModel<T> model, boolean carryOver) {
		super(id, model);
		this.carryOver = carryOver;
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.getAjaxCallListeners().add(new TrackViewStateListener(carryOver));
	}
	
}
