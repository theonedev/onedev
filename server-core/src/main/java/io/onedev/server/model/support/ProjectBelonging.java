package io.onedev.server.model.support;

import java.io.Serial;

import jakarta.persistence.MappedSuperclass;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;

@MappedSuperclass
public abstract class ProjectBelonging extends AbstractEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	public abstract Project getProject();
		
}
