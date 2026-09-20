package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.server.data.migration.DataMigrator;
import io.onedev.server.data.migration.MigrationHelper;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.EntityIdCounter;
import io.onedev.server.model.ProjectNumberCounter;

public class ProjectNumberCounterMigrationTest {

    @TempDir
    Path directory;

    @Test
    public void migratesForkScopesAndRegistersIdsForTheNewTable() throws Exception {
        Files.writeString(directory.resolve("Projects.xml"), "<list><project><id>1</id></project></list>");
        Files.writeString(directory.resolve("Projects.xml.2"),
                "<list><project><id>2</id><forkedFrom>1</forkedFrom></project><project><id>3</id></project></list>");
        var types = Map.of("Issue", 11L, "PullRequest", 21L, "Build", 31L, "Workspace", 41L);
        for (var entry : types.entrySet()) {
            writeEntity(entry.getKey() + "s.xml", 1, 1, 5);
            writeEntity(entry.getKey() + "s.xml.2", 2, 1, entry.getValue());
            writeEntity(entry.getKey() + "s.xml.3", 3, 3, 8);
        }
        assertTrue(MigrationHelper.migrate("244", new DataMigrator(), directory.toFile()));
        var counters = VersionedXmlDoc.fromFile(directory.resolve("ProjectNumberCounters.xml").toFile());
        var byProject = new HashMap<String, org.dom4j.Element>();
        for (var counter : counters.getRootElement().elements())
            byProject.put(counter.elementTextTrim("project"), counter);
        assertEquals(3, byProject.size());
        for (var entry : types.entrySet()) {
            var property = "next" + entry.getKey() + "Number";
            assertEquals(String.valueOf(entry.getValue() + 1), byProject.get("1").elementTextTrim(property));
            assertEquals("1", byProject.get("2").elementTextTrim(property));
            assertEquals("9", byProject.get("3").elementTextTrim(property));
        }
        checkRegisteredCounter(3);
    }

    @Test
    public void registersNewCounterTypeEvenWithoutProjects() throws Exception {
        assertTrue(MigrationHelper.migrate("244", new DataMigrator(), directory.toFile()));
        assertTrue(VersionedXmlDoc.fromFile(directory.resolve("ProjectNumberCounters.xml").toFile())
                .getRootElement().elements().isEmpty());
        checkRegisteredCounter(0);
    }

    private void checkRegisteredCounter(int count) {
        var ids = VersionedXmlDoc.fromFile(directory.resolve("EntityIdCounters.xml").toFile()).getRootElement().elements();
        var byEntity = new HashMap<String, org.dom4j.Element>();
        for (var counter : ids)
            byEntity.put(counter.elementTextTrim("entityName"), counter);
        assertEquals(String.valueOf(ids.size()),
                byEntity.get(EntityIdCounter.class.getName()).elementTextTrim("maxId"));
        assertEquals(String.valueOf(count),
                byEntity.get(ProjectNumberCounter.class.getName()).elementTextTrim("maxId"));
    }

    private void writeEntity(String file, long project, long scope, long number) throws Exception {
        Files.writeString(directory.resolve(file), "<list><entity><id>" + project + "</id><project>" + project
                + "</project><numberScope>" + scope + "</numberScope><number>" + number + "</number></entity></list>");
    }
}
