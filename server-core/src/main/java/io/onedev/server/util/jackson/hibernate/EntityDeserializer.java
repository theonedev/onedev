package io.onedev.server.util.jackson.hibernate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.SettableAnyProperty;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.PropertyValue;
import com.google.common.base.Preconditions;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.dao.Dao;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

@SuppressWarnings("serial")
public class EntityDeserializer extends BeanDeserializer {

	private static ThreadLocal<Stack<Object[]>> paramsStack = ThreadLocal.withInitial(() -> new Stack<Object[]>());
	
	private final Class<? extends AbstractEntity> entityClass;
	
	private final BeanDeserializer defaultDeserializer;
	
	private final Dao dao;
	
	public EntityDeserializer(
			Class<? extends AbstractEntity> entityClass, 
			BeanDeserializer defaultDeserializer, 
			Dao dao) {
		super(defaultDeserializer);
		
		this.entityClass = entityClass;
		this.defaultDeserializer = defaultDeserializer;
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
			entity.setOldVersion(entity.getFacade());

			Object bean;
			if (entity instanceof HibernateProxy)
				bean = ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
			else
				bean = entity;
			defaultDeserializer.deserialize(jp, ctxt, bean);
			return entity;
		}
		
		PropertyValue buffer = null;
        for (JsonToken t = jp.getCurrentToken(); t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
        	String propName = jp.getCurrentName();
        	jp.nextToken();
        	SettableBeanProperty property = _beanProperties.find(propName);
        	if (property != null) {
        		Object value = property.deserialize(jp, ctxt);
            	if (property.getName().equals(AbstractEntity.PROP_ID) && value != null) {
        			jp.nextToken();
        			AbstractEntity entity = dao.load(entityClass, (Long)value);
        			entity.setOldVersion(entity.getFacade());
        			
        			Object bean;
        			if (entity instanceof HibernateProxy) 
        				bean = ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        			else 
        				bean = entity;
        	        for (PropertyValue pv = buffer; pv != null; pv = pv.next)
        	            pv.assign(bean);
        	        defaultDeserializer.deserialize(jp, ctxt, bean);
        	        return entity;
            	} else {
            		buffer = new RegularPropertyValue(buffer, value, property);
            	}
            	continue;
        	} 
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                handleIgnoredProperty(jp, ctxt, handledType(), propName);
                continue;
            }
            if (_anySetter != null) {
                buffer = new AnyPropertyValue(buffer, _anySetter.deserialize(jp, ctxt), _anySetter, propName);
                continue;
            }
        	
        	ctxt.handleUnexpectedToken(entityClass, jp);
        }

        // reach end of object
        AbstractEntity entity;
    	try {
			entity = entityClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
        for (PropertyValue pv = buffer; pv != null; pv = pv.next)
            pv.assign(entity);
        
        return entity;
	}

    private static class RegularPropertyValue extends PropertyValue {
    
    	private final SettableBeanProperty _property;
    
    	public RegularPropertyValue(PropertyValue next, Object value, SettableBeanProperty prop) {
    		super(next, value);
    		_property = prop;
    	}

	    @Override
	    public void assign(Object bean) throws IOException, JsonProcessingException {
	        _property.set(bean, value);
	    }
	}

    private static class AnyPropertyValue extends PropertyValue {
    	
    	private final SettableAnyProperty _property;
    	
    	private final String _propertyName;
    
    	public AnyPropertyValue(PropertyValue next, Object value, SettableAnyProperty prop, String propName) {
    		super(next, value);
    		_property = prop;
    		_propertyName = propName;
    	}

	    @Override
	    public void assign(Object bean) throws IOException, JsonProcessingException {
	        _property.set(bean, _propertyName, value);
	    }
    }
}
