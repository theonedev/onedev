package io.onedev.server.model.support;

import javax.persistence.MappedSuperclass;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;

@MappedSuperclass
public abstract class ProjectBelonging extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public abstract Project getProject();
		
}
