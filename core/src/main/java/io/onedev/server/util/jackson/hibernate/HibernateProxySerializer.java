package io.onedev.server.util.jackson.hibernate;

import java.io.IOException;

import org.hibernate.proxy.HibernateProxy;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;

/**
 * Serializer to use for values proxied using {@link HibernateProxy}.
 * <p>
 * TODO: should try to make this work more like Jackson
 * <code>BeanPropertyWriter</code>, possibly sub-classing it -- it handles much
 * of functionality we need, and has access to more information than value
 * serializers (like this one) have.
 */
public class HibernateProxySerializer extends JsonSerializer<HibernateProxy> {

	protected PropertySerializerMap _dynamicSerializers = PropertySerializerMap.emptyForProperties();

	// since 2.3
	@Override
	public boolean isEmpty(HibernateProxy value) {
		return (value == null) || (findProxied(value) == null);
	}

	@Override
	public void serialize(HibernateProxy value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		Object proxiedValue = findProxied(value);
		// TODO: figure out how to suppress nulls, if necessary? (too late for
		// that here)
		if (proxiedValue == null) {
			provider.defaultSerializeNull(jgen);
			return;
		}
		findSerializer(provider, proxiedValue).serialize(proxiedValue, jgen,
				provider);
	}

	@Override
	public void serializeWithType(HibernateProxy value, JsonGenerator jgen,
			SerializerProvider provider, TypeSerializer typeSer)
			throws IOException, JsonProcessingException {
		Object proxiedValue = findProxied(value);
		if (proxiedValue == null) {
			provider.defaultSerializeNull(jgen);
			return;
		}
		/*
		 * This isn't exactly right, since type serializer really refers to
		 * proxy object, not value. And we really don't either know static type
		 * (necessary to know how to apply additional type info) or other
		 * things; so it's not going to work well. But... we'll do out best.
		 */
		findSerializer(provider, proxiedValue).serializeWithType(proxiedValue,
				jgen, provider, typeSer);
	}

	/*
	 * /**********************************************************************
	 * /* Helper methods
	 * /**********************************************************************
	 */

	protected JsonSerializer<Object> findSerializer(
			SerializerProvider provider, Object value) throws IOException,
			JsonProcessingException {
		/*
		 * TODO: if Hibernate did use generics, or we wanted to allow use of
		 * Jackson annotations to indicate type, should take that into user.
		 */
		Class<?> type = value.getClass();
		/*
		 * we will use a map to contain serializers found so far, keyed by type:
		 * this avoids potentially costly lookup from global caches and/or
		 * construction of new serializers
		 */
		/*
		 * 18-Oct-2013, tatu: Whether this is for the primary property or
		 * secondary is really anyone's guess at this point; proxies can exist
		 * at any level?
		 */
		PropertySerializerMap.SerializerAndMapResult result = _dynamicSerializers
				.findAndAddPrimarySerializer(type, provider, null);
		if (_dynamicSerializers != result.map) {
			_dynamicSerializers = result.map;
		}
		return result.serializer;
	}

	/**
	 * Helper method for finding value being proxied, if it is available or if
	 * it is to be forced to be loaded.
	 */
	protected Object findProxied(HibernateProxy proxy) {
		return proxy.getHibernateLazyInitializer().getImplementation();
	}
}
