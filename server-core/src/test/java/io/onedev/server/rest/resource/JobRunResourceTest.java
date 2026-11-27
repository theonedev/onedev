package io.onedev.server.rest.resource;

import io.onedev.server.job.JobService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.rest.resource.support.JobRunOnPullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.PullRequestService;
import jakarta.validation.ValidationException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobRunResourceTest {

    @Test
    void runsOnSelectedBuildCommitWithAConflictedPreview() {
        var requests = mock(PullRequestService.class);
        var jobs = mock(JobService.class);
        var resource = new JobRunResource(jobs, null, null, requests, null);
        var project = new Project();
        var request = new PullRequest();
        request.setId(5L);
        request.setNumber(1);
        request.setTargetProject(project);
        request.setMergePreview(new MergePreview());
        var run = new JobRunOnPullRequest();
        run.setPullRequestId(request.getId());
        run.setJobName("test");
        run.setReason("manual");
        when(requests.load(request.getId())).thenReturn(request);
        var subject = mock(Subject.class);
        var user = new User();
        try (var security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getSubject).thenReturn(subject);
            security.when(() -> SecurityUtils.getUser(subject)).thenReturn(user);
            security.when(() -> SecurityUtils.canRunJob(subject, project, "test")).thenReturn(true);
            assertThrows(ValidationException.class, () -> resource.runBuild(run));
            verifyNoInteractions(jobs);
            var selected = ObjectId.fromString("a".repeat(40));
            request.setBuildCommitHash(selected.name());
            var build = mock(Build.class);
            when(build.getId()).thenReturn(7L);
            when(jobs.submit(user, project, selected, "test", run.getParams(), request.getMergeRef(), request,
                    null, "manual")).thenReturn(build);
            assertEquals(7L, resource.runBuild(run));
        }
    }
}
