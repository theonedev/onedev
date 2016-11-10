package com.gitplex.commons.hibernate.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gitplex.commons.hibernate.AbstractEntity;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.loader.AppLoader;

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