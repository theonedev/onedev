package io.onedev.server.util.xstream;

import org.hibernate.proxy.HibernateProxy;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/** Keeps generated proxy class names out of persistent XML documents. */
public class HibernateProxyMapper extends MapperWrapper {

	public HibernateProxyMapper(Mapper wrapped) {
		super(wrapped);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public String serializedClass(Class type) {
		if (type != null && HibernateProxy.class.isAssignableFrom(type))
			return super.serializedClass(type.getSuperclass());
		return super.serializedClass(type);
	}

}
