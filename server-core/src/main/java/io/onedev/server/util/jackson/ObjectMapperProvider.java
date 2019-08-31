package io.onedev.server.util.jackson;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

@Singleton
public class ObjectMapperProvider implements Provider<ObjectMapper> {

	private final Set<ObjectMapperConfigurator> configurators;
	
	@Inject
	public ObjectMapperProvider(Set<ObjectMapperConfigurator> configurators) {
		this.configurators = configurators;
	}
	
	@Override
	public ObjectMapper get() {
        ObjectMapper mapper = new ObjectMapper();
        
        TypeResolverBuilder<?> typer = new StdTypeResolverBuilder() {

            @Override
            public TypeDeserializer buildTypeDeserializer(DeserializationConfig config,
                    JavaType baseType, Collection<NamedType> subtypes) {
                return useForType(baseType) ? super.buildTypeDeserializer(config, baseType, subtypes) : null;
            }

            @Override
            public TypeSerializer buildTypeSerializer(SerializationConfig config,
                    JavaType baseType, Collection<NamedType> subtypes) {
                return useForType(baseType) ? super.buildTypeSerializer(config, baseType, subtypes) : null;            
            }
        	
			private boolean useForType(JavaType t) {
				return  !Collection.class.isAssignableFrom(t.getRawClass()) 
						&& !Map.class.isAssignableFrom(t.getRawClass()) 
						&& t.getRawClass() != JsonNode.class
						&& (t.getRawClass() == Object.class || !t.isConcrete());				
			}

        };
        typer = typer.init(JsonTypeInfo.Id.CLASS, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        mapper.setDefaultTyping(typer);
        
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);	
		
		for (ObjectMapperConfigurator each: configurators)
			each.configure(mapper);
		
		mapper.setConfig(mapper.getSerializationConfig().withView(DefaultView.class));
		
		return mapper;
	}

}
