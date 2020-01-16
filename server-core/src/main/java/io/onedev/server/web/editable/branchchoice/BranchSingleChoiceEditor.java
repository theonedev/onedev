package io.onedev.server.web.editable.branchchoice;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.branch.choice.BranchSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class BranchSingleChoiceEditor extends PropertyEditor<String> {
	
	private BranchSingleChoice input;
	
	public BranchSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
		Map<String, String> choices = new LinkedHashMap<>();
		if (Project.get() != null) {
			for (RefInfo ref: Project.get().getBranchRefInfos()) {
				String branch = GitUtils.ref2branch(ref.getRef().getName());
				choices.put(branch, branch);
			}
		}
		
		String selection = getModelObject();
		if (!choices.containsKey(selection))
			selection = null;

    	input = new BranchSingleChoice("input", Model.of(selection), Model.ofMap(choices)) {
    		
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
