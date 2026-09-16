package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.UUID;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.type.descriptor.java.PrimitiveByteArrayJavaType;
import org.hibernate.type.spi.TypeConfiguration;
import org.junit.jupiter.api.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import io.onedev.server.util.xstream.HibernateProxyMapper;

public class HibernateBackupCompatibilityTest {

    @Test
    public void serializesProxyTypesAsPersistentEntityTypes() {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", "jdbc:hsqldb:mem:" + UUID.randomUUID())
                .applySetting("hibernate.connection.username", "sa")
                .applySetting("hibernate.cache.use_second_level_cache", false)
                .applySetting("jakarta.persistence.validation.mode", "none").build();
        try {
            var metadata = new MetadataSources(registry)
                    .addAnnotatedClass(HibernateOrmUpgradeTest.Item.class).buildMetadata();
            try (var factory = metadata.buildSessionFactory(); var session = factory.openSession()) {
                var reference = session.getReference(HibernateOrmUpgradeTest.Item.class, 1L);
                assertNotEquals(HibernateOrmUpgradeTest.Item.class, reference.getClass());
                var xstream = new XStream() {
                    @Override
                    protected MapperWrapper wrapMapper(MapperWrapper next) {
                        return new HibernateProxyMapper(next);
                    }
                };
                assertEquals(xstream.getMapper().serializedClass(HibernateOrmUpgradeTest.Item.class),
                        xstream.getMapper().serializedClass(reference.getClass()));
                assertFalse(org.hibernate.Hibernate.isInitialized(reference));
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    public void bindsPostgresBlobNullsAsBinaryInsteadOfOid() throws Exception {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", PostgreSQLDialect.class.getName())
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", false).build();
        try {
            var metadata = new MetadataSources(registry).buildMetadata();
            TypeConfiguration types = ((org.hibernate.boot.spi.MetadataImplementor) metadata).getTypeConfiguration();
            var binder = types.getJdbcTypeRegistry().getDescriptor(Types.BLOB)
                    .getBinder(PrimitiveByteArrayJavaType.INSTANCE);
            var statement = mock(PreparedStatement.class);
            binder.bind(statement, null, 1, null);
            verify(statement).setNull(1, Types.VARBINARY);
            var bytes = new byte[] {0, 1, -1};
            binder.bind(statement, bytes, 2, null);
            verify(statement).setBytes(2, bytes);
            var callable = mock(CallableStatement.class);
            binder.bind(callable, null, "value", null);
            verify(callable).setNull("value", Types.VARBINARY);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
