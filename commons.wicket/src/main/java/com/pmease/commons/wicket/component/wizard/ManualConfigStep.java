package com.pmease.commons.wicket.component.wizard;

import org.apache.wicket.Component;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.util.init.ManualConfig;

@SuppressWarnings("serial")
public class ManualConfigStep implements WizardStep {

	private ManualConfig config;
	
	private EditContext editContext;
	
	public ManualConfigStep(ManualConfig config) {
		this.config = config;
		editContext = EditableUtils.getContext(config.getSetting());
	}
	
	@Override
	public Component render(String componentId) {
		return (Component) editContext.renderForEdit(componentId);
	}

	@Override
	public Skippable getSkippable() {
		if (config.getSkippable() != null) {
			return new Skippable() {

				@Override
				public void skip() {
					config.getSkippable().skip();
				}
				
			};
		} else {
			return null;
		}
	}

	@Override
	public boolean complete() {
		editContext.validate();
		if (editContext.hasValidationErrors()) {
			return false;
		} else {
			config.complete();
			return true;
		}
	}

	@Override
	public String getMessage() {
		return config.getMessage();
	}

}
