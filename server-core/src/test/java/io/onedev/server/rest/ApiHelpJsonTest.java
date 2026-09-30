package io.onedev.server.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import jakarta.persistence.ManyToOne;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;

import org.apache.shiro.authz.UnauthenticatedException;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.Immutable;
import io.onedev.server.rest.resource.ApiHelpResource;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.jackson.ObjectMapperProvider;

public class ApiHelpJsonTest extends AppLoaderMocker {

	private ObjectMapper mapper;

	@Override
	protected void setup() {
		var registry = mock(ImplementationRegistry.class);
		when(registry.getImplementations(Choice.class)).thenReturn(List.of(FirstChoice.class, SecondChoice.class));
		when(AppLoader.getInstance(ImplementationRegistry.class)).thenReturn(registry);
		mapper = new ObjectMapperProvider(Set.of(), Set.of(), registry, Set.of()).get();
		when(AppLoader.getInstance(ObjectMapper.class)).thenReturn(mapper);
		when(AppLoader.getInstance(ResourceConfig.class)).thenReturn(new ResourceConfig(
				SampleResource.class, AnotherResource.class, InternalResource.class, ApiHelpResource.class));
	}

	@Override
	protected void teardown() {
	}

	@Test
	public void preservesHierarchyAndDoesNotExposeInternalResourcesOrJavaMethods() {
		var help = new ApiHelpJson();
		JsonNode index = mapper.valueToTree(help.getResources("https://onedev.example.com"));
		assertEquals(2, index.path("resources").size());
		assertEquals("AAA", index.at("/resources/0/title").asText());
		assertEquals(SampleResource.class.getName(), index.at("/resources/1/id").asText());
		assertEquals(ApiHelpJson.PATH + "/" + SampleResource.class.getName(), index.at("/resources/1/help").asText());
		JsonNode resource = mapper.valueToTree(help.getResource(SampleResource.class));
		assertFalse(resource.has("id"));
		assertFalse(resource.has("help"));
		assertEquals(ApiHelpJson.PATH + "/" + SampleResource.class.getName() + "/create", resource.at("/methods/0/help").asText());
		assertEquals(ApiHelpJson.PATH + "/" + SampleResource.class.getName() + "/update", resource.at("/methods/1/help").asText());
		assertEquals("Create a record", resource.at("/methods/0/title").asText());
		assertFalse(resource.at("/methods/0").has("id"));
		var endpoint = new ApiHelpResource(null);
		assertThrows(NotFoundException.class, () -> endpoint.getResource(InternalResource.class.getName()));
		assertThrows(NotFoundException.class, () -> endpoint.getResource("java.lang.System"));
		assertThrows(NotFoundException.class, () -> endpoint.getMethod(SampleResource.class.getName(), "toString"));
		assertThrows(NotFoundException.class, () -> endpoint.getMethod(SampleResource.class.getName(), "missing"));
	}

	@Test
	public void matchesCreateAndUpdateBodyVisibilityAndAssociationIds() throws Exception {
		var create = method("create", TestEntity.class);
		var update = method("update", Long.class, TestEntity.class);
		var created = create.at("/requestBody/example");
		assertFalse(created.has("id"));
		assertFalse(created.has("readOnly"));
		assertFalse(created.has("secret"));
		assertTrue(created.has("immutable"));
		assertTrue(created.path("ownerId").isIntegralNumber());
		assertFalse(created.has("owner"));
		assertFalse(update.at("/requestBody/example").has("immutable"));
		assertEquals("Name & description", definition(create, TestEntity.class, "CREATE_BODY")
				.at("/properties/name/description").asText());
		assertFalse(definition(update, TestEntity.class, "UPDATE_BODY").path("properties").has("immutable"));
		assertTrue(update.at("/response/success/body").isNull());
		assertEquals("Id of created test entity", create.at("/response/success/description").asText());
		var read = method("read");
		assertTrue(read.at("/response/success/body/example").has("id"));
		assertTrue(read.at("/response/success/body/example").has("readOnly"));
	}

	@Test
	public void describesParametersAndUsesCustomExamplesAndPlainDescriptions() throws Exception {
		var detail = method("query", Long.class, String.class, List.class, int.class, UriInfo.class);
		assertFalse(detail.has("id"));
		assertFalse(detail.has("resource"));
		assertFalse(detail.has("resourceDescription"));
		assertFalse(detail.has("help"));
		assertFalse(detail.has("curlExample"));
		assertEquals("/~api/samples/{id}", detail.path("path").asText());
		assertEquals(42, detail.at("/pathParameters/0/example").asInt());
		assertEquals(3, detail.path("queryParameters").size());
		assertTrue(detail.at("/queryParameters/0/required").asBoolean());
		assertFalse(detail.at("/queryParameters/1/required").asBoolean());
		assertTrue(detail.at("/queryParameters/2/required").asBoolean());
		assertEquals("Find O'Reilly & friends", detail.at("/queryParameters/0/description").asText());
		assertEquals("O'Reilly & friends", detail.at("/queryParameters/0/example").asText());
		assertEquals("custom result", detail.at("/response/success/body/example").asText());
		assertTrue(detail.path("requestBody").isNull());
	}

