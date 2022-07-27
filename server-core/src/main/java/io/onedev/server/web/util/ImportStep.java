package io.onedev.server.web.util;

import java.io.Serializable;

import org.apache.wicket.Component;

import io.onedev.server.web.component.wizard.WizardStep;
import io.onedev.server.web.editable.BeanContext;

public abstract class ImportStep<T extends Serializable> implements WizardStep {

	private static final long serialVersionUID = 1L;
	
	private T setting;
	
	@Override
	public Component render(String componentId) {
		return BeanContext.edit(componentId, setting);
	}

	@Override
	public String getDescription() {
		return null;
	}
	
	protected abstract T newSetting();

	@Override
	public void init() {
		setting = newSetting();
	}
	
	@Override
	public void complete() {
		
	}

	public T getSetting() {
		return setting;
	}
	
}
