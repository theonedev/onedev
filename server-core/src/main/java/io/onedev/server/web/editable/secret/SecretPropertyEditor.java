package io.onedev.server.web.editable.secret;

import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

public class SecretPropertyEditor extends PropertyEditor<String> {

	private FormComponent<String> input2;
	
	private boolean masked = true;
	
	public SecretPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var input1 = new WebMarkupContainer("input1");
		input1.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				return masked? "form-control text-monospace masked": "form-control text-monospace";
			}
		}));
		add(input1);
		
		input2 = new TextArea<>("input2", Model.of(getModelObject()));
		input2.setRequired(false);
		input2.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
		input2.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		add(input2);
		
		var toggleLink = new AjaxLink<Void>("toggle") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				masked = !masked;
				target.add(SecretPropertyEditor.this);
			}
		};
		toggleLink.add(new SpriteImage("icon", new AbstractReadOnlyModel<>() {
			@Override
			public String getObject() {
				return masked ? "eye" : "eye-close";
			}
		}));
		toggleLink.add(AttributeAppender.append("title", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				return masked? "Show": "Hide";
			}
		}));
		add(toggleLink);
		
		add(AttributeAppender.append("class", "secret-editor"));
		setOutputMarkupId(true);
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input2.getConvertedInput();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new SecretEditorResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript(String.format("onedev.server.secretEditor.onDomReady('%s');", getMarkupId())));
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
