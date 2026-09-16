package io.onedev.server.persistence;

import java.util.EnumSet;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import io.onedev.commons.loader.AppLoader;

public class IdGenerator implements BeforeExecutionGenerator {

	private static final long serialVersionUID = 1L;

	@Override
	public Object generate(SharedSessionContractImplementor session, Object object,
			Object currentValue, EventType eventType) {
		return currentValue != null ? currentValue
				: AppLoader.getInstance(IdService.class).nextId(object.getClass());
	}

	@Override
	public EnumSet<EventType> getEventTypes() {
		return EventTypeSets.INSERT_ONLY;
	}

	@Override
	public boolean allowAssignedIdentifiers() {
		return true;
	}
}
