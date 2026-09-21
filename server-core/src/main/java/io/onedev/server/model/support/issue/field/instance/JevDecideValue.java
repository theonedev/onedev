package io.onedev.server.model.support.issue.field.instance;

import io.onedev.server.annotation.Editable;

/**
 * The specified value is used as a fallback when Jev cannot decide confidently.
 */
@Editable
public class JevDecideValue extends SpecifiedValue {

	private static final long serialVersionUID = 1L;

	public static final String DISPLAY_NAME = "Decide by Jev";

}