	@Test
	public void documentsAllPolymorphicChoicesAndRecursiveTypes() throws Exception {
		var detail = method("choose", Choice.class);
		assertEquals("FirstChoice", detail.at("/requestBody/example/@type").asText());
		var choices = definition(detail, Choice.class, "CREATE_BODY").path("oneOf");
		assertEquals(2, choices.size());
		assertEquals("SecondChoice", choices.at("/1/allOf/1/properties/@type/const").asText());
		var first = definition(detail, FirstChoice.class, "CREATE_BODY");
		assertEquals(2, first.at("/properties/mode/enum").size());
		assertEquals("date-time", first.at("/properties/date/format").asText());
		assertEquals("#/$defs/" + FirstChoice.class.getName() + ".CREATE_BODY", first.at("/properties/next/$ref").asText());
		assertTrue(detail.at("/requestBody/example/next/next").isNull());
	}

	@Test
	public void representsBinaryAndAbsentBodiesWithoutTryingToGenerateExamples() throws Exception {
		var upload = method("upload", InputStream.class);
		assertEquals("application/octet-stream", upload.at("/requestBody/contentType").asText());
		assertFalse(upload.path("requestBody").has("example"));
		assertEquals("application/octet-stream", upload.at("/response/success/body/contentType").asText());
		assertFalse(upload.at("/response/success/body").has("example"));
	}

	@Test
	public void keepsDocumentationAvailableWhenAnExampleProviderFails() throws Exception {
		var detail = method("brokenExample");
		assertTrue(detail.at("/response/success/body").has("exampleUnavailable"));
		assertFalse(detail.at("/response/success/body").has("example"));
		assertEquals("string", detail.at("/response/success/body/schema/type").asText());
	}

	@Test
	public void respectsTheAnonymousAccessSettingEvenThoughHelpIsHiddenFromTheResourceList() throws Exception {
		var settings = new SecuritySetting();
		var settingService = mock(SettingService.class);
		when(settingService.getSecuritySetting()).thenReturn(settings);
		var filter = new AnonymousCheckFilter(settingService);
		var resourceInfo = mock(ResourceInfo.class);
		doReturn(ApiHelpResource.class).when(resourceInfo).getResourceClass();
		var resourceField = AnonymousCheckFilter.class.getDeclaredField("resourceInfo");
		resourceField.setAccessible(true);
		resourceField.set(filter, resourceInfo);
		var request = mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn("GET");
		var requestField = AnonymousCheckFilter.class.getDeclaredField("request");
		requestField.setAccessible(true);
		requestField.set(filter, request);
		try (var security = mockStatic(SecurityUtils.class)) {
			security.when(SecurityUtils::isAnonymous).thenReturn(true);
			settings.setEnableAnonymousAccess(false);
			assertThrows(UnauthenticatedException.class, () -> filter.filter(null));
			settings.setEnableAnonymousAccess(true);
			assertDoesNotThrow(() -> filter.filter(null));
			settings.setEnableAnonymousAccess(false);
			security.when(SecurityUtils::isAnonymous).thenReturn(false);
			assertDoesNotThrow(() -> filter.filter(null));
		}
	}

	private JsonNode method(String name, Class<?>... parameters) throws Exception {
		return mapper.valueToTree(new ApiHelpJson().getMethod(SampleResource.class,
				SampleResource.class.getMethod(name, parameters)));
	}

	private JsonNode definition(JsonNode detail, Class<?> type, String origin) {
		return detail.path("$defs").path(type.getName() + "." + origin);
	}

	@Path("/samples")
	public static class SampleResource {
		@Api(order = 10, name = "Create a record") @POST
		public Long create(TestEntity entity) { return null; }
		@Api(order = 20) @POST @Path("/{id}")
		public Response update(@PathParam("id") Long id, TestEntity entity) { return null; }
		@GET public TestEntity read() { return null; }
		@GET @Path("/{id}") @Api(example = "custom result")
		public String query(@PathParam("id") @Api(example = "42") Long id,
				@QueryParam("query") @NotEmpty @Api(description = "Find <b>O'Reilly &amp; friends</b>", example = "O'Reilly & friends") String query,
				@QueryParam("fields") List<String> fields, @QueryParam("count") int count, @Context UriInfo uriInfo) { return null; }
		@POST @Path("/choice") public Response choose(Choice choice) { return null; }
		@POST @Path("/upload") public StreamingOutput upload(InputStream stream) { return null; }
		@GET @Path("/broken") @Api(exampleProvider = "broken") public String brokenExample() { return null; }
		public static String broken() { throw new IllegalStateException("Unavailable provider"); }
	}

	@Path("/another") @Api(name = "AAA")
	public static class AnotherResource { @GET public String get() { return null; } }
	@Path("/internal") @Api(internal = true)
	public static class InternalResource { @GET public String get() { return null; } }

	public static class TestEntity extends AbstractEntity {
		private static final long serialVersionUID = 1L;
		@Api(description = "<b>Name</b> &amp; description") private String name;
		@Immutable private String immutable;
		@JsonProperty(access = JsonProperty.Access.READ_ONLY) private String readOnly;
		@JsonIgnore private String secret;
		@ManyToOne private TestEntity owner;
	}

	public enum Mode { FIRST, SECOND }
	public abstract static class Choice implements Serializable { private static final long serialVersionUID = 1L; }
	public static class FirstChoice extends Choice {
		private static final long serialVersionUID = 1L;
		private Mode mode;
		private Date date;
		private FirstChoice next;

		public Mode getMode() {
			return mode;
		}

		public Date getDate() {
			return date;
		}

		public FirstChoice getNext() {
			return next;
		}
	}
	public static class SecondChoice extends Choice {
		private static final long serialVersionUID = 1L;
		private String value;

		public String getValue() {
			return value;
		}
	}
}
