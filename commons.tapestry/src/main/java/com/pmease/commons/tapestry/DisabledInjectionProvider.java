package com.pmease.commons.tapestry;

import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.transform.InjectionProvider2;

public class DisabledInjectionProvider implements InjectionProvider2 {

	@Override
	public boolean provideInjection(PlasticField field, ObjectLocator locator,
			MutableComponentModel componentModel) {
		return false;
	}

}
