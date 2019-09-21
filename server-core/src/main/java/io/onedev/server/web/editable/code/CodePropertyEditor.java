package io.onedev.server.web.editable.code;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.ReflectionUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.Code;

@SuppressWarnings("serial")
public class CodePropertyEditor extends PropertyEditor<List<String>> {

	private TextArea<String> input;
	
	public CodePropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
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
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> convertedInput = new ArrayList<>();
		if (input.getConvertedInput() != null)
			convertedInput.addAll(Splitter.on("\n").trimResults(CharMatcher.is('\r')).splitToList(input.getConvertedInput()));
		return convertedInput;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CodeSupportResourceReference()));

		Code code = Preconditions.checkNotNull(descriptor.getPropertyGetter().getAnnotation(Code.class));
		
		String variableProvider = code.variableProvider();
		ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
		ArrayNode variablesNode = mapper.createArrayNode();
		if (variableProvider.length() != 0) {
			ComponentContext.push(new ComponentContext(this));
			try { 
				for (String each: (List<String>) ReflectionUtils.invokeStaticMethod(
						descriptor.getBeanClass(), variableProvider)) {
					ObjectNode variableNode = mapper.createObjectNode();
					variableNode.put("text", Interpolative.MARK + each + Interpolative.MARK); 
					variablesNode.add(variableNode);
				}
			} finally {
				ComponentContext.pop();
			}
		}
		
		try {
			String script = String.format("onedev.server.codeSupport.onEditorDomReady('%s', '%s', %s, '%s');", 
					input.getMarkupId(), code.language(), mapper.writeValueAsString(variablesNode), Interpolative.MARK);
			response.render(OnLoadHeaderItem.forScript(script));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
