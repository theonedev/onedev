package io.onedev.server.web.editable.script;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class ScriptPropertyEditor extends PropertyEditor<List<String>> {

	private final String modeName;
	
	private TextArea<String> input;
	
	public ScriptPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel, String modeName) {
		super(id, propertyDescriptor, propertyModel);
		this.modeName = modeName;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		
		container.add(input = new TextArea<String>("input", Model.of(StringUtils.join(getModelObject(), "\n"))));
		input.setLabel(Model.of(getDescriptor().getDisplayName(this)));		
		input.setOutputMarkupId(true);

		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
	}

	@Override
	public ErrorContext getErrorContext(PathElement element) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> convertedInput = new ArrayList<>();
		if (input.getConvertedInput() != null)
			convertedInput.addAll(Splitter.on("\n").trimResults(CharMatcher.is('\r')).splitToList(input.getConvertedInput()));
		return convertedInput;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ScriptSupportResourceReference()));
		
		String script = String.format("onedev.server.scriptSupport.onEditorDomReady('%s', '%s');", 
				input.getMarkupId(), modeName);
		response.render(OnLoadHeaderItem.forScript(script));
	}

}
