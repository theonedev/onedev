package io.onedev.server.util;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.markup.html.form.FormComponent;

import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.web.behavior.ForceOrdinaryStyleBehavior;
import io.onedev.server.web.component.wizard.WizardStep;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

public class ManualConfigStep implements WizardStep {

	private ManualConfig config;
	
	public ManualConfigStep(ManualConfig config) {
		this.config = config;
	}
	
	@Override
	public FormComponent<Serializable> render(String componentId) {
		BeanEditor editor = BeanContext.edit(componentId, config.getSetting(), 
				config.getExcludeProperties(), true);
		if (config.isForceOrdinaryStyle())
			editor.add(new ForceOrdinaryStyleBehavior());
		return editor;
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

	@Override
	public void init() {
	}
	
}
