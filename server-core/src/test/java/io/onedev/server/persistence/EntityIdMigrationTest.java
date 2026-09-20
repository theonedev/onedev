package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.data.DefaultDataService;
import io.onedev.server.data.migration.DataMigrator;
import io.onedev.server.data.migration.MigrationHelper;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.EntityIdCounter;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.ModelVersion;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;

public class EntityIdMigrationTest {

    @TempDir
    Path directory;

    @Test
    public void migrationInitializesEveryEntityFromAllExportBatches() throws Exception {
        writeEntities("Issues.xml", Issue.class, 5, 2);
        writeEntities("Issues.xml.1", Issue.class, 500, 200);
        writeEntities("IssueComments.xml", IssueComment.class, 900);
        writeEntities("Users.xml", User.class, -2, -1, 1);
        writeEntities("Builds.xml", Build.class, -5);
        writeEntities("ModelVersions.xml", ModelVersion.class, 1);
        var originalIssues = Files.readString(directory.resolve("Issues.xml"));
        assertTrue(MigrationHelper.migrate("244", new DataMigrator(), directory.toFile()));

        var counters = VersionedXmlDoc.fromFile(directory.resolve("EntityIdCounters.xml").toFile());
        var maxIds = new HashMap<String, Long>();
        var rowIds = new HashSet<Long>();
        for (var counter : counters.getRootElement().elements()) {
            assertEquals(EntityIdCounter.class.getName(), counter.getName());
            assertEquals("0.0", counter.attributeValue("revision"));
            assertTrue(rowIds.add(Long.parseLong(counter.elementTextTrim("id"))));
            assertNull(maxIds.put(counter.elementTextTrim("entityName"), Long.parseLong(counter.elementTextTrim("maxId"))));
        }
        var entities = ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class);
        entities.removeIf(type -> !type.isAnnotationPresent(jakarta.persistence.Entity.class));
        assertEquals(entities.size(), maxIds.size());
        for (var entity : entities)
            assertTrue(maxIds.containsKey(entity.getName()), entity.getName());
        assertEquals(500L, maxIds.get(Issue.class.getName()));
        assertEquals(900L, maxIds.get(IssueComment.class.getName()));
        assertEquals(1L, maxIds.get(User.class.getName()));
        assertEquals(0L, maxIds.get(Build.class.getName()));
        assertEquals(0L, maxIds.get(Role.class.getName()), "Entities without export files start at zero");
        assertEquals(1L, maxIds.get(ModelVersion.class.getName()));
        assertEquals((long) entities.size(), maxIds.get(EntityIdCounter.class.getName()));
        for (long id = 1; id <= entities.size(); id++)
            assertTrue(rowIds.contains(id));
        assertEquals(originalIssues, Files.readString(directory.resolve("Issues.xml")));
    }

    @Test
    public void freshDatabaseSeedsCountersWithoutRunningMigration() throws Exception {
        var url = "jdbc:hsqldb:mem:" + UUID.randomUUID();
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", url)
                .applySetting("hibernate.connection.username", "sa")
                .applySetting("hibernate.cache.use_second_level_cache", false).build();
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            var naming = new PrefixedNamingStrategy("o_");
            var sources = new MetadataSources(registry);
            ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class).stream()
                    .filter(type -> type.isAnnotationPresent(jakarta.persistence.Entity.class))
                    .forEach(sources::addAnnotatedClass);
            var metadata = sources.getMetadataBuilder().applyPhysicalNamingStrategy(naming).build();
            var factories = new DefaultSessionFactoryService();
            inject(factories, "metadata", metadata);
            Files.createDirectories(directory.resolve("conf"));
            Files.writeString(directory.resolve("conf/hibernate.properties"),
                    "hibernate.dialect=org.hibernate.dialect.HSQLDialect\nhibernate.connection.url=" + url + "\n");
            var data = new DefaultDataService();
            inject(data, "sessionFactoryService", factories);
            inject(data, "physicalNamingStrategy", naming);
            inject(data, "hibernateConfig", new HibernateConfig(directory.toFile()));
            PersistenceUtils.callWithTransaction(connection, () -> {
                data.populateDatabase(connection);
                return null;
            });
            // A subsequent startup must preserve existing rows instead of seeding again.
            PersistenceUtils.callWithTransaction(connection, () -> {
                data.populateDatabase(connection);
                return null;
            });
            var maxIds = new HashMap<String, Long>();
            try (var statement = connection.createStatement(); var rows = statement.executeQuery(
                    "select o_entityName, o_maxId from o_EntityIdCounter")) {
                while (rows.next())
                    assertNull(maxIds.put(rows.getString(1), rows.getLong(2)));
            }
            assertEquals(metadata.getEntityBindings().size(), maxIds.size());
            for (var binding : metadata.getEntityBindings()) {
                var type = binding.getMappedClass();
                long expected = type == EntityIdCounter.class ? maxIds.size() : type == ModelVersion.class ? 1 : 0;
                assertEquals(expected, maxIds.get(type.getName()), type.getName());
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private void writeEntities(String fileName, Class<?> type, long... ids) {
        var doc = new VersionedXmlDoc();
        var list = doc.addElement("list");
        for (long id : ids)
            list.addElement(type.getName()).addElement("id").setText(String.valueOf(id));
        doc.writeToFile(directory.resolve(fileName).toFile(), false);
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
