package io.onedev.server.web.component.wizard;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.form.FormComponent;

import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.web.editable.BeanContext;

@SuppressWarnings("serial")
public class ManualConfigStep implements WizardStep {

	private ManualConfig config;
	
	public ManualConfigStep(ManualConfig config) {
		this.config = config;
	}
	
	@Override
	public FormComponent<Serializable> render(String componentId) {
		return BeanContext.edit(componentId, config.getSetting(), config.getExcludeProperties(), true);
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
	public String getTitle() {
		return config.getTitle();
	}

	@Nullable
	@Override
	public String getDescription() {
		return config.getDescription();
	}
}
