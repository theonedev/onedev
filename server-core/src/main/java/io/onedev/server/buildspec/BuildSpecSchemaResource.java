package io.onedev.server.buildspec;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.commons.utils.ClassUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ImplementationProvider;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.annotation.RetryCondition;
import io.onedev.server.annotation.UserMatch;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.data.migration.MigrationHelper;
import io.onedev.server.model.support.build.JobProperty;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.util.Pair;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;

@Api(internal = true)
@Path("/build-spec-schema.yml")
@Consumes(MediaType.APPLICATION_JSON)
@Produces("application/x-yaml")
@Singleton
public class BuildSpecSchemaResource {

    private final ImplementationRegistry implementationRegistry;
    
    private volatile String schema;

    @Inject
    public BuildSpecSchemaResource(ImplementationRegistry implementationRegistry) {
        this.implementationRegistry = implementationRegistry;        
    }

    private void processProperty(Map<String, Object> currentNode, Object bean, PropertyDescriptor property) {
        var descriptionSections = new ArrayList<String>();
        var descriptionSection = property.getDescription();
        if (descriptionSection != null)
            descriptionSections.add(descriptionSection);

        var getter = property.getPropertyGetter();
        var returnType = property.getPropertyClass();

        if (returnType == String.class) {
            if (getter.getAnnotation(Code.class) == null && getter.getAnnotation(Multiline.class) == null) {
                descriptionSections.add("NOTE: If set, the value can only contain one line");
            }
            InputStream grammarStream = null;
            try {
                if (getter.getAnnotation(Patterns.class) != null) {
                    if (getter.getAnnotation(Interpolative.class) != null) {
                        grammarStream = PatternSet.class.getResourceAsStream("InterpolativePatternSet.g4");
                    } else {
                        grammarStream = PatternSet.class.getResourceAsStream("PatternSet.g4");
                    }
                } else if (getter.getAnnotation(RetryCondition.class) != null) {
                    grammarStream = io.onedev.server.buildspec.job.retrycondition.RetryCondition.class.getResourceAsStream("RetryCondition.g4");
                } else if (getter.getAnnotation(UserMatch.class) != null) {
                    grammarStream = io.onedev.server.util.usermatch.UserMatch.class.getResourceAsStream("UserMatch.g4");
                }
                if (grammarStream != null) {
                    try {
                        var grammar = IOUtils.toString(grammarStream, StandardCharsets.UTF_8);
                        descriptionSections.add("NOTE: If set, the value should conform with below ANTLR v4 grammar:\n\n" + grammar);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } finally {
                IOUtils.closeQuietly(grammarStream);
            }
        }

        if (descriptionSections.size() != 0)
            currentNode.put("description", StringUtils.join(descriptionSections, "\n\n"));

        Class<?> elementClass = null;
        if (Collection.class.isAssignableFrom(returnType)) {
            elementClass = ReflectionUtils.getCollectionElementClass(property.getPropertyGetter().getGenericReturnType());
            if (elementClass == null)
                throw new ExplicitException("Unknown collection element class (bean: " + property.getBeanClass() + ", property: " + property.getPropertyName() + ")");
            processCollectionProperty(currentNode, elementClass);
        } else {
            processType(currentNode, returnType);
        }

        Object defaultValue;
        var value = property.getPropertyValue(bean);
        if (value instanceof Integer) {
            var intValue = (Integer) value;
            if (intValue == 0)
                defaultValue = null;
            else
                defaultValue = intValue;
        } else if (value instanceof Long) {
            var longValue = (Long) value;
            if (longValue == 0)
                defaultValue = null;
            else
                defaultValue = longValue;
        } else if (value instanceof Double) {
            var doubleValue = (Double) value;
            if (doubleValue == 0)
                defaultValue = null;
            else
                defaultValue = doubleValue;
        } else if (value instanceof Float) {
            var floatValue = (Float) value;
            if (floatValue == 0)
                defaultValue = null;
            else
                defaultValue = floatValue;
        } else if (value instanceof Boolean) {
            var booleanValue = (Boolean) value;
            if (!booleanValue)
                defaultValue = null;
            else
                defaultValue = booleanValue;
        } else if (value instanceof Enum) {
            var enumValue = (Enum<?>) value;
            defaultValue = enumValue.name();
        } else if (value instanceof String || value instanceof Date) {
            defaultValue = value;
        } else {
            defaultValue = null;
        }
        if (defaultValue != null) {
            currentNode.put("default", defaultValue);
        }
    }

    private Object newBean(Class<?> beanClass) {
        try {
            return beanClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void processBean(Map<String, Object> currentNode, Class<?> beanClass, 
            Collection<Class<?>> implementations, Set<String> processedProperties) {
        var propsNode = (Map<String, Object>) currentNode.get("properties");
        if (propsNode == null) {
            propsNode = new HashMap<>();
        }
        var requiredNode = (List<String>) currentNode.get("required");
        if (requiredNode == null) {
            requiredNode = new ArrayList<>();
        }

        var beanDescriptor = new BeanDescriptor(beanClass); 

        var propertyMap = new HashMap<String, PropertyDescriptor>();
        for (var groupProperties: beanDescriptor.getProperties().values()) {
            for (var property: groupProperties) {
                propertyMap.put(property.getPropertyName(), property);
            }
        }

        /*
         * As long as one implementation overrides a property in base class, we will exclude the property from 
         * common property section, and add it individually to each implementation's property section to avoid 
         * defining same property both in common section and oneOf section
         */
        var excludedProperties = new HashSet<String>();
        Map<String, byte[]> valueBytesMap = new HashMap<>();
        Object bean = null;
        for (var implementation: implementations) {
            bean = newBean(implementation);
            for (var groupProperties: new BeanDescriptor(implementation).getProperties().values()) {
                for (var property: groupProperties) {
                    if (!excludedProperties.contains(property.getPropertyName()) 
                            && propertyMap.containsKey(property.getPropertyName()) 
                            && property.getBeanClass() != beanClass) {
                        excludedProperties.add(property.getPropertyName());
                    }
                    if (!excludedProperties.contains(property.getPropertyName())) {
                        var value = property.getPropertyValue(bean);
                        var lastValueBytes = valueBytesMap.get(property.getPropertyName());
                        if (lastValueBytes == null) {
                            lastValueBytes = SerializationUtils.serialize((Serializable) value);
                            valueBytesMap.put(property.getPropertyName(), lastValueBytes);
                        } else if (!Arrays.equals(lastValueBytes, SerializationUtils.serialize((Serializable) value))) {
                            excludedProperties.add(property.getPropertyName());
                        }
                    }
                }
            }                        
        }
        if (bean == null) {
            bean = newBean(beanClass);
        }
        
        var dependents = new ArrayList<Pair<PropertyDescriptor, DependsOn>>();
        for (var groupProperties: beanDescriptor.getProperties().values()) {
            for (var property: groupProperties) {
                if (!excludedProperties.contains(property.getPropertyName()) && processedProperties.add(property.getPropertyName())) {
                    if (property.getPropertyName().equals("type"))
                        throw new ExplicitException("Property 'type' is reserved (class: " + beanClass.getName() + ")");
                    var dependsOn = property.getPropertyGetter().getAnnotation(DependsOn.class);
                    if (dependsOn != null) {
                        dependents.add(new Pair<>(property, dependsOn));
                    } else {
                        if (property.isPropertyRequired())
                            requiredNode.add(property.getPropertyName()); 
                        var propNode = new HashMap<String, Object>();
                        propsNode.put(property.getPropertyName(), propNode);
                        processProperty(propNode, bean, property);
                    }
                }
            }
        }
        if (!dependents.isEmpty()) {
            var allOfNode = new ArrayList<Map<String, Object>>();
            currentNode.put("allOf", allOfNode);
            for (var dependent: dependents) {
                var allOfItemNode = new HashMap<String, Object>();
                allOfNode.add(allOfItemNode);
                var dependsOn = dependent.getRight();
                var dependencyProperty = propertyMap.get(dependsOn.property());
                if (dependencyProperty == null) 
                    throw new ExplicitException("Dependency property not found: " + dependsOn.property());
                
                var ifNode = new HashMap<String, Object>();
                allOfItemNode.put("if", ifNode);
                var ifPropsNode = new HashMap<String, Object>();
                ifNode.put("properties", ifPropsNode);
                var dependencyPropertyNode = new HashMap<String, Object>();
                ifPropsNode.put(dependencyProperty.getPropertyName(), dependencyPropertyNode);
                
                var inverse = dependsOn.inverse();
                var dependencyPropertyClass = dependencyProperty.getPropertyClass();
                if (dependencyPropertyClass == boolean.class) {
                    if (dependsOn.value().length() != 0) {
                        dependencyPropertyNode.put("const", Boolean.parseBoolean(dependsOn.value()));
                    } else {
                        dependencyPropertyNode.put("const", true);
                    }
                } else if (dependencyPropertyClass == int.class || dependencyPropertyClass == long.class 
                        || dependencyPropertyClass == double.class || dependencyPropertyClass == float.class) {
                    if (dependsOn.value().length() != 0) {
                        dependencyPropertyNode.put("const", Integer.parseInt(dependsOn.value()));
                    } else {
                        dependencyPropertyNode.put("const", 0);
                        inverse = !inverse;
                    }
                } else {
                    if (dependsOn.value().length() != 0) {
                        if (dependencyPropertyClass == Boolean.class) 
                            dependencyPropertyNode.put("const", Boolean.parseBoolean(dependsOn.value()));
                        else if (dependencyPropertyClass == Integer.class || dependencyPropertyClass == Long.class || dependencyPropertyClass == Double.class || dependencyPropertyClass == Float.class) 
                            dependencyPropertyNode.put("const", Integer.parseInt(dependsOn.value()));
                        else
                            dependencyPropertyNode.put("const", dependsOn.value());
                    } else {
                        var typeList = new ArrayList<String>();
                        typeList.add("object");
                        typeList.add("string");
                        typeList.add("integer");
                        typeList.add("number");
                        typeList.add("boolean");
                        dependencyPropertyNode.put("type", typeList);
                    }
                }

                Map<String, Object> branchNode;
                if (!inverse) {
                    branchNode = new HashMap<>();
                    allOfItemNode.put("then", branchNode);
                } else {
                    branchNode = new HashMap<>();
                    allOfItemNode.put("else", branchNode);
                }

                var property = dependent.getLeft();
                var branchPropsNode = new HashMap<String, Object>();
                branchNode.put("properties", branchPropsNode);
                var propNode = new HashMap<String, Object>();
                branchPropsNode.put(property.getPropertyName(), propNode);
                processProperty(propNode, bean, property);
                if (property.isPropertyRequired()) {
                    var requiredList = new ArrayList<String>();
                    requiredList.add(property.getPropertyName());
                    branchNode.put("required", requiredList);
                }
            }
        }

        if (!propsNode.isEmpty())
            currentNode.put("properties", propsNode);
        if (!requiredNode.isEmpty())
            currentNode.put("required", requiredNode);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void processType(Map<String, Object> currentNode, Class<?> type) {
        if (type == String.class) {
            currentNode.put("type", "string");
        } else if (type == Boolean.class || type == boolean.class) {
            currentNode.put("type", "boolean");
        } else if (type == Integer.class || type == int.class || type == Long.class || type == long.class) {
            currentNode.put("type", "integer");
        } else if (type == Float.class || type == float.class || type == Double.class || type == double.class) {
            currentNode.put("type", "number");   
        } else if (Enum.class.isAssignableFrom(type)) {
            currentNode.put("type", "string");
            var enumList = new ArrayList<String>();
            var enumClass = (Class<Enum>) type;
            for (var enumValue: EnumSet.allOf(enumClass)) {
                enumList.add(((Enum) enumValue).name());
            }
            currentNode.put("enum", enumList);
        } else if (type == Date.class) {
            currentNode.put("type", "string");
            currentNode.put("format", "date-time");
        } else if (type.getAnnotation(Editable.class) != null) {
            if (ClassUtils.isConcrete(type)) {
                currentNode.put("type", "object");
                processBean(currentNode, type, new ArrayList<>(), new HashSet<>());
                currentNode.put("additionalProperties", false);    
            } else {
                processPolymorphic(currentNode, type);
            }
        } else {
            throw new ExplicitException("Unsupported type: " + type);
        }    
    }

    @SuppressWarnings("unchecked")
    private void processPolymorphic(Map<String, Object> currentNode, Class<?> baseClass) {
        Collection<Class<?>> implementations = new ArrayList<>();
        var implementationProvider = baseClass.getAnnotation(ImplementationProvider.class);
        if (implementationProvider != null) 
            implementations.addAll((Collection<? extends Class<? extends Serializable>>) ReflectionUtils.invokeStaticMethod(baseClass, implementationProvider.value()));
        else 
            implementations.addAll(implementationRegistry.getImplementations(baseClass));

        currentNode.put("type", "object");
        
        var propsNode = new HashMap<String, Object>();
        var typeNode = new HashMap<String, Object>();
        typeNode.put("type", "string");
        propsNode.put("type", typeNode);
        currentNode.put("properties", propsNode);
        
        var enumList = new ArrayList<String>();
        typeNode.put("enum", enumList);
        
        var requiredList = new ArrayList<String>();
        requiredList.add("type");
        currentNode.put("required", requiredList);

        var processedProperties = new HashSet<String>();
        processBean(currentNode, baseClass, implementations, processedProperties);
        
        var oneOfList = new ArrayList<Map<String, Object>>();
        currentNode.put("oneOf", oneOfList);        
        
        for (var implementation: implementations) {
            enumList.add(implementation.getSimpleName());
            var oneOfItemNode = new HashMap<String, Object>();
            var oneOfItemPropsNode = new HashMap<String, Object>();
            var typeConstNode = new HashMap<String, Object>();
            typeConstNode.put("const", implementation.getSimpleName());
            oneOfItemPropsNode.put("type", typeConstNode);
            oneOfItemNode.put("properties", oneOfItemPropsNode);
            var description = EditableUtils.getDescription(implementation);
            if (description != null)
                oneOfItemNode.put("description", description);                 
            processBean(oneOfItemNode, implementation, new ArrayList<>(), new HashSet<>(processedProperties));
            oneOfList.add(oneOfItemNode);
        }
    }

    private void processCollectionProperty(Map<String, Object> currentNode, Class<?> collectionElementClass) {
        currentNode.put("type", "array");
        var itemsNode = new HashMap<String, Object>();
        currentNode.put("items", itemsNode);
        if (Collection.class.isAssignableFrom(collectionElementClass)) {
            itemsNode.put("type", "array");
            var nestedItemsNode = new HashMap<String, Object>();
            nestedItemsNode.put("type", "string");
            itemsNode.put("items", nestedItemsNode);
        } else {                       
            processType(itemsNode, collectionElementClass);
        }
    }

    @GET
    @SuppressWarnings("unchecked")
    public String getBuildSpecSchema() {
        if (schema == null) {
            var rootNode = new HashMap<String, Object>();
            rootNode.put("$schema", "https://json-schema.org/draft/2020-12/schema");
            rootNode.put("title", "YAML schema of build spec file");
            rootNode.put("type", "object");    

            var propsNode = new HashMap<String, Object>();
            rootNode.put("properties", propsNode);
            
            var versionNode = new HashMap<String, Object>();
            propsNode.put("version", versionNode);
            versionNode.put("type", "integer");
            versionNode.put("const", Integer.parseInt(MigrationHelper.getVersion(BuildSpec.class)));

            var jobsNode = new HashMap<String, Object>();
            propsNode.put("jobs", jobsNode);
            processCollectionProperty(jobsNode, Job.class);
            
            var servicesNode = new HashMap<String, Object>();
            propsNode.put("services", servicesNode);
            processCollectionProperty(servicesNode, Service.class);
            
            var propertiesNode = new HashMap<String, Object>();
            propsNode.put("properties", propertiesNode);
            processCollectionProperty(propertiesNode, JobProperty.class);
            
            var importsNode = new HashMap<String, Object>();
            propsNode.put("imports", importsNode);
            processCollectionProperty(importsNode, Import.class);
            
            var jobPropsNode = (Map<String, Object>) ((Map<String, Object>) jobsNode.get("items")).get("properties");
            
            var stepTemplatesNode = new HashMap<String, Object>();
            propsNode.put("stepTemplates", stepTemplatesNode);
            stepTemplatesNode.put("type", "array");
            var stepTemplateNode = new HashMap<String, Object>();
            stepTemplatesNode.put("items", stepTemplateNode);
            stepTemplateNode.put("type", "object");
            var stepTemplatePropsNode = new HashMap<String, Object>();
            stepTemplateNode.put("properties", stepTemplatePropsNode);
            stepTemplatePropsNode.put("steps", jobPropsNode.get("steps"));
            stepTemplatePropsNode.put("paramSpecs", jobPropsNode.get("paramSpecs"));

            var requiredList = new ArrayList<String>();
            requiredList.add("version");
            rootNode.put("required", requiredList);
            rootNode.put("additionalProperties", false);
 
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            schema = new Yaml(options).dump(rootNode);
        } 
        return schema;
    }

 }