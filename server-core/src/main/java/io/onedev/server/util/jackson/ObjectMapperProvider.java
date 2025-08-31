package io.onedev.server.util.jackson;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.server.web.page.layout.AdministrationSettingContribution;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;
import io.onedev.server.web.page.project.setting.ProjectSettingContribution;

@Singleton
public class ObjectMapperProvider implements Provider<ObjectMapper> {

	private final Set<ObjectMapperConfigurator> configurators;

	private final Set<ProjectSettingContribution> projectSettingContributions;

	private final Set<AdministrationSettingContribution> administrationSettingContributions;

	private final ImplementationRegistry implementationRegistry;
	
	@Inject
	public ObjectMapperProvider(Set<ProjectSettingContribution> projectSettingContributions,
								Set<AdministrationSettingContribution> administrationSettingContributions,
								ImplementationRegistry implementationRegistry,
								Set<ObjectMapperConfigurator> configurators) {
		this.projectSettingContributions = projectSettingContributions;
		this.administrationSettingContributions = administrationSettingContributions;
		this.implementationRegistry = implementationRegistry;
		this.configurators = configurators;
	}
	
	@Override
	public ObjectMapper get() {
		JsonMapper.Builder builder = JsonMapper.builder();
		builder.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
        ObjectMapper mapper = builder.build();
        
        TypeResolverBuilder<?> typer = new StdTypeResolverBuilder() {

            @Override
            public TypeDeserializer buildTypeDeserializer(DeserializationConfig config,
                    JavaType baseType, Collection<NamedType> subtypes) {
                return useForType(baseType) ? super.buildTypeDeserializer(config, baseType, getSubTypes(baseType)) : null;
            }

            private Collection<NamedType> getSubTypes(JavaType baseType) {
				if (baseType.getRawClass() == ContributedProjectSetting.class) {
					List<NamedType> subTypes = new ArrayList<>();
					for (var contribution: projectSettingContributions) {
						for (var settingClass: contribution.getSettingClasses())
							subTypes.add(new NamedType(settingClass));
					}
					return subTypes;
				} else if (baseType.getRawClass() == ContributedAdministrationSetting.class) {
					List<NamedType> subTypes = new ArrayList<>();
					for (var contribution: administrationSettingContributions) {
						for (var settingClass: contribution.getSettingClasses())
							subTypes.add(new NamedType(settingClass));
					}
					return subTypes;
				}
            	return implementationRegistry.getImplementations(baseType.getRawClass())
            			.stream()
            			.map(it->new NamedType(it))
            			.collect(Collectors.toList());
            }
            
            @Override
            public TypeSerializer buildTypeSerializer(SerializationConfig config,
                    JavaType baseType, Collection<NamedType> subtypes) {
                return useForType(baseType) ? super.buildTypeSerializer(config, baseType, getSubTypes(baseType)) : null;            
            }
        	
			private boolean useForType(JavaType t) {
				return  !Collection.class.isAssignableFrom(t.getRawClass()) 
						&& !Map.class.isAssignableFrom(t.getRawClass()) 
						&& t.getRawClass() != JsonNode.class
						&& t.getRawClass() != Object.class
						&& t.getRawClass() != Serializable.class
						&& !Enum.class.isAssignableFrom(t.getRawClass())
						&& !t.isConcrete();				
			}

        };
        typer = typer.init(JsonTypeInfo.Id.NAME, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        mapper.setDefaultTyping(typer);
        mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);	
		
		SimpleModule emptyStringModule = new SimpleModule();
		emptyStringModule.addDeserializer(String.class, new JsonDeserializer<String>() {

			@Override
			public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
				String value = p.getValueAsString();
				return (value != null && value.trim().isEmpty()) ? null : value;
			}
		
		});
		mapper.registerModule(emptyStringModule);
		
		for (ObjectMapperConfigurator each: configurators)
			each.configure(mapper);
		
		return mapper;
	}

}
