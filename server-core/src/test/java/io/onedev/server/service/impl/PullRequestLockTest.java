package io.onedev.server.service.impl;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestReviewRequested;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastActivityDate;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.persistence.PrefixedNamingStrategy;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.PullRequestChangeService;
import jakarta.persistence.Entity;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.shiro.subject.Subject;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.mapping.SimpleValue;
import org.junit.jupiter.api.Test;

class PullRequestLockTest {

    @Test
    void staleReviewWaitsForReconciliationOnItsOwnConnection() throws Exception {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", "jdbc:hsqldb:mem:" + UUID.randomUUID() + ";hsqldb.tx=mvcc")
                .applySetting("hibernate.connection.username", "sa")
                .applySetting("hibernate.connection.pool_size", 2)
                .applySetting("hibernate.cache.use_second_level_cache", false)
                .applySetting("jakarta.persistence.validation.mode", "none")
                .applySetting("hibernate.hbm2ddl.auto", "create-drop").build();
        try {
            var sources = new MetadataSources(registry);
            ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class).stream()
                    .filter(type -> type.isAnnotationPresent(Entity.class)).forEach(sources::addAnnotatedClass);
            var metadata = sources.getMetadataBuilder().applyPhysicalNamingStrategy(new PrefixedNamingStrategy("o_")).build();
            var ids = new AtomicLong();
            for (var binding : metadata.getEntityBindings())
                ((SimpleValue) binding.getIdentifier()).setCustomIdGeneratorCreator(
                        context -> (IdentifierGenerator) (session, entity) -> ids.incrementAndGet());
            try (var factory = metadata.buildSessionFactory()) {
                var id = createReviewedRequest(factory);
                var executor = Executors.newSingleThreadExecutor();
                try (var writer = factory.openSession()) {
                    var tx = writer.beginTransaction();
                    var request = writer.find(PullRequest.class, id);
                    request.lockForUpdate(writer);
                    var submitting = new CountDownLatch(1);
                    var submission = executor.submit(() -> {
                        try (var reader = factory.openSession()) {
                            var reviewTx = reader.beginTransaction();
                            var stale = reader.find(PullRequest.class, id);
                            var reviewed = stale.getLatestUpdate();
                            var review = stale.getReviews().iterator().next();
                            assertEquals(PullRequestReview.Status.APPROVED, review.getStatus());
                            var reviews = new DefaultPullRequestReviewService();
                            var dao = mock(Dao.class);
                            when(dao.getSession()).thenReturn(reader);
                            FieldUtils.writeField(reviews, "dao", dao, true);
                            submitting.countDown();
                            var subject = mock(Subject.class);
                            try (var security = mockStatic(SecurityUtils.class)) {
                                security.when(() -> SecurityUtils.getUser(subject)).thenReturn(review.getUser());
                                security.when(() -> SecurityUtils.canReadCode(subject, stale.getProject())).thenReturn(true);
                                assertThrows(NotAcceptableException.class,
                                        () -> reviews.submitDecision(subject, reviewed, true,
                                                () -> fail("A stale review must not prepare its summary")));
                            }
                            assertEquals(PullRequestReview.Status.PENDING, stale.getReviews().iterator().next().getStatus());
                            assertEquals("release", stale.getTargetBranch());
                            assertNull(stale.getMergePreview());
                            reviewTx.rollback();
                        }
                        return null;
                    });
                    assertTrue(submitting.await(5, SECONDS));
                    assertThrows(TimeoutException.class, () -> submission.get(100, TimeUnit.MILLISECONDS));
                    var changes = new DefaultPullRequestChangeService();
                    var dao = mock(Dao.class);
                    when(dao.getSession()).thenReturn(writer);
                    FieldUtils.writeField(changes, "dao", dao, true);
                    FieldUtils.writeField(changes, "listenerRegistry", mock(ListenerRegistry.class), true);
                    assertNotNull(request.getMergePreview());
                    changes.changeTargetBranch(request.getSubmitter(), request, "release");
                    assertNull(request.getMergePreview());
                    var update = new PullRequestUpdate();
                    update.setRequest(request);
                    update.setHeadCommitHash("b".repeat(40));
                    writer.persist(update);
                    request.getUpdates().add(update);
                    var reviews = new DefaultPullRequestReviewService();
                    var events = mock(ListenerRegistry.class);
                    var transactions = mock(TransactionService.class);
                    doAnswer(invocation -> {
                        invocation.getArgument(0, Runnable.class).run();
                        return null;
                    }).when(transactions).run(any());
                    FieldUtils.writeField(reviews, "dao", dao, true);
                    FieldUtils.writeField(reviews, "listenerRegistry", events, true);
                    FieldUtils.writeField(reviews, "transactionService", transactions, true);
                    try (var oneDev = mockStatic(OneDev.class)) {
                        oneDev.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(mock(SubscriptionService.class));
                        reviews.reconcile(request.getSubmitter(), request, Set.of());
                        reviews.reconcile(request.getSubmitter(), request, Set.of());
                    }
                    verify(events).post(isA(PullRequestReviewRequested.class));
                    assertEquals(2, request.getUpdates().size());
                    tx.commit();
                    submission.get(5, SECONDS);
                } finally {
                    executor.shutdownNow();
                }
                try (var session = factory.openSession()) {
                    var tx = session.beginTransaction();
                    var request = session.find(PullRequest.class, id);
                    var review = request.getReviews().iterator().next();
                    var reviews = new DefaultPullRequestReviewService();
                    var changes = mock(PullRequestChangeService.class);
                    var dao = mock(Dao.class);
                    when(dao.getSession()).thenReturn(session);
                    FieldUtils.writeField(reviews, "dao", dao, true);
                    FieldUtils.writeField(reviews, "changeService", changes, true);
                    var latest = request.getLatestUpdate();
                    var subject = mock(Subject.class);
                    try (var security = mockStatic(SecurityUtils.class)) {
                        security.when(() -> SecurityUtils.getUser(subject)).thenReturn(review.getUser());
                        security.when(() -> SecurityUtils.canReadCode(subject, request.getProject())).thenReturn(true);
                        reviews.submitDecision(subject, latest, false, () -> "Please revise");
                    }
                    assertEquals(PullRequestReview.Status.REQUESTED_FOR_CHANGES, review.getStatus());
                    assertEquals(latest, review.getUpdate());
                    verify(changes).create(argThat(change -> change.getData() instanceof PullRequestRequestedForChangesData),
                            eq("Please revise"));
                    reviews.approve(review.getUser(), latest, null);
                    assertEquals(PullRequestReview.Status.APPROVED, review.getStatus());
                    assertEquals(latest, review.getUpdate());
                    verify(changes).create(argThat(change -> change.getData() instanceof PullRequestApproveData), isNull());
                    session.flush();
                    session.remove(request);
                    tx.commit();
                }
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private long createReviewedRequest(SessionFactory factory) {
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            var activity = new ProjectLastActivityDate();
            session.persist(activity);
            var project = new Project();
            project.setLastActivityDate(activity);
            project.setName("project");
            project.setPath("project");
            session.persist(project);
            var submitter = new User();
            submitter.setName("author");
            session.persist(submitter);
            var reviewer = new User();
            reviewer.setName("reviewer");
            reviewer.setType(User.Type.AI);
            session.persist(reviewer);
            var request = new PullRequest();
            request.setTitle("Review this change");
            var preview = new MergePreview();
            preview.setHeadCommitHash("a".repeat(40));
            preview.setTargetBranch("main");
            preview.setTargetHeadCommitHash("c".repeat(40));
            preview.setMergeCommitHash("d".repeat(40));
            preview.setMergeStrategy(MergeStrategy.CREATE_MERGE_COMMIT);
            request.setMergePreview(preview);
            request.setSubmitter(submitter);
            request.setNumberScope(project);
            request.setTargetProject(project);
            request.setTargetBranch("main");
            request.setSourceProject(project);
            request.setSourceBranch("feature");
            request.setMergeStrategy(MergeStrategy.CREATE_MERGE_COMMIT);
            var opened = new LastActivity();
            opened.setDate(request.getSubmitDate());
            opened.setDescription("opened");
            request.setLastActivity(opened);
            session.persist(request);
            var update = new PullRequestUpdate();
            update.setRequest(request);
            update.setHeadCommitHash("a".repeat(40));
            session.persist(update);
            var review = new PullRequestReview();
            review.setRequest(request);
            review.setUser(reviewer);
            review.setUpdate(update);
            review.setStatus(PullRequestReview.Status.APPROVED);
            session.persist(review);
            tx.commit();
            return request.getId();
        }
    }
}
