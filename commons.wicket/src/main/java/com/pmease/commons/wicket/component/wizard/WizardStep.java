/*
 * Copyright PMEase Inc.,
 * Date: 2008-8-4
 * All rights reserved.
 *
 * Revision: $Id$
 */
package com.pmease.commons.wicket.component.wizard;

import java.io.Serializable;

import org.apache.wicket.Component;

public interface WizardStep extends Serializable {

	Component render(String componentId);
	
	String getMessage();
	
	Skippable getSkippable();
	
	void complete();
	
}
