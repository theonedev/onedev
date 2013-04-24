package com.pmease.commons.product;

public class GuicyInterfaceImpl implements GuicyInterface {

	@Override
	public String get() {
		return GuicyInterfaceImpl.class.getName();
	}

}
