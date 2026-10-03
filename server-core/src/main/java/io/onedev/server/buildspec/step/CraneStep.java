package io.onedev.server.buildspec.step;

import io.onedev.server.annotation.Editable;

public abstract class CraneStep extends RegistryToolStep {

	private static final long serialVersionUID = 1L;

	@Editable
	@Override
	public String getImage() {
		return "1dev/crane:1.0.0";
	}

}
