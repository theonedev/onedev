package io.onedev.server.buildspec.step.pullrequesttitleanddescriptionprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;

class GeneratedPullRequestTitleAndDescriptionTest {

    @Test
    void usesSingleCommitWithoutRequiringAiServices() {
        var subject = "fix(sql): SQL-Korrekturdateien müssen übersprungen werden";
        for (var body : List.of("", "Preserve this body.\n\nCloses #3063")) {
            var commit = RevCommit.parse(("tree 0000000000000000000000000000000000000000\n"
                    + "author Test <test@example.com> 0 +0000\n"
                    + "committer Test <test@example.com> 0 +0000\n\n"
                    + subject + "\n\n" + body + "\n").getBytes(StandardCharsets.UTF_8));
            var update = new PullRequestUpdate() {
                @Override
                public List<RevCommit> getCommits() {
                    return List.of(commit);
                }
            };
            var request = new PullRequest() {
                @Override
                public PullRequestUpdate getLatestUpdate() {
                    return update;
                }
            };
            request.setSourceBranch("fix/sql-correction");

            var result = new GeneratedPullRequestTitleAndDescription().getTitleAndDescription(request);

            assertEquals(subject, result.getLeft());
            if (body.isEmpty())
                assertNull(result.getRight());
            else
                assertEquals(body, result.getRight());
        }
    }
}
