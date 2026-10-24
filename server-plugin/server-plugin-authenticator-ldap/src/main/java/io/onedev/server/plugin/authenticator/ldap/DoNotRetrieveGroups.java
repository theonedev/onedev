package io.onedev.server.plugin.authenticator.ldap;

import java.io.Serial;

import io.onedev.server.annotation.Editable;

@Editable(order=100)
public class DoNotRetrieveGroups implements GroupRetrieval {

	@Serial
	private static final long serialVersionUID = 1L;

}
