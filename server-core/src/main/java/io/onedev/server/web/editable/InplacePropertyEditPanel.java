package io.onedev.server.web.editable;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.util.EditContext;
import io.onedev.server.web.util.IssueQueryAware;
import io.onedev.server.web.util.ProjectAware;

public abstract class InplacePropertyEditPanel extends Panel implements EditContext, IssueQueryAware, ProjectAware {

	private final Serializable bean;
	
	private final String propertyName;
	
	private final BeanDescriptor beanDescriptor;
	
	private PropertyEditor<?> propertyEditor;
	
	public InplacePropertyEditPanel(String id, Serializable bean, String propertyName) {
		super(id);
		this.beanDescriptor = new BeanDescriptor(bean.getClass());
		this.bean = bean;
		this.propertyName = propertyName;
	}

	@Override
	public Object getInputValue(String name) {
		return beanDescriptor.getProperty(name).getPropertyValue(bean);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		
		form.add(propertyEditor = PropertyContext.edit("property", bean, propertyName));
		form.add(new FencedFeedbackPanel("feedback", propertyEditor) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(propertyEditor.hasErrorMessage());
			}
			
		});
		
		WebMarkupContainer confirm = new WebMarkupContainer("confirm");
		confirm.setVisible(propertyEditor.needExplicitSubmit());
		add(confirm);
		
		confirm.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onUpdated(target, bean, propertyName);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		confirm.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancelled(target);
			}
			
		});
		form.add(confirm);
		
		add(form);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof PropertyUpdating) {
			event.stop();
			if (!propertyEditor.needExplicitSubmit()) {
				propertyEditor.getDescriptor().setPropertyValue(bean, propertyEditor.getConvertedInput());
				onUpdated(((PropertyUpdating) event.getPayload()).getHandler(), bean, propertyName);
			}
		}
		
	}
	
	protected abstract void onUpdated(IPartialPageRequestHandler handler, Serializable bean, String propertyName);
	
	protected abstract void onCancelled(IPartialPageRequestHandler handler);
	
}
