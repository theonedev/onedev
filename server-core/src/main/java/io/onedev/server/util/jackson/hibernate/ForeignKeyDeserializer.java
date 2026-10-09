package io.onedev.server.util.jackson.hibernate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.onedev.commons.loader.AppLoader;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.BaseAuthorization;
import io.onedev.server.rest.RestProjectUtils;
import io.onedev.server.persistence.dao.Dao;

import java.io.IOException;

public final class ForeignKeyDeserializer extends StdDeserializer<AbstractEntity> {

	public ForeignKeyDeserializer(Class<?> entityClass) {
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
        if (valueClass == Project.class && entityId < 0) {
            var owner = jp.currentValue();
            if (Project.DEFAULT_ID.equals(entityId) && (owner instanceof UserAuthorization
                    || owner instanceof GroupAuthorization || owner instanceof BaseAuthorization
                    || owner instanceof AccessTokenAuthorization)) {
                RestProjectUtils.checkProjectDefaultsPermission();
            } else {
                RestProjectUtils.checkProjectId(entityId);
            }
        }
        return (AbstractEntity) AppLoader.getInstance(Dao.class).load(valueClass, entityId);
    }
}
