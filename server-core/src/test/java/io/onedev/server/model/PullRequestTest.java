package io.onedev.server.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

class PullRequestTest {

    private final PullRequest request = new PullRequest();

    @Test
    void preservesSingleCommitSubjectWithoutBranchPrefix() {
        for (var subject : List.of("fix(sql): example text",
                "fix(sql): SQL-Korrekturdateien müssen übersprungen werden",
                "feat(api)!: Change response format", "Update documentation")) {
            for (var branch : List.of("sql-correction", "fix/sql-correction",
                    "fix-sql-correction", "fix_sql_correction", "wip/fix/sql-correction")) {
                request.setSourceBranch(branch);
                assertEquals(subject, request.generateTitleFromSingleCommit(commit(subject)), branch);
            }
        }
    }

    @Test
    void removesIssueSuffixFromSingleCommitTitle() {
        for (var suffix : List.of(" (#3063)", " (OD-3063)")) {
            assertEquals("fix(sql): example text", request.generateTitleFromSingleCommit(
                    commit("fix(sql): example text" + suffix)));
        }
    }

    @Test
    void leavesDescriptionEmptyForSubjectOnlyCommit() {
        for (var ending : List.of("", "\n", "\n\n", "\n\n  \n")) {
            assertNull(request.generateDescriptionFromSingleCommit(
                    commit("fix(sql): SQL-Korrekturdateien müssen übersprungen werden" + ending)));
        }
    }

    @Test
    void preservesBodyAndFooterWithoutIncludingSubject() {
        var subject = "feat(sql)!: Change correction handling";
        var body = "SQL-Korrekturdateien müssen übersprungen werden.\nKeep this second line."
                + "\n\nBREAKING CHANGE: Correction files are skipped.\n\nCloses #3063";
        var commit = commit(subject + "\n\n" + body + "\n");

        assertEquals(subject, request.generateTitleFromSingleCommit(commit));
        assertEquals(body, request.generateDescriptionFromSingleCommit(commit));
    }

    private RevCommit commit(String message) {
        return RevCommit.parse(("tree 0000000000000000000000000000000000000000\n"
                + "author Test <test@example.com> 0 +0000\n"
                + "committer Test <test@example.com> 0 +0000\n\n" + message)
                .getBytes(StandardCharsets.UTF_8));
    }
}
