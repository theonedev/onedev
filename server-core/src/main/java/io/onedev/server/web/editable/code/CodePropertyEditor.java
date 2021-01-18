package io.onedev.server.web.editable.code;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.convert.ConversionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.Code;

@SuppressWarnings("serial")
public class CodePropertyEditor extends PropertyEditor<List<String>> {

	private TextArea<String> input;
	
	private AbstractPostAjaxBehavior behavior;
	
	public CodePropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		
		container.add(input = new TextArea<String>("input", Model.of(StringUtils.join(getModelObject(), "\n"))));
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
						for (String each: (List<String>) ReflectionUtils.invokeStaticMethod(
								descriptor.getBeanClass(), variableProvider)) {
							if (each.toLowerCase().contains(matchWith)) {
								ObjectNode variableNode = mapper.createObjectNode();
								variableNode.put("text", "@" + StringUtils.escape(each, "@") + "@"); 
								variablesNode.add(variableNode);
							}
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
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> convertedInput = new ArrayList<>();
		if (input.getConvertedInput() != null)
			convertedInput.addAll(Splitter.on("\n").trimResults(CharMatcher.is('\r')).splitToList(input.getConvertedInput()));
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
				"onedev.server.codeSupport.onEditorDomReady('%s', '%s', %s);", 
				input.getMarkupId(), 
				getCode().language(), 
				behavior.getCallbackFunction(matchWith, line, start, end));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
