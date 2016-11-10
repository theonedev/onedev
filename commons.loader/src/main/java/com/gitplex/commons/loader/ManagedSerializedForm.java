package com.gitplex.commons.loader;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class ManagedSerializedForm implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Class<?> managedClass;

	public ManagedSerializedForm(Class<?> managedClass) {
		this.managedClass = managedClass;
	}
	
	public Object readResolve() throws ObjectStreamException  {
		return AppLoader.getInstance(managedClass);
	}

}