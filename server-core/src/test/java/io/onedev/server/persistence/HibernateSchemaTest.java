package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
                var tableCount = metadata.collectTableMappings().stream().filter(table -> table.isPhysicalTable()).count();
                var foreignKeyCount = metadata.collectTableMappings().stream()
                        .flatMap(table -> table.getForeignKeyCollection().stream())
                        .filter(key -> key.isCreationEnabled() && key.isPhysicalConstraint()).count();
                assertTrue(tableCount > 0, "Production table discovery failed");
                assertTrue(foreignKeyCount > 0, "Production foreign key discovery failed");
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
                assertEquals(tableCount, sql.stream().filter(command -> command.startsWith("create table ")).count(), dialect);
                assertTrue(sql.stream().noneMatch(command -> command.contains(" foreign key ")));
                assertTrue(sql.stream().anyMatch(command -> command.contains(" primary key ")), dialect);
                assertTrue(sql.stream().anyMatch(command -> command.contains(" not null")), dialect);
                assertTrue(sql.stream().anyMatch(command -> command.contains(" unique ")), dialect);
                assertTrue(sql.stream().anyMatch(command -> command.startsWith("create index ")), dialect);
                if (dialect.equals("io.onedev.server.persistence.PostgreSQLDialect")) {
                    assertTrue(sql.stream().anyMatch(command -> command.contains(" bytea")));
                    assertTrue(sql.stream().noneMatch(command -> command.contains(" oid")));
                }
                var script = new ArrayList<>(sql);
                sql.clear();
                data.applyConstraints(connection);
                assertEquals(foreignKeyCount, sql.size(), dialect);
                assertTrue(sql.stream().allMatch(command -> command.contains(" foreign key ")));
                script.addAll(sql);
                sql.clear();
                data.dropConstraints(connection);
                assertEquals(foreignKeyCount, sql.size(), dialect);
                var dropKeyword = dialect.contains("MySQL") || dialect.contains("MariaDB")
                        ? " drop foreign key " : " drop constraint ";
                assertTrue(sql.stream().allMatch(command -> command.contains(dropKeyword)), dialect);
                script.addAll(sql);
                sql.clear();
                data.cleanDatabase(connection);
                assertEquals(tableCount, sql.stream().filter(command -> command.startsWith("drop table ")).count(), dialect);
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
            assertFalse(entities.isEmpty(), "Production entity discovery failed");
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
                // Capture this run's schema only: recreation must be stable, while model
                // evolution is free to change the schema between runs.
                var snapshot = snapshot(connection);
                assertTrue(foreignKeyCount(connection) > 0);
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
                    var key = Setting.Key.SYSTEM;
                    try (var insert = connection.prepareStatement("insert into o_Setting (o_id, o_key, o_value) values (?, ?, ?)")) {
                        insert.setLong(1, 1);
                        insert.setInt(2, key.ordinal());
                        insert.setBytes(3, SerializationUtils.serialize("initial setting"));
                        insert.executeUpdate();
                    }
                    var setting = session.find(Setting.class, 1L);
                    assertEquals(key, setting.getKey());
                    assertEquals("initial setting", setting.getValue());
                    var tx = session.beginTransaction();
                    setting.setKey(Setting.Key.BACKUP);
                    setting.setValue("updated setting");
                    tx.commit();
                    session.clear();
                    setting = session.find(Setting.class, 1L);
                    assertEquals(Setting.Key.BACKUP, setting.getKey());
                    assertEquals("updated setting", setting.getValue());
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
