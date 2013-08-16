package com.pmease.gitop.core.model.permission.object;

public class SystemObject implements ProtectedObject {

	@Override
	public boolean has(ProtectedObject object) {
		return true;
	}

}
