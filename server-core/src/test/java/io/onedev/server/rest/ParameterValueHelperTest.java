package io.onedev.server.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Function;

import jakarta.ws.rs.BadRequestException;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.model.Parameter.Source;
import org.glassfish.jersey.server.spi.internal.ParamValueFactoryWithSource;
import org.glassfish.jersey.server.spi.internal.ParameterValueHelper;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.rest.annotation.Immutable;
import io.onedev.server.util.jackson.hibernate.HibernateObjectMapperModule;

public class ParameterValueHelperTest extends AppLoaderMocker {

    private Dao dao;
    private ObjectMapper mapper;

    @Override
    protected void setup() {
        dao = mock(Dao.class);
        mapper = new ObjectMapper().registerModule(new HibernateObjectMapperModule(dao));
        // Match ObjectMapperProvider: REST entities are mapped through their fields.
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        when(AppLoader.getInstance(XStream.class)).thenReturn(new XStream());
    }

    @Override
    protected void teardown() {
    }

    @Test
    public void updatesTheEntitySelectedByTheFirstArgumentAndClearsContext() {
        var entity = existingEntity(42L);
        var args = parameters(
                provider(Source.PATH, request -> 42L),
                provider(Source.ENTITY, request -> read(
                        "{\"name\":\"updated\",\"immutableValue\":\"replacement\"}")),
                provider(Source.QUERY, request -> {
                    // Even arguments after the body must be resolved before deserialization.
                    verifyNoInteractions(dao);
                    return "query";
                }));

        assertEquals(42L, args[0]);
        assertSame(entity, args[1]);
        assertEquals("query", args[2]);
        assertEquals("updated", entity.getName());
        assertEquals("retained", entity.getDescription());
        assertEquals("original", entity.getImmutableValue());
        assertEquals("before", entity.getOldVersion().getRootElement().elementText("name"));
        verify(dao).load(TestEntity.class, 42L);
        assertNoEntityContext();
    }

    @Test
    public void createsAnEntityWhenThereIsNoLeadingLongArgument() {
        var created = (TestEntity) parameters(provider(Source.ENTITY,
                request -> read("{\"name\":\"created\",\"immutableValue\":\"initial\"}")))[0];
        assertNull(created.getId());
        assertNull(created.getOldVersion());
        assertEquals("created", created.getName());
        assertEquals("initial", created.getImmutableValue());

        // A Long elsewhere in the argument list must not be treated as the entity ID.
        var args = parameters(provider(Source.QUERY, request -> "first"),
                provider(Source.PATH, request -> 42L),
                provider(Source.ENTITY, request -> read("{}")));
        assertNull(((TestEntity) args[2]).getId());
        verifyNoInteractions(dao);
    }

    @Test
    public void clearsContextWhenANonBodyParameterFails() {
        var failure = new BadRequestException("Invalid query parameter");
        assertSame(failure, assertThrows(BadRequestException.class, () -> parameters(
                provider(Source.PATH, request -> 42L),
                provider(Source.QUERY, request -> { throw failure; }),
                provider(Source.ENTITY, request -> fail("Body must not be read")))));
        assertNoEntityContext();
    }

    @Test
    public void clearsContextWhenBodyDeserializationFails() {
        existingEntity(42L);
        assertThrows(BadRequestException.class, () -> parameters(
                provider(Source.PATH, request -> 42L),
                provider(Source.ENTITY, request -> read("{\"description\":\"retained\",\"name\":"))));
        verify(dao).load(TestEntity.class, 42L);
        assertNoEntityContext();
    }

    @Test
    public void restoresOuterContextAfterNestedSuccessAndFailure() {
        var outer = existingEntity(42L);
        var inner = existingEntity(7L);
        var args = parameters(provider(Source.PATH, request -> 42L),
                provider(Source.ENTITY, request -> {
                    var nested = parameters(provider(Source.PATH, nestedRequest -> 7L),
                            provider(Source.ENTITY, nestedRequest -> read("{\"name\":\"inner\"}")));
                    assertSame(inner, nested[1]);
                    assertSame(outer, read("{\"name\":\"after success\"}"));

                    assertThrows(BadRequestException.class, () -> parameters(
                            provider(Source.PATH, nestedRequest -> 7L),
                            provider(Source.ENTITY, nestedRequest -> read("{\"description\":\"retained\",\"name\":"))));
                    return read("{\"name\":\"after failure\"}");
                }));
        assertSame(outer, args[1]);
        assertEquals("after failure", outer.getName());
        assertEquals("inner", inner.getName());
        assertNoEntityContext();
    }

    private TestEntity existingEntity(Long id) {
        var entity = new TestEntity();
        entity.setId(id);
        entity.setName("before");
        entity.setDescription("retained");
        entity.setImmutableValue("original");
        when(dao.load(TestEntity.class, id)).thenReturn(entity);
        return entity;
    }

    private void assertNoEntityContext() {
        clearInvocations(dao);
        // Deserialize outside Jersey on the same thread to detect a leaked argument array.
        var created = read("{\"name\":\"fresh\"}");
        assertNull(created.getId());
        assertNull(created.getOldVersion());
        assertEquals("fresh", created.getName());
        verifyNoInteractions(dao);
    }

    private TestEntity read(String json) {
        try {
            return mapper.readValue(json, TestEntity.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e);
        }
    }

    private ParamValueFactoryWithSource<?> provider(Source source, Function<ContainerRequest, ?> function) {
        return new ParamValueFactoryWithSource<>(function, source);
    }

    private Object[] parameters(ParamValueFactoryWithSource<?>... providers) {
        return ParameterValueHelper.getParameterValues(List.of(providers), null);
    }

    public static class TestEntity extends AbstractEntity {
        private static final long serialVersionUID = 1L;

        private String name;
        private String description;
        @Immutable
        private String immutableValue;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getImmutableValue() { return immutableValue; }
        public void setImmutableValue(String immutableValue) { this.immutableValue = immutableValue; }
    }
}
