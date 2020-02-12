package io.onedev.server.web.editable.scriptchoice;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.util.script.ScriptContribution;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class ScriptChoiceEditor extends PropertyEditor<String> {

	private FormComponent<String> input;
	
	public ScriptChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<Map<String, String>> choicesModel = new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				Map<String, String> choices = new LinkedHashMap<>();
				
				for (GroovyScript script: OneDev.getInstance(SettingManager.class).getGroovyScripts())
					choices.put(script.getName(), script.getName());
				
				for (ScriptContribution contribution: OneDev.getExtensions(ScriptContribution.class)) {
					GroovyScript script = contribution.getScript();
					String displayName = GroovyScript.BUILTIN_PREFIX + script.getName();
					choices.put(displayName, displayName);
				}
				
				return choices;
			}
			
		};
		
		String selection = getModelObject();
		if (!choicesModel.getObject().containsKey(selection))
			selection = null;
		
		input = new StringSingleChoice("input", Model.of(selection), choicesModel) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
        // add this to control allowClear flag of select2
    	input.setRequired(descriptor.isPropertyRequired());
        input.setLabel(Model.of(getDescriptor().getDisplayName()));

		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		
		add(input);
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
