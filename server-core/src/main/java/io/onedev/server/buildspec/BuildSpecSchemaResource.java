package io.onedev.server.buildspec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.commons.utils.ClassUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ImplementationProvider;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.data.migration.MigrationHelper;
import io.onedev.server.model.support.build.JobProperty;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.util.Pair;
import io.onedev.server.util.ReflectionUtils;
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
    
    private final Yaml yaml;

    private String schema;

    @Inject
    public BuildSpecSchemaResource(ImplementationRegistry implementationRegistry) {
        this.implementationRegistry = implementationRegistry;
        
        // Configure YAML output
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        this.yaml = new Yaml(options);
    }

    private void processProperty(Map<String, Object> currentNode, PropertyDescriptor property) {
        var description = property.getDescription();
        if (description != null)
            currentNode.put("description", description);
        var returnType = property.getPropertyClass();

        if (Collection.class.isAssignableFrom(returnType)) {
            var elementClass = ReflectionUtils.getCollectionElementClass(property.getPropertyGetter().getGenericReturnType());
            if (elementClass == null)
                throw new ExplicitException("Unknown collection element class (bean: " + property.getBeanClass() + ", property: " + property.getPropertyName() + ")");
            processCollectionProperty(currentNode, elementClass);
        } else {
            processType(currentNode, returnType);
        }
    }

    @SuppressWarnings("unchecked")
    private void processBean(Map<String, Object> currentNode, Class<?> beanClass, 
            Map<String, PropertyDescriptor> processedProperties) {
        var propsNode = (Map<String, Object>) currentNode.get("properties");
        if (propsNode == null) {
            propsNode = new HashMap<>();
            currentNode.put("properties", propsNode);
        }
        var requiredNode = (List<String>) currentNode.get("required");
        if (requiredNode == null) {
            requiredNode = new ArrayList<>();
            currentNode.put("required", requiredNode);
        }
        
        var dependents = new ArrayList<Pair<PropertyDescriptor, DependsOn>>();
        for (var groupProperties: new BeanDescriptor(beanClass).getProperties().values()) {
            for (var property: groupProperties) {
                if (processedProperties.putIfAbsent(property.getPropertyName(), property) == null) {
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
                        processProperty(propNode, property);
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
                var dependencyProperty = processedProperties.get(dependsOn.property());
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
                processProperty(propNode, property);
                if (property.isPropertyRequired()) {
                    var requiredList = new ArrayList<String>();
                    requiredList.add(property.getPropertyName());
                    branchNode.put("required", requiredList);
                }
            }
        }
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
                processBean(currentNode, type, new HashMap<>());
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
        currentNode.put("properties", propsNode);
        var typeNode = new HashMap<String, Object>();
        propsNode.put("type", typeNode);
        typeNode.put("type", "string");
        
        var enumList = new ArrayList<String>();
        typeNode.put("enum", enumList);
        
        var requiredList = new ArrayList<String>();
        requiredList.add("type");
        currentNode.put("required", requiredList);

        var processedProperties = new HashMap<String, PropertyDescriptor>();
        processBean(currentNode, baseClass, processedProperties);
        
        var oneOfList = new ArrayList<Map<String, Object>>();
        currentNode.put("oneOf", oneOfList);        
        
        for (var implementation: implementations) {
            enumList.add(implementation.getSimpleName());
            var oneOfItemNode = new HashMap<String, Object>();
            oneOfList.add(oneOfItemNode);
            var description = EditableUtils.getDescription(implementation);
            if (description != null)
                oneOfItemNode.put("description", description);                 
            processBean(oneOfItemNode, implementation, new HashMap<>(processedProperties));
            var implementationPropsNode = (Map<String, Object>) oneOfItemNode.get("properties");
            var typeConstNode = new HashMap<String, Object>();
            typeConstNode.put("const", implementation.getSimpleName());
            implementationPropsNode.put("type", typeConstNode);
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

    @Path("/")
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

            schema = yaml.dump(rootNode);
        }
        return schema;
    }

 }