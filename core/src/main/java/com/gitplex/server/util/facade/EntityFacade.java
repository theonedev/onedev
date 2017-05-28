package com.gitplex.server.util.facade;

import java.io.Serializable;

public class EntityFacade implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long id;

	public EntityFacade(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}

}
