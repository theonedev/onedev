package io.onedev.server.util.jackson.hibernate;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.dao.Dao;

@SuppressWarnings("serial")
public final class ManyToOneDeserializer extends StdDeserializer<AbstractEntity> {

	public ManyToOneDeserializer(Class<?> entityClass) {
		super(entityClass);
	}
	
	@Override
    public AbstractEntity getNullValue() {
        return null;
    }

    @SuppressWarnings("unchecked")
	@Override
    public AbstractEntity deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        Long entityId = jp.getLongValue();
        Class<? extends AbstractEntity> valueClass = (Class<? extends AbstractEntity>) handledType();
        return (AbstractEntity) AppLoader.getInstance(Dao.class).load(valueClass, entityId);
    }
}