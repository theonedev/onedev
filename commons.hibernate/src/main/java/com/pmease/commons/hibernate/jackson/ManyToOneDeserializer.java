package com.pmease.commons.hibernate.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.loader.AppLoader;

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
        return (AbstractEntity) AppLoader.getInstance(GeneralDao.class).load(valueClass, entityId);
    }
}