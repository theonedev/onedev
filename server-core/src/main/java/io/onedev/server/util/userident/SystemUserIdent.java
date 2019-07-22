package io.onedev.server.util.userident;

import io.onedev.server.OneDev;

public class SystemUserIdent extends UserIdent {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return OneDev.NAME;
	}

}
