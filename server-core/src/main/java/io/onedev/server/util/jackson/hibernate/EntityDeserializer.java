package io.onedev.server.util.jackson.hibernate;

import java.io.IOException;
import java.util.Stack;

import org.hibernate.proxy.HibernateProxy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.google.common.base.Preconditions;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.rest.annotation.Immutable;

public class EntityDeserializer extends BeanDeserializer {

	private static ThreadLocal<Stack<Object[]>> paramsStack = ThreadLocal.withInitial(() -> new Stack<Object[]>());
	
	private final Class<? extends AbstractEntity> entityClass;
	
	private final Dao dao;
	
	public EntityDeserializer(
			Class<? extends AbstractEntity> entityClass, 
			BeanDeserializer beanDeserializer, 
			Dao dao) {
		super(beanDeserializer);
		this.entityClass = entityClass;
		this.dao = dao;
	}
	
	public static void pushParams(Object[] params) {
		paramsStack.get().push(params);
	}

	public static void popParams() {
		paramsStack.get().pop();
	}

	@Override
	public AbstractEntity deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		Preconditions.checkState(jp.getCurrentToken() == JsonToken.START_OBJECT);
		jp.nextToken();
		
		if (!paramsStack.get().isEmpty() 
				&& paramsStack.get().peek().length != 0 
				&& paramsStack.get().peek()[0] instanceof Long) {
			Long entityId = (Long) paramsStack.get().peek()[0];
			AbstractEntity entity = dao.load(entityClass, entityId);
			entity.setOldVersion(VersionedXmlDoc.fromBean(entity));

			Object bean;
			if (entity instanceof HibernateProxy)
				bean = ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
			else
				bean = entity;
			return (AbstractEntity) deserialize(jp, ctxt, bean);
		} else {
			return (AbstractEntity) super.deserialize(jp, ctxt);
		}
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt, Object bean) throws IOException {
		// [databind#631]: Assign current value, to be accessible by custom serializers
		p.setCurrentValue(bean);
		if (_injectables != null) {
			injectValues(ctxt, bean);
		}
		if (_unwrappedPropertyHandler != null) {
			return deserializeWithUnwrapped(p, ctxt, bean);
		}
		if (_externalTypeIdHandler != null) {
			return deserializeWithExternalTypeId(p, ctxt, bean);
		}
		String propName;

		// 23-Mar-2010, tatu: In some cases, we start with full JSON object too...
		if (p.isExpectedStartObjectToken()) {
			propName = p.nextFieldName();
			if (propName == null) {
				return bean;
			}
		} else {
			if (p.hasTokenId(JsonTokenId.ID_FIELD_NAME)) {
				propName = p.currentName();
			} else {
				return bean;
			}
		}
		if (_needViewProcesing) {
			Class<?> view = ctxt.getActiveView();
			if (view != null) {
				return deserializeWithView(p, ctxt, bean, view);
			}
		}
		do {
			p.nextToken();
			SettableBeanProperty prop = _beanProperties.find(propName);

			if (prop != null) { // normal case
				try {
					var propValue = prop.deserialize(p, ctxt);
					if (prop.getAnnotation(Immutable.class) == null)
						prop.set(bean, propValue);
				} catch (Exception e) {
					wrapAndThrow(e, bean, propName, ctxt);
				}
				continue;
			}
			handleUnknownVanilla(p, ctxt, bean, propName);
		} while ((propName = p.nextFieldName()) != null);
		return bean;
	}
	
}
