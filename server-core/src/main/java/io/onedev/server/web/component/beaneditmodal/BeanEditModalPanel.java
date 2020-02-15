package io.onedev.server.web.component.beaneditmodal;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class BeanEditModalPanel extends ModalPanel {

	private final Serializable bean;
	
	private final Collection<String> propertyNames;
	
	private final boolean exclude;
	
	private final String title;
	
	public BeanEditModalPanel(AjaxRequestTarget target, Serializable bean) {
		this(target, bean, new HashSet<>(), true, null);
	}
			
	public BeanEditModalPanel(AjaxRequestTarget target, Serializable bean, 
			Collection<String> propertyNames, boolean exclude, @Nullable String title) {
		super(target);
		this.bean = bean;
		this.propertyNames = propertyNames;
		this.exclude = exclude;
		this.title = title;
	}
	
	@Override
	protected Component newContent(String id) {
		return new BeanEditContentPanel(id) {
			
			@Override
			protected void onSave(AjaxRequestTarget target, Serializable bean) {
				BeanEditModalPanel.this.onSave(target, bean);
			}
			
			@Override
			protected void onCancel(AjaxRequestTarget target) {
				close();
				BeanEditModalPanel.this.onCancel(target);
			}

			@Override
			protected Serializable getBean() {
				return bean;				
			}

			@Override
			protected Collection<String> getPropertyNames() {
				return propertyNames;
			}

			@Override
			protected boolean isExclude() {
				return exclude;
			}
			
			@Override
			protected String getTitle() {
				return title;
			}
			
		};
	}

	protected abstract void onSave(AjaxRequestTarget target, Serializable bean);
	
	protected void onCancel(AjaxRequestTarget target) {
	}
	
}
