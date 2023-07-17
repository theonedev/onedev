package io.onedev.server.web.editable.code;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Code;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.convert.ConversionException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CodePropertyEditor extends PropertyEditor<Serializable> {

	private TextArea<String> input;
	
	private AbstractPostAjaxBehavior behavior;
	
	public CodePropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Serializable> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		
		if (descriptor.getPropertyClass() == String.class)
			container.add(input = new TextArea<String>("input", Model.of((String)getModelObject())));
		else
			container.add(input = new TextArea<String>("input", Model.of(StringUtils.join((List<?>)getModelObject(), "\n"))));
		
		input.setLabel(Model.of(getDescriptor().getDisplayName()));		
		input.setOutputMarkupId(true);

		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		add(behavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel("input-assist", AjaxChannel.Type.DROP));
			}
			
			@SuppressWarnings("unchecked")
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String matchWith = params.getParameterValue("matchWith").toString().toLowerCase();
				String line = params.getParameterValue("line").toString();
				String start = params.getParameterValue("start").toString();
				
				String variableProvider = getCode().variableProvider();
				ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
				ArrayNode variablesNode = mapper.createArrayNode();
				if (variableProvider.length() != 0) {
					ComponentContext.push(new ComponentContext(input));
					try { 
						for (InputSuggestion suggestion: ((List<InputSuggestion>) ReflectionUtils.invokeStaticMethod(
								descriptor.getBeanClass(), variableProvider, matchWith))) {
							ObjectNode variableNode = mapper.createObjectNode();
							String text = "@" + suggestion.getContent() + "@";
							variableNode.put("text", text); 
							if (suggestion.getDescription() != null)
								variableNode.put("description", suggestion.getDescription());
							variablesNode.add(variableNode);
						}
					} finally {
						ComponentContext.pop();
					}
				}
				try {
					String script = String.format("onedev.server.codeSupport.showVariables('%s', %s, %s, %s);", 
							input.getMarkupId(), mapper.writeValueAsString(variablesNode), line, start);
					target.appendJavaScript(script);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			}
			
		});
	}
	
	private Code getCode() {
		return Preconditions.checkNotNull(descriptor.getPropertyGetter().getAnnotation(Code.class));
	}

	@Override
	protected Serializable convertInputToValue() throws ConversionException {
		Serializable convertedInput;
		if (input.getConvertedInput() != null) {
			if (descriptor.getPropertyClass() == String.class) {
				convertedInput = input.getConvertedInput();
			} else {
				convertedInput = new ArrayList<String>();
				((List<String>)convertedInput).addAll(Splitter.on("\n").trimResults(CharMatcher.is('\r')).splitToList(input.getConvertedInput()));
			}
		} else {
			if (descriptor.getPropertyClass() == String.class) 
				convertedInput = null;
			else 
				convertedInput = new ArrayList<>();
		}
		return convertedInput;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CodeSupportResourceReference()));

		CallbackParameter matchWith = CallbackParameter.explicit("matchWith");
		CallbackParameter line = CallbackParameter.explicit("line");
		CallbackParameter start = CallbackParameter.explicit("start");
		CallbackParameter end = CallbackParameter.explicit("end");
		String script = String.format(
				"onedev.server.codeSupport.onEditorLoad('%s', '%s', %s);", 
				input.getMarkupId(), 
				getCode().language(), 
				behavior.getCallbackFunction(matchWith, line, start, end));
		
		// Initialize codemirror via onLoad; otherwise it will not be shown 
		// correctly in a modal dialog
		response.render(OnLoadHeaderItem.forScript(script));
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
