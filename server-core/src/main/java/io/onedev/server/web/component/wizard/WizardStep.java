/*
 * Copyright OneDev Inc.,
 * Date: 2008-8-4
 * All rights reserved.
 *
 * Revision: $Id$
 */
package io.onedev.server.web.component.wizard;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;

public interface WizardStep extends Serializable {

	Component render(String componentId);
	
	String getTitle();
	
	@Nullable
	String getDescription();
	
	void init();
	
	void complete();
	
}
