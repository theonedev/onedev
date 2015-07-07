package com.pmease.commons.wicket.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

public abstract class DirtyAwareAjaxLink<T> extends AjaxLink<T> {

	private static final long serialVersionUID = 1L;
	
	public DirtyAwareAjaxLink(final String id) {
		super(id);
	}

	public DirtyAwareAjaxLink(final String id, final IModel<T> model) {
		super(id, model);
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		
		attributes.getAjaxCallListeners().add(new IAjaxCallListener() {
			
			@Override
			public CharSequence getSuccessHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getPrecondition(Component component) {
				return "return pmease.commons.form.confirmLeave();";
			}
			
			@Override
			public CharSequence getFailureHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getCompleteHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getBeforeSendHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getBeforeHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getAfterHandler(Component component) {
				return null;
			}
			
		});
	}
	
}
