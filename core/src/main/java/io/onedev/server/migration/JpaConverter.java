package io.onedev.server.migration;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.persistence.ManyToOne;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Element;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.proxy.HibernateProxy;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import io.onedev.launcher.loader.AppLoader;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.utils.ClassUtils;
import io.onedev.utils.ExceptionUtils;
import io.onedev.utils.ReflectionUtils;
import io.onedev.utils.StringUtils;

@SuppressWarnings({"rawtypes", "unchecked"})
public class JpaConverter extends ReflectionConverter {

	private ThreadLocal<Stack<HierarchicalStreamReader>> readerStack = 
		new ThreadLocal<Stack<HierarchicalStreamReader>>() {
		
		@Override
		protected Stack<HierarchicalStreamReader> initialValue() {
			return new Stack<HierarchicalStreamReader>();
		}

	};

	private ThreadLocal<Stack<HierarchicalStreamWriter>> writerStack = 
		new ThreadLocal<Stack<HierarchicalStreamWriter>>() {

			@Override
			protected Stack<HierarchicalStreamWriter> initialValue() {
				return new Stack<HierarchicalStreamWriter>();
			}
		
	};
	
	public JpaConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
		super(mapper, reflectionProvider);
	}
	
	@Override
	public void marshal(Object original, HierarchicalStreamWriter writer, MarshallingContext context) {
		try {
			writerStack.get().push(writer);
			if (original instanceof HibernateProxy)
				original = ((HibernateProxy)original).getHibernateLazyInitializer().getImplementation();
			super.marshal(original, writer, context);
		} finally {
			writerStack.get().pop();
		}
	}

	@Override
	protected void marshallField(MarshallingContext context, Object newObj, Field field) {
		HierarchicalStreamWriter writer = writerStack.get().peek();
		if (field.getAnnotation(ManyToOne.class) != null) {
			super.marshallField(context, ((AbstractEntity) newObj).getId(), field);
		} else if (newObj instanceof HibernateProxy) {
			newObj = ((HibernateProxy)newObj).getHibernateLazyInitializer().getImplementation();
			super.marshallField(context, newObj, field);
		} else if (newObj instanceof VersionedDocument) {
			marshallElement(writer, ((VersionedDocument)newObj).getRootElement());
		} else if (Collection.class.isAssignableFrom(field.getType()) 
				&& ReflectionUtils.getCollectionElementType(field.getGenericType()) == VersionedDocument.class) {
			for (VersionedDocument vdom: (List<VersionedDocument>)newObj) {
				if (vdom != null) {
					marshallElement(writer, vdom.getRootElement());
				} else {
					writer.startNode(mapper.serializedClass(null));
					writer.endNode();
				}
			}
		} else if (Map.class.isAssignableFrom(field.getType()) 
				&& ReflectionUtils.getMapValueType(field.getGenericType()) == VersionedDocument.class) {
			for (Map.Entry<Object, VersionedDocument> entry: 
					((Map<Object, VersionedDocument>)newObj).entrySet()) {
				writer.startNode(mapper.serializedClass(Map.Entry.class));
				if (entry.getKey() != null) {
					writer.startNode(mapper.serializedClass(entry.getKey().getClass()));
					context.convertAnother(entry.getKey());
					writer.endNode();
				} else {
					writer.startNode(mapper.serializedClass(null));
					writer.endNode();
				}
				if (entry.getValue() != null) {
					marshallElement(writer, entry.getValue().getRootElement());
				} else {
					writer.startNode(mapper.serializedClass(null));
					writer.endNode();
				}
				writer.endNode();
			}
		} else if (field.getType().isEnum()) {
			writer.setValue(((Enum)newObj).name());
		} else if (field.getType() == String.class) {
			String stringValue = (String) newObj;
			if (stringValue.indexOf('\0') != -1)
				writer.setValue(StringUtils.replace(stringValue, "\0", ""));
			else 
				writer.setValue(stringValue);
		} else {
			super.marshallField(context, newObj, field);
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		try {
			readerStack.get().push(reader);
			return super.unmarshal(reader, context);
		} finally {
			readerStack.get().pop();
		}
	}

	@Override
	protected Object unmarshallField(UnmarshallingContext context,
			Object result, Class type, Field field) {
		HierarchicalStreamReader reader = readerStack.get().peek();
		if (field.getAnnotation(ManyToOne.class) != null) {
			return AppLoader.getInstance(Dao.class).load(
					(Class<? extends AbstractEntity>) field.getType(), 
					Long.valueOf(reader.getValue()));
		} else if (field.getType() == VersionedDocument.class) {
			VersionedDocument vdom = new VersionedDocument();
			reader.moveDown();
			unmarshallElement(reader, vdom);
			reader.moveUp();
			return vdom;
		} else if (Collection.class.isAssignableFrom(field.getType()) 
				&& ReflectionUtils.getCollectionElementType(field.getGenericType()) == VersionedDocument.class) {
			Collection collection;
			try {
				collection = (Collection) mapper.defaultImplementationOf(
						field.getType()).newInstance();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
			while (reader.hasMoreChildren()) {
				reader.moveDown();
				if (!reader.getNodeName().equals("null")) {
					VersionedDocument vdom = new VersionedDocument();
					unmarshallElement(reader, vdom);
					collection.add(vdom);
				} else {
					collection.add(null);
				}
				reader.moveUp();
			}
			return collection;
		} else if (Map.class.isAssignableFrom(field.getType()) 
				&& ReflectionUtils.getMapValueType(field.getGenericType()) == VersionedDocument.class) {
			Map map;
			try {
				map = (Map) mapper.defaultImplementationOf(field.getType()).newInstance();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
			while (reader.hasMoreChildren()) {
				reader.moveDown();
				reader.moveDown();
		        Class keyType = HierarchicalStreams.readClassType(reader, mapper);
		        Object key;
		        if (!reader.getNodeName().equals("null"))
		        	key = context.convertAnother(reader, keyType);
		        else
		        	key = null;
		        reader.moveUp();
		        reader.moveDown();
		        if (!reader.getNodeName().equals("null")) {
					VersionedDocument vdom = new VersionedDocument();
					unmarshallElement(reader, vdom);
					map.put(key, vdom);
		        } else {
		        	map.put(key, null);
		        }
				reader.moveUp();
				reader.moveUp();
			}
			return map;
		} else if (field.getType().isEnum()) {
			// process this separately since the default implementation 
			// adds enum ordinal besides name for polymorphic enums 
			// (for example Subscription.Condition)
			return Enum.valueOf((Class)field.getType(), reader.getValue());
		} else {
			return super.unmarshallField(context, result, type, field);
		}
	}
	
	public boolean canConvert(Class clazz) {
		return !ClassUtils.isSystemType(clazz) && clazz != PersistentBag.class;
	}
	
	private void marshallElement(HierarchicalStreamWriter writer, Element element) {
		writer.startNode(element.getName());
		for (Attribute attribute: (List<Attribute>)element.attributes())
			writer.addAttribute(attribute.getName(), attribute.getValue());
		if (element.getText().trim().length() != 0)
			writer.setValue(element.getText().trim());
		for (Element child: (List<Element>)element.elements())
			marshallElement(writer, child);
		writer.endNode();
	}
	
	private void unmarshallElement(HierarchicalStreamReader reader, Branch branch) {
		Element element = branch.addElement(reader.getNodeName());
		for (int i=0; i<reader.getAttributeCount(); i++) {
			String attributeName = reader.getAttributeName(i);
			String attributeValue = reader.getAttribute(i);
			element.addAttribute(attributeName, attributeValue);
		}
		if (StringUtils.isNotBlank(reader.getValue()))
			element.setText(reader.getValue().trim());
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			unmarshallElement(reader, element);
			reader.moveUp();
		}
	}
}
