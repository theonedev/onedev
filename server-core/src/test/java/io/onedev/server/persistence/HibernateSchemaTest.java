package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.data.DefaultDataService;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.EntityIdCounter;
import io.onedev.server.model.Setting;

public class HibernateSchemaTest {

    @Test
    public void exportsSchemaForEverySupportedDialect() throws Exception {
        for (var dialect : List.of("org.hibernate.dialect.HSQLDialect", "org.hibernate.dialect.MySQLDialect",
                "org.hibernate.dialect.MariaDBDialect", "io.onedev.server.persistence.PostgreSQLDialect")) {
            var registry = new StandardServiceRegistryBuilder()
                    .applySetting("hibernate.dialect", dialect)
                    .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
                    .applySetting("hibernate.cache.use_second_level_cache", false)
                    .applySetting("hibernate.format_sql", true).build();
            var installDir = Files.createTempDirectory("onedev-dialect-test");
            try {
                Files.createDirectories(installDir.resolve("conf"));
                Files.writeString(installDir.resolve("conf/hibernate.properties"),
                        "hibernate.dialect=" + dialect + "\nhibernate.connection.url=jdbc:unused\n");
                var sources = new MetadataSources(registry);
                ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class).forEach(sources::addAnnotatedClass);
                var metadata = sources.getMetadataBuilder().applyPhysicalNamingStrategy(new PrefixedNamingStrategy("o_")).build();
                var factories = new DefaultSessionFactoryService();
                inject(factories, "metadata", metadata);
                var data = new DefaultDataService();
                inject(data, "sessionFactoryService", factories);
                inject(data, "hibernateConfig", new HibernateConfig(installDir.toFile()));
                var sql = new ArrayList<String>();
                var statement = Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class<?>[] {Statement.class}, (proxy, method, args) -> {
                            if (method.getName().equals("close")) return null;
                            if (method.getName().equals("execute")) { sql.add((String) args[0]); return false; }
                            throw new UnsupportedOperationException(method.getName());
                        });
                var connection = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class<?>[] {Connection.class}, (proxy, method, args) -> {
                            if (method.getName().equals("createStatement")) return statement;
                            throw new UnsupportedOperationException(method.getName());
                        });
                data.createTables(connection);
                assertEquals(100, sql.stream().filter(command -> command.startsWith("create table ")).count(), dialect);
                assertTrue(sql.stream().noneMatch(command -> command.contains(" foreign key ")));
                if (dialect.equals("io.onedev.server.persistence.PostgreSQLDialect")) {
                    assertTrue(sql.stream().anyMatch(command -> command.contains(" bytea")));
                    assertTrue(sql.stream().noneMatch(command -> command.contains(" oid")));
                }
                var script = new ArrayList<>(sql);
                sql.clear();
                data.applyConstraints(connection);
                assertEquals(179, sql.size(), dialect);
                assertTrue(sql.stream().allMatch(command -> command.contains(" foreign key ")));
                script.addAll(sql);
                sql.clear();
                data.dropConstraints(connection);
                assertEquals(179, sql.size(), dialect);
                var dropKeyword = dialect.contains("MySQL") || dialect.contains("MariaDB")
                        ? " drop foreign key " : " drop constraint ";
                assertTrue(sql.stream().allMatch(command -> command.contains(dropKeyword)), dialect);
                script.addAll(sql);
                sql.clear();
                data.cleanDatabase(connection);
                assertEquals(100, sql.stream().filter(command -> command.startsWith("drop table ")).count(), dialect);
                script.addAll(sql);
                for (var command : script) {
                    assertFalse(command.isBlank() || command.contains("\n") || command.contains("\r"), command);
                    assertFalse(command.contains("o_o_"), command);
                }
                var output = Path.of("target", "schema-audit");
                Files.createDirectories(output);
                Files.write(output.resolve(dialect.substring(dialect.lastIndexOf('.') + 1) + ".sql"), script);
            } finally {
                StandardServiceRegistryBuilder.destroy(registry);
                FileUtils.deleteDirectory(installDir.toFile());
            }
        }
    }

    @Test
    public void createsValidatesAndRecreatesProductionSchema() throws Exception {
        var registry = new StandardServiceRegistryBuilder().disableAutoClose()
                .applySetting("hibernate.connection.driver_class", "org.hsqldb.jdbc.JDBCDriver")
                .applySetting("hibernate.connection.url", "jdbc:hsqldb:mem:" + UUID.randomUUID())
                .applySetting("hibernate.connection.username", "sa")
                .applySetting("hibernate.connection.password", "")
                .applySetting("hibernate.cache.use_second_level_cache", false)
                .applySetting("hibernate.format_sql", true)
                .applySetting("hibernate.hbm2ddl.auto", "validate")
                .applySetting("jakarta.persistence.validation.mode", "none")
                .build();
        var installDir = Files.createTempDirectory("onedev-schema-test");
        try {
            Files.createDirectories(installDir.resolve("conf"));
            Files.writeString(installDir.resolve("conf/hibernate.properties"),
                    "hibernate.dialect=org.hibernate.dialect.HSQLDialect\n"
                    + "hibernate.connection.url=jdbc:hsqldb:mem:unused\n");
            var naming = new PrefixedNamingStrategy("o_");
            var sources = new MetadataSources(registry);
            var entities = ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class);
            entities.removeIf(entity -> !entity.isAnnotationPresent(jakarta.persistence.Entity.class));
            assertTrue(entities.size() > 90, "Production entity discovery failed");
            entities.forEach(sources::addAnnotatedClass);
            var metadata = sources.getMetadataBuilder().applyPhysicalNamingStrategy(naming).build();
            var factories = new DefaultSessionFactoryService();
            inject(factories, "metadata", metadata);
            var data = new DefaultDataService();
            inject(data, "sessionFactoryService", factories);
            inject(data, "physicalNamingStrategy", naming);
            inject(data, "hibernateConfig", new HibernateConfig(installDir.toFile()));
            var connections = registry.getService(org.hibernate.engine.jdbc.connections.spi.ConnectionProvider.class);
            try (var connection = connections.getConnection()) {
                data.createTables(connection);
                assertEquals(0, foreignKeyCount(connection));
                data.applyConstraints(connection);
                assertConstraintEnforcement(connection);
                var snapshot = snapshot(connection);
                assertTrue(foreignKeyCount(connection) > 100);
                var output = Path.of("target", "schema-audit");
                Files.createDirectories(output);
                Files.write(output.resolve("schema.tsv"), snapshot);
                try (var statement = connection.createStatement(); var result = statement.executeQuery("SCRIPT")) {
                    var script = new ArrayList<String>();
                    while (result.next()) script.add(result.getString(1));
                    Files.write(output.resolve("schema.sql"), script);
                }
                for (var entity : entities) {
                    assertEquals("o_" + entity.getSimpleName(), data.getTableName(entity));
                    assertEquals(data.getTableName(entity), metadata.getEntityBinding(entity.getName()).getTable().getName());
                }
                assertEquals("o_id", data.getColumnName(AbstractEntity.PROP_ID));
                // Schema validation and real selects exercise mapped names, SQL types,
                // embedded values and associations against the database we just created.
                try (var factory = metadata.buildSessionFactory(); var session = factory.openSession()) {
                    for (var entity : entities)
                        assertTrue(session.createQuery("from " + entity.getSimpleName(), entity).setMaxResults(1).list().isEmpty());
                    data.dropConstraints(connection);
                    assertEquals(0, foreignKeyCount(connection));
                    data.applyConstraints(connection);
                    assertEquals(stableSnapshot(snapshot), stableSnapshot(snapshot(connection)));
                    data.cleanDatabase(connection);
                    assertTrue(tables(connection).isEmpty());
                    data.createTables(connection);
                    data.applyConstraints(connection);
                    assertEquals(stableSnapshot(snapshot), stableSnapshot(snapshot(connection)));
                    data.cleanDatabase(connection);
                    assertTrue(tables(connection).isEmpty());
                }
                // Exercise an actual Hibernate 5 schema, including INTEGER enum columns
                // and bounded BLOB columns, with the upgraded ORM.
                try (var input = getClass().getResourceAsStream("hibernate5-schema.sql")) {
                    assertNotNull(input);
                    try (var reader = new BufferedReader(new InputStreamReader(input,
                            StandardCharsets.UTF_8)); var statement = connection.createStatement()) {
                        for (var sql : reader.lines().collect(Collectors.toList())) {
                            if (!sql.startsWith("--") && !sql.isBlank()) statement.execute(sql);
                        }
                    }
                }
                var legacySnapshot = snapshot(connection);
                // ID counters were added after the Hibernate 5 schema fixture.
                assertEquals(structure(snapshot.stream()
                        .filter(line -> !line.contains("\tO_ENTITYIDCOUNTER")).collect(Collectors.toList())),
                        structure(legacySnapshot));
                assertConstraintEnforcement(connection);
                var legacySources = new MetadataSources(registry);
                var legacyEntities = entities.stream().filter(entity -> entity != EntityIdCounter.class)
                        .collect(Collectors.toList());
                legacyEntities.forEach(legacySources::addAnnotatedClass);
                var legacyMetadata = legacySources.getMetadataBuilder().applyPhysicalNamingStrategy(naming).build();
                inject(factories, "metadata", legacyMetadata);
                try (var factory = legacyMetadata.buildSessionFactory(); var session = factory.openSession()) {
                    for (var entity : legacyEntities)
                        assertTrue(session.createQuery("from " + entity.getSimpleName(), entity).setMaxResults(1).list().isEmpty());
                    var key = Setting.Key.SYSTEM;
                    try (var insert = connection.prepareStatement("insert into o_Setting (o_id, o_key, o_value) values (?, ?, ?)")) {
                        insert.setLong(1, 1);
                        insert.setInt(2, key.ordinal());
                        insert.setBytes(3, SerializationUtils.serialize("legacy setting"));
                        insert.executeUpdate();
                    }
                    var setting = session.find(Setting.class, 1L);
                    assertEquals(key, setting.getKey());
                    assertEquals("legacy setting", setting.getValue());
                    var tx = session.beginTransaction();
                    setting.setKey(Setting.Key.BACKUP);
                    setting.setValue("updated setting");
                    tx.commit();
                    session.clear();
                    setting = session.find(Setting.class, 1L);
                    assertEquals(Setting.Key.BACKUP, setting.getKey());
                    assertEquals("updated setting", setting.getValue());
                }
                data.cleanDatabase(connection);
                assertTrue(tables(connection).isEmpty());
                assertEquals(Integer.valueOf(1), PersistenceUtils.callWithLock(connection, () -> 1));
                assertEquals(Integer.valueOf(2), PersistenceUtils.callWithLock(connection, () -> 2));
                assertEquals(List.of("O_DATABASELOCK"), tables(connection));
                try (var statement = connection.createStatement(); var result = statement.executeQuery("select o_id from o_DatabaseLock")) {
                    assertTrue(result.next());
                    assertEquals(1, result.getInt(1));
                }
                System.out.println("Schema audit: " + entities.size() + " entities, " + snapshot.size() + " schema entries");
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
            FileUtils.deleteDirectory(installDir.toFile());
        }
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static List<String> tables(Connection connection) throws SQLException {
        var tables = new ArrayList<String>();
        try (var result = connection.getMetaData().getTables(null, "PUBLIC", "%", new String[] {"TABLE"})) {
            while (result.next()) tables.add(result.getString("TABLE_NAME"));
        }
        return tables;
    }

    private static int foreignKeyCount(Connection connection) throws SQLException {
        int count = 0;
        for (var table : tables(connection)) {
            try (var result = connection.getMetaData().getImportedKeys(null, "PUBLIC", table)) {
                while (result.next()) count++;
            }
        }
        return count;
    }

    private static List<String> snapshot(Connection connection) throws SQLException {
        var rows = new TreeSet<String>();
        var jdbc = connection.getMetaData();
        for (var table : tables(connection)) {
            assertTrue(table.startsWith("O_") && !table.startsWith("O_O_"), table);
            rows.add("TABLE\t" + table);
            try (var result = jdbc.getColumns(null, "PUBLIC", table, "%")) {
                while (result.next()) {
                    var column = result.getString("COLUMN_NAME");
                    assertTrue(column.startsWith("O_") && !column.startsWith("O_O_"), table + "." + column);
                    rows.add(row("COLUMN", table, result, "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME",
                            "COLUMN_SIZE", "DECIMAL_DIGITS", "NULLABLE", "COLUMN_DEF"));
                }
            }
            try (var result = jdbc.getPrimaryKeys(null, "PUBLIC", table)) {
                while (result.next()) rows.add(row("PRIMARY_KEY", table, result, "COLUMN_NAME", "KEY_SEQ"));
            }
            try (var result = jdbc.getIndexInfo(null, "PUBLIC", table, false, false)) {
                while (result.next()) rows.add(row("INDEX", table, result, "INDEX_NAME", "NON_UNIQUE",
                        "COLUMN_NAME", "ORDINAL_POSITION", "ASC_OR_DESC"));
            }
            try (var result = jdbc.getImportedKeys(null, "PUBLIC", table)) {
                while (result.next()) rows.add(row("FOREIGN_KEY", table, result, "FK_NAME", "FKCOLUMN_NAME",
                        "PKTABLE_NAME", "PKCOLUMN_NAME", "KEY_SEQ", "UPDATE_RULE", "DELETE_RULE"));
            }
        }
        return new ArrayList<>(rows);
    }

    private static void assertConstraintEnforcement(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("insert into o_Setting(o_id, o_key) values(10, 0)");
            for (var sql : List.of("insert into o_Setting(o_id, o_key) values(10, 1)",
                    "insert into o_Setting(o_id, o_key) values(11, 0)",
                    "insert into o_Setting(o_id, o_key) values(11, null)",
                    "insert into o_EmailAddress(o_id, o_primary, o_value, o_owner_id) values(1, false, 'test@example.com', -1)")) {
                var failure = assertThrows(SQLException.class, () -> statement.execute(sql));
                assertTrue(failure.getSQLState().startsWith("23"), sql + ": " + failure.getMessage());
            }
            statement.execute("delete from o_Setting where o_id=10");
        }
    }

    private static List<String> structure(List<String> snapshot) {
        var rows = new ArrayList<String>();
        var indexes = new TreeMap<String, List<String>>();
        for (var row : snapshot) {
            var parts = row.split("\t");
            if (parts[0].equals("COLUMN")) {
                // Hibernate 7 narrows ordinal enums and increases HSQLDB BLOB capacity.
                // All other column attributes, including nullability/defaults, must match.
                if (parts[4].equals("BLOB")) parts[5] = "<capacity>";
                if (parts[4].equals("TINYINT")) {
                    parts[3] = "4";
                    parts[4] = "INTEGER";
                    parts[5] = "32";
                }
                rows.add(String.join("\t", parts));
            } else if (parts[0].equals("INDEX")) {
                // Compare each complete index, preserving its column order and uniqueness.
                // HSQLDB now assigns system names to inline unique constraints.
                indexes.computeIfAbsent(parts[1] + "\t" + parts[2], key -> new ArrayList<>())
                        .add(String.join("\t", parts[1], parts[3], parts[5], parts[4], parts[6]));
            } else {
                rows.add(row);
            }
        }
        for (var index : indexes.values()) {
            Collections.sort(index);
            rows.add("INDEX\t" + String.join(";", index));
        }
        Collections.sort(rows);
        return rows;
    }

    private static List<String> stableSnapshot(List<String> snapshot) {
        // HSQLDB assigns new internal names to primary/unnamed unique indexes on recreation.
        return snapshot.stream().map(row -> row.replaceAll("SYS_(PK|CT)_[0-9]+", "SYS_$1"))
                .sorted().collect(Collectors.toList());
    }

    private static String row(String kind, String table, ResultSet result, String... columns) throws SQLException {
        var row = new StringBuilder(kind).append('\t').append(table);
        for (var column : columns) row.append('\t').append(result.getString(column));
        return row.toString();
    }
}
