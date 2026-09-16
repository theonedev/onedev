package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class HibernateConfigTest {
    @TempDir
    public Path temporary;

    @Test
    public void translatesLegacyConnectionSettingsAndPreservesExplicitModernSettings() throws Exception {
        var install = Files.createDirectory(temporary.resolve("install"));
        Files.createDirectories(install.resolve("conf"));
        var properties = install.resolve("conf/hibernate.properties");
        Files.writeString(properties,
                "hibernate.connection.driver_class=org.hsqldb.jdbc.JDBCDriver\n"
                + "hibernate.connection.url=jdbc:hsqldb:file:${installDir}/data/database\n"
                + "hibernate.connection.username=legacy-user\n"
                + "hibernate.connection.password=legacy-password\n"
                + "hibernate.cache.region.factory_class=com.hazelcast.hibernate.HazelcastLocalCacheRegionFactory\n");
        var legacy = new HibernateConfig(install.toFile());
        assertEquals("org.hsqldb.jdbc.JDBCDriver", legacy.getDriver());
        assertEquals("jdbc:hsqldb:file:" + install + "/data/database", legacy.getUrl());
        assertEquals("legacy-user", legacy.getUser());
        assertEquals("legacy-password", legacy.getPassword());
        assertFalse(legacy.containsKey("hibernate.connection.url"));
        assertEquals(legacy.getUrl(), legacy.getProperty("jakarta.persistence.jdbc.url"));
        assertEquals("com.hazelcast.hibernate.HazelcastLocalCacheRegionFactory",
                legacy.getProperty("hibernate.cache.region.factory_class"));

        Files.writeString(properties, Files.readString(properties)
                + "jakarta.persistence.jdbc.url=jdbc:hsqldb:mem:explicit\n"
                + "jakarta.persistence.jdbc.user=explicit-user\n"
                + "hibernate.cache.region.factory_class=example.CustomRegionFactory\n");
        var modern = new HibernateConfig(install.toFile());
        assertEquals("jdbc:hsqldb:mem:explicit", modern.getUrl());
        assertEquals("explicit-user", modern.getUser());
        assertEquals("legacy-password", modern.getPassword());
        assertEquals("example.CustomRegionFactory", modern.getProperty("hibernate.cache.region.factory_class"));
    }
}
