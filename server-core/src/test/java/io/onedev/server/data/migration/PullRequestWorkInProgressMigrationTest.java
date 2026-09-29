package io.onedev.server.data.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PullRequestWorkInProgressMigrationTest {

    @TempDir
    Path directory;

    @Test
    void migratesLegacyPrefixesAcrossBatchesAndStatuses() {
        var titles = List.of("WIP Add feature", "[wIp] Fix bug", "wip: Update docs", "[WIP]: Fix tests",
                "WIPped title", "Ordinary title", "Mention WIP later", " WIP Leading space", "WIP");
        var cleanedTitles = List.of("Add feature", "Fix bug", "Update docs", "Fix tests",
                "ped title", "Ordinary title", "Mention WIP later", " WIP Leading space", "");
        var statuses = List.of("OPEN", "MERGED", "DISCARDED");
        for (int batch = 0; batch < statuses.size(); batch++) {
            var doc = new VersionedXmlDoc();
            var list = doc.addElement("list");
            for (var title : titles) {
                var request = list.addElement("io.onedev.server.model.PullRequest");
                request.addElement("title").setText(title);
                request.addElement("status").setText(statuses.get(batch));
            }
            doc.writeToFile(directory.resolve("PullRequests.xml" + (batch == 0 ? "" : "." + batch)).toFile(), false);
        }

        assertTrue(MigrationHelper.migrate("245", new DataMigrator(), directory.toFile()));

        for (int batch = 0; batch < statuses.size(); batch++) {
            var doc = VersionedXmlDoc.fromFile(directory.resolve("PullRequests.xml" + (batch == 0 ? "" : "." + batch)).toFile());
            var requests = doc.getRootElement().elements();
            for (int i = 0; i < titles.size(); i++) {
                assertEquals(cleanedTitles.get(i), requests.get(i).elementText("title"));
                assertEquals(String.valueOf(i < 5 || i == 8), requests.get(i).elementText("workInProgress"));
                assertEquals(statuses.get(batch), requests.get(i).elementText("status"));
            }
        }
    }
}
