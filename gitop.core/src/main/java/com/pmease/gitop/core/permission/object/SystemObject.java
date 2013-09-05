package com.pmease.gitop.core.permission.object;

public class SystemObject implements ProtectedObject {

	@Override
	public boolean has(ProtectedObject object) {
		return true;
	}

}
