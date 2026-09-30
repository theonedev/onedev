package io.onedev.server.rest;

import static io.onedev.server.web.page.help.ApiHelpUtils.*;
import static io.onedev.server.web.page.help.ValueInfo.Origin.*;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import org.jsoup.Jsoup;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.page.help.ExampleProvider;
import io.onedev.server.web.page.help.JsonMember;
import io.onedev.server.web.page.help.ValueInfo.Origin;

/** Builds JSON help from the same metadata and examples as the help pages. */
public class ApiHelpJson {

	public static final String PATH = "/~api/help";

	private final Map<String, Object> definitions = new LinkedHashMap<>();

	public Map<String, Object> getResources(String serverUrl) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("title", "RESTful API Help");
		result.put("baseUrl", serverUrl + "/~api");
		result.put("authentication", "When anonymous access is disabled or lacks permission, authenticate with "
				+ "a user name and password (or access token) via the HTTP Basic authorization header.");
		result.put("usage", "Follow each resource's help URL to list its methods, then each method's help URL "
				+ "for parameters, request/response examples and schemas. Examples are illustrative; replace example values before calling the API.");
		result.put("resources", getResourceClasses().stream().map(this::resourceSummary).toList());
		return result;
	}

	public Map<String, Object> getResource(Class<?> resourceClass) {
		var result = resourceSummary(resourceClass);
		result.remove("id");
		result.remove("help");
		result.put("methods", getResourceMethods(resourceClass).stream()
				.map(method -> methodSummary(resourceClass, method)).toList());
		return result;
	}

	public Map<String, Object> getMethod(Class<?> resourceClass, Method method) {
		definitions.clear();
		var result = methodSummary(resourceClass, method);
		result.remove("help");
		List<Map<String, Object>> pathParams = new ArrayList<>();
		List<Map<String, Object>> queryParams = new ArrayList<>();
		for (var param: method.getParameters()) {
			if (param.isAnnotationPresent(PathParam.class))
				pathParams.add(parameter(method, param, true));
			else if (param.isAnnotationPresent(QueryParam.class))
				queryParams.add(parameter(method, param, false));
		}
		result.put("pathParameters", pathParams);
		result.put("queryParameters", queryParams);
		var bodyParam = getRequestBodyParam(method);
		result.put("requestBody", bodyParam != null
				? body(resourceClass, bodyParam.getParameterizedType(), bodyParam.getAnnotation(Api.class), getPostValueOrigin(method))
				: null);
		Map<String, Object> success = new LinkedHashMap<>();
		success.put("statusCode", 200);
		success.put("body", method.getReturnType() != Response.class && method.getReturnType() != void.class
				? body(resourceClass, method.getGenericReturnType(), method.getAnnotation(Api.class), READ_BODY) : null);
		if (method.getReturnType() == Long.class && bodyParam != null) {
			var createdType = bodyParam.getType();
			var entityCreate = createdType.getAnnotation(EntityCreate.class);
			if (entityCreate != null)
				createdType = entityCreate.value();
			if (AbstractEntity.class.isAssignableFrom(createdType))
				success.put("description", "Id of created " + WordUtils.uncamel(createdType.getSimpleName()).toLowerCase());
		}
		result.put("response", Map.of("success", success, "failure", Map.of(
				"statusCode", "Non-200 status indicating the error type", "contentType", MediaType.TEXT_PLAIN,
				"description", "Error detail")));
		if (!definitions.isEmpty())
			result.put("$defs", definitions);
		return result;
	}

	private Map<String, Object> resourceSummary(Class<?> resourceClass) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("id", resourceClass.getName());
		result.put("title", getResourceTitle(resourceClass));
		result.put("description", text(getResourceDescription(resourceClass)));
		result.put("path", resourceClass.getAnnotation(Path.class).value());
		result.put("help", PATH + "/" + resourceClass.getName());
		return result;
	}

	private Map<String, Object> methodSummary(Class<?> resourceClass, Method method) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("title", getMethodTitle(method));
		result.put("description", text(getMethodDescription(method)));
		result.put("httpMethod", getHttpMethod(method));
		result.put("path", "/~api" + getResourcePath(resourceClass, method));
		result.put("help", PATH + "/" + resourceClass.getName() + "/" + method.getName());
		return result;
	}

	private Map<String, Object> parameter(Method method, Parameter param, boolean path) {
		var name = path ? param.getAnnotation(PathParam.class).value() : param.getAnnotation(QueryParam.class).value();
		var origin = path ? PATH_PLACEHOLDER : QUERY_PARAM;
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("name", name);
		var description = getDescription(param);
		result.put("description", description != null ? text(description) : WordUtils.capitalize(WordUtils.uncamel(name)));
		result.put("required", path || ParamCheckFilter.isRequired(param));
		result.put("schema", schema(param.getParameterizedType(), origin));
		result.put("example", example(exampleValue(method.getDeclaringClass(), param.getParameterizedType(),
				param.getAnnotation(Api.class), origin), param.getParameterizedType(), origin));
		if (!path && Collection.class.isAssignableFrom(param.getType()))
			result.put("serialization", "Repeat the query parameter for each value");
		return result;
	}

	private Map<String, Object> body(Class<?> resourceClass, Type type, @Nullable Api api, Origin origin) {
		Map<String, Object> result = new LinkedHashMap<>();
		var clazz = ReflectionUtils.getClass(type);
		if (InputStream.class.isAssignableFrom(clazz) || StreamingOutput.class.isAssignableFrom(clazz)) {
			result.put("contentType", MediaType.APPLICATION_OCTET_STREAM);
		} else {
			result.put("contentType", MediaType.APPLICATION_JSON);
			try {
				result.put("example", example(exampleValue(resourceClass, type, api, origin), type, origin));
			} catch (RuntimeException e) {
				// Third-party types or contributed example providers may not support example generation.
				// Keep the operation discoverable, without passing off a partial example as a valid one.
				result.put("exampleUnavailable", "An example could not be generated from the API metadata.");
			}
			result.put("schema", schema(type, origin));
		}
		return result;
	}

	private Object exampleValue(Class<?> declaringClass, Type type, @Nullable Api api, Origin origin) {
		var value = new ExampleProvider(declaringClass, api).getExample();
		var clazz = ReflectionUtils.getClass(type);
		if (value instanceof String && (clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || clazz == Boolean.class))
			return OneDev.getInstance(ObjectMapper.class).convertValue(value, clazz);
		return value != null ? value : getExampleValue(type, origin);
	}

	// Serialize examples like ExampleValuePanel: omit input IDs, use association IDs,
	// preserve display order, and include the discriminator for abstract declared types.
	private Object example(@Nullable Object value, @Nullable Type type, Origin origin) {
		if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean
				|| value instanceof Date || value instanceof Enum)
			return OneDev.getInstance(ObjectMapper.class).valueToTree(value);
		if (value instanceof Collection) {
			var elementType = type != null ? ReflectionUtils.getCollectionElementType(type) : null;
			return ((Collection<?>) value).stream().map(it -> example(it, elementType, origin)).toList();
		}
		if (value.getClass().isArray()) {
			List<Object> values = new ArrayList<>();
			for (int i = 0; i < Array.getLength(value); i++)
				values.add(example(Array.get(value, i), value.getClass().getComponentType(), origin));
			return values;
		}
		Map<String, Object> result = new LinkedHashMap<>();
		if (value instanceof Map) {
			var valueType = type != null ? ReflectionUtils.getMapValueType(type) : null;
			((Map<?, ?>) value).forEach((key, item) -> result.put(key.toString(), example(item, valueType, origin)));
		} else {
			if (type != null && isPolymorphic(ReflectionUtils.getClass(type)))
				result.put("@type", value.getClass().getSimpleName());
			for (var member: members(value.getClass(), origin)) {
				var memberValue = member.getValue(value);
				if (isAssociation(member))
					result.put(member.getName() + "Id", memberValue != null ? ((AbstractEntity) memberValue).getId() : null);
				else
					result.put(member.getName(), example(memberValue, member.getGenericType(), origin));
			}
		}
		return result;
	}

	private Map<String, Object> schema(@Nullable Type type, Origin origin) {
		Map<String, Object> result = new LinkedHashMap<>();
		var clazz = type != null ? ReflectionUtils.getClass(type) : Object.class;
		if (clazz == null || clazz == Object.class || clazz == java.io.Serializable.class)
			return result;
		if (clazz == String.class || clazz == char.class || clazz == Character.class) {
			result.put("type", "string");
		} else if (clazz == boolean.class || clazz == Boolean.class) {
			result.put("type", "boolean");
		} else if (clazz.isPrimitive() || Number.class.isAssignableFrom(clazz)) {
			result.put("type", clazz == float.class || clazz == double.class || clazz == Float.class || clazz == Double.class
					? "number" : "integer");
		} else if (Date.class.isAssignableFrom(clazz)) {
			result.put("type", "string");
			result.put("format", "date-time");
		} else if (clazz.isEnum()) {
			result.put("type", "string");
			List<String> values = new ArrayList<>();
			for (var constant: clazz.getEnumConstants())
				values.add(((Enum<?>) constant).name());
			result.put("enum", values);
		} else if (Collection.class.isAssignableFrom(clazz) || clazz.isArray()) {
			result.put("type", "array");
			result.put("items", schema(clazz.isArray() ? clazz.getComponentType() : ReflectionUtils.getCollectionElementType(type), origin));
		} else if (Map.class.isAssignableFrom(clazz)) {
			result.put("type", "object");
			result.put("additionalProperties", schema(ReflectionUtils.getMapValueType(type), origin));
		} else if (clazz.getName().startsWith("java.")) {
			// Do not infer a JSON representation from private JDK implementation fields.
			result.put("javaType", clazz.getName());
		} else {
			var key = clazz.getName() + "." + origin.name();
			result.put("$ref", "#/$defs/" + key);
			if (!definitions.containsKey(key)) {
				Map<String, Object> definition = new LinkedHashMap<>();
				definitions.put(key, definition); // Register before visiting recursive members.
				var description = getDescription(clazz);
				if (description != null)
					definition.put("description", text(description));
				if (isPolymorphic(clazz)) {
					List<Object> alternatives = new ArrayList<>();
					for (var implementation: getImplementations(clazz)) {
						alternatives.add(Map.of("allOf", List.of(schema(implementation, origin), Map.of(
								"type", "object", "properties", Map.of("@type", Map.of("const", implementation.getSimpleName())),
								"required", List.of("@type")))));
					}
					definition.put("oneOf", alternatives);
				} else {
					definition.put("type", "object");
					Map<String, Object> properties = new LinkedHashMap<>();
					for (var member: members(clazz, origin)) {
						var property = schema(isAssociation(member) ? Long.class : member.getGenericType(), origin);
						var memberDescription = getDescription(member);
						if (memberDescription != null)
							property.put("description", text(memberDescription));
						properties.put(member.getName() + (isAssociation(member) ? "Id" : ""), property);
					}
					definition.put("properties", properties);
				}
			}
		}
		return result;
	}

	private List<JsonMember> members(Class<?> clazz, Origin origin) {
		return getJsonMembers(clazz, origin).stream()
				.filter(it -> origin != CREATE_BODY && origin != UPDATE_BODY || !it.isAnnotationPresent(Id.class)).toList();
	}

	private boolean isAssociation(JsonMember member) {
		return member.isAnnotationPresent(ManyToOne.class) || member.isAnnotationPresent(JoinColumn.class);
	}

	private boolean isPolymorphic(Class<?> clazz) {
		return Modifier.isAbstract(clazz.getModifiers()) && !clazz.getName().startsWith("java.")
				&& !clazz.getName().startsWith("javax.");
	}

	@Nullable
	private String text(@Nullable String html) {
		if (html == null)
			return null;
		var document = Jsoup.parseBodyFragment(html);
		for (var link: document.select("a[href]")) {
			var href = link.attr("href");
			link.appendText(" (" + href + ")");
		}
		return document.text();
	}

}
