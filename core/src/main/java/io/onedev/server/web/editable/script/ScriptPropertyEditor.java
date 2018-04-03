package io.onedev.server.web.editable.script;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class ScriptPropertyEditor extends PropertyEditor<String> {

	private TextArea<String> input;
	
	public ScriptPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		
		container.add(input = new TextArea<String>("input", Model.of(getModelObject())));
		input.setLabel(Model.of(getPropertyDescriptor().getDisplayName(this)));		
		input.setOutputMarkupId(true);
		
		container.add(new AttributeAppender("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (hasErrors(true))
					return " has-error";
				else
					return "";
			}
			
		}));
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ScriptSupportResourceReference()));
		
		String script = String.format("onedev.server.scriptSupport.onEditorDomReady('%s');", input.getMarkupId());
		response.render(OnLoadHeaderItem.forScript(script));
	}

}
