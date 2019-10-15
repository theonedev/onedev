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

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.groovyscript.GroovyScript;
import io.onedev.server.util.scriptidentity.ScriptIdentity;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.ScriptChoice;

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
				ScriptChoice scriptChoice = descriptor.getPropertyGetter().getAnnotation(ScriptChoice.class);
				Preconditions.checkNotNull(scriptChoice);
				
				for (GroovyScript script: OneDev.getInstance(SettingManager.class).getGroovyScripts()) {
					if (script.isAuthorized(ScriptIdentity.get())) 
						choices.put(script.getName(), null);
				}
				return choices;
			}
			
		};
		
		input = new StringSingleChoice("input", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String selection = ScriptChoiceEditor.this.getModelObject();
				if (choicesModel.getObject().containsKey(selection))
					return selection;
				else
					return null;
			}
			
		}, choicesModel) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor, this);
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
