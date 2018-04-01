/*
 * Copyright OneDev Inc.,
 * Date: 2008-8-4
 * All rights reserved.
 *
 * Revision: $Id$
 */
package io.onedev.server.web.component.wizard;

import java.io.Serializable;

import org.apache.wicket.Component;

public interface WizardStep extends Serializable {

	Component render(String componentId);
	
	String getMessage();
	
	Skippable getSkippable();
	
	void complete();
	
}
