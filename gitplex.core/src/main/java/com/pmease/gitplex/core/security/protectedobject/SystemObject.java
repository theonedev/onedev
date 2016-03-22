package com.pmease.gitplex.core.security.protectedobject;

public class SystemObject implements ProtectedObject {

	@Override
	public boolean has(ProtectedObject object) {
		return true;
	}

}
