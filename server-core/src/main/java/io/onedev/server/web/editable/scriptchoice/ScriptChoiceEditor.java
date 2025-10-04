package io.onedev.server.web.editable.scriptchoice;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.util.ScriptContribution;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

public class ScriptChoiceEditor extends PropertyEditor<String> {

	private FormComponent<String> input;
	
	public ScriptChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<List<String>> choicesModel = new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				List<String> choices = new ArrayList<>();
				
				for (GroovyScript script: OneDev.getInstance(SettingService.class).getGroovyScripts())
					choices.add(script.getName());
				
				for (ScriptContribution contribution: OneDev.getExtensions(ScriptContribution.class)) {
					GroovyScript script = contribution.getScript();
					choices.add(GroovyScript.BUILTIN_PREFIX + script.getName());
				}
				
				return choices;
			}
			
		};
		
		String selection = getModelObject();
		if (!choicesModel.getObject().contains(selection))
			selection = null;
		
		input = new StringSingleChoice("input", Model.of(selection), choicesModel, false) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
				getSettings().setAllowClear(!descriptor.isPropertyRequired());
			}
			
		};
        input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));

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

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
