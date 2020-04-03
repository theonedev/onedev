package io.onedev.server.util.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import io.onedev.server.migration.VersionedXmlDoc;

public class MapConverter extends com.thoughtworks.xstream.converters.collections.MapConverter {

	public MapConverter(Mapper mapper) {
		super(mapper);
	}

    public MapConverter(Mapper mapper, Class<?> type) {
    	super(mapper, type);
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
