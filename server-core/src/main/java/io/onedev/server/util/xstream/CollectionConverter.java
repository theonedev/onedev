package io.onedev.server.util.xstream;

import java.util.ArrayList;

import org.hibernate.collection.internal.PersistentBag;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import io.onedev.server.migration.VersionedXmlDoc;

public class CollectionConverter extends com.thoughtworks.xstream.converters.collections.CollectionConverter {

	public CollectionConverter(Mapper mapper) {
		super(mapper);
	}

    public CollectionConverter(Mapper mapper, Class<?> type) {
    	super(mapper, type);
    }
    
	@Override
	public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) {
		return super.canConvert(clazz) || clazz == PersistentBag.class;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		if (source instanceof PersistentBag)
			source = new ArrayList((PersistentBag)source);
		super.marshal(source, writer, context);
	}

	@Override
	protected void writeCompleteItem(Object item, MarshallingContext context, HierarchicalStreamWriter writer) {
		if (item instanceof VersionedXmlDoc) 
			((VersionedXmlDoc)item).marshall(writer);
		else 
			super.writeCompleteItem(item, context, writer);
	}

	@Override
	protected Object readCompleteItem(HierarchicalStreamReader reader, UnmarshallingContext context, Object current) {
		if (reader.getAttribute("revision") != null) 
			return VersionedXmlDoc.unmarshall(reader);
		else 
			return super.readCompleteItem(reader, context, current);
	}

}
