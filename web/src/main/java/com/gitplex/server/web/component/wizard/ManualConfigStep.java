package com.gitplex.server.web.component.wizard;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.FormComponent;

import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.utils.init.ManualConfig;

@SuppressWarnings("serial")
public class ManualConfigStep implements WizardStep {

	private ManualConfig config;
	
	public ManualConfigStep(ManualConfig config) {
		this.config = config;
	}
	
	@Override
	public FormComponent<Serializable> render(String componentId) {
		return BeanContext.editBean(componentId, config.getSetting(), config.getExcludeProperties());
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
	public void complete() {
		config.complete();
	}

	@Override
	public String getMessage() {
		return config.getMessage();
	}

}
