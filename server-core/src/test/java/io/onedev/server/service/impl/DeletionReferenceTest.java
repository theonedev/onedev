package io.onedev.server.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.mapping.SimpleValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueCommentReaction;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.IssueReaction;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.IssueStateHistory;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastActivityDate;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.persistence.PrefixedNamingStrategy;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import jakarta.inject.Inject;

class DeletionReferenceTest {
	private static final String HASH = "0".repeat(40);
	private static StandardServiceRegistry registry;
	private static SessionFactory factory;
	private static jakarta.validation.ValidatorFactory validatorFactory;
	private Session session;
	private User author;
	private MockedStatic<SecurityUtils> security;
	private MockedStatic<OneDev> application;
	private GlobalIssueSetting issueSetting;
	private long fixtureNumber;

	@BeforeAll
	static void createDatabase() {
		validatorFactory = jakarta.validation.Validation.buildDefaultValidatorFactory();
		registry = new StandardServiceRegistryBuilder()
				.applySetting("hibernate.connection.url", "jdbc:hsqldb:mem:deletion_" + UUID.randomUUID())
				.applySetting("hibernate.connection.username", "sa")
				.applySetting("hibernate.cache.use_second_level_cache", false)
				.applySetting("hibernate.hbm2ddl.auto", "create-drop")
				.applySetting("jakarta.persistence.validation.mode", "none").build();
		var sources = new MetadataSources(registry);
		ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class).forEach(sources::addAnnotatedClass);
		var metadata = sources.getMetadataBuilder().applyPhysicalNamingStrategy(new PrefixedNamingStrategy("o_")).build();
		var ids = new AtomicLong();
		for (var entity : metadata.getEntityBindings()) {
			((SimpleValue) entity.getIdentifier()).setCustomIdGeneratorCreator(context ->
					(IdentifierGenerator) (session, object) -> ids.incrementAndGet());
		}
		factory = metadata.buildSessionFactory();
	}

	@AfterAll
	static void closeDatabase() {
		if (validatorFactory != null)
			validatorFactory.close();
		if (factory != null)
			factory.close();
		StandardServiceRegistryBuilder.destroy(registry);
	}

	@BeforeEach
	void beginTransaction() {
		session = factory.withOptions()
				.interceptor(new io.onedev.server.persistence.HibernateInterceptor(Set.of())).openSession();
		session.beginTransaction();
		author = user("author");
		security = mockStatic(SecurityUtils.class);
		security.when(SecurityUtils::getUser).thenReturn(author);
		issueSetting = new GlobalIssueSetting();
		var settings = mock(SettingService.class);
		when(settings.getIssueSetting()).thenReturn(issueSetting);
		application = mockStatic(OneDev.class);
		application.when(() -> OneDev.getInstance(jakarta.validation.Validator.class)).thenReturn(validatorFactory.getValidator());
		application.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
		application.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(mock(SubscriptionService.class));
		var projects = mock(io.onedev.server.service.ProjectService.class);
		when(projects.load(any())).thenAnswer(it -> session.find(Project.class, (Long) it.getArgument(0)));
		application.when(() -> OneDev.getInstance(io.onedev.server.service.ProjectService.class)).thenReturn(projects);
	}

	@AfterEach
	void rollback() {
		session.getTransaction().rollback();
		session.close();
		if (security != null)
			security.close();
		if (application != null)
			application.close();
	}

	@Test
	void deletingProjectClearsManagedPullRequestSourceBeforeFlush() throws Exception {
		var source = project("source");
		var target = project("target");
		var request = request(target);
		request.setSourceProject(source);
		request.setStatus(PullRequest.Status.MERGED);
		session.flush();
		session.clear();
		source = session.getReference(Project.class, source.getId());
		// Project deletion loads outgoing requests; flushing must not retain the deleted source.
		service(new DefaultProjectService(Set.of())).delete(source);
		session.flush();
		session.clear();
		assertNull(session.find(Project.class, source.getId()));
		assertNull(session.find(PullRequest.class, request.getId()).getSourceProject());
		assertEquals(target.getId(), session.find(PullRequest.class, request.getId()).getTargetProject().getId());
	}

	@Test
	void deletingProjectPreservesForkWorkspaceNumberScopes() throws Exception {
		var original = project("original");
		var fork = project("fork");
		fork.setForkedFrom(original);
		var descendant = project("descendant");
		descendant.setForkedFrom(fork);
		var unrelated = project("unrelated");
		var forkWorkspace = workspace(fork, original);
		var descendantWorkspace = workspace(descendant, original);
		var unrelatedWorkspace = workspace(unrelated, unrelated);
		session.flush();
		session.clear();
		// Match ProjectResource: BaseEntityService.load obtains a Hibernate reference.
		original = session.getReference(Project.class, original.getId());

		service(new DefaultProjectService(Set.of())).delete(original);
		session.flush();
		session.clear();
		assertNull(session.find(Project.class, original.getId()));
		assertEquals(fork.getId(), session.find(Workspace.class, forkWorkspace.getId()).getNumberScope().getId());
		assertEquals(fork.getId(), session.find(Workspace.class, descendantWorkspace.getId()).getNumberScope().getId());
		assertEquals(unrelated.getId(), session.find(Workspace.class, unrelatedWorkspace.getId()).getNumberScope().getId());
		assertEquals(descendant.getId(), session.find(Workspace.class, descendantWorkspace.getId()).getProject().getId());
	}

	@Test
	void workflowDeletionCascadesReactionsAndPreservesOtherIssues() throws Exception {
		var project = project("project");
		var issue = issue(project);
		var other = issue(project); other.setState("Closed");
		var reaction = new IssueReaction(); reaction.setIssue(issue); reaction.setUser(author); reaction.setEmoji("thumbsup");
		session.persist(reaction); issue.getReactions().add(reaction);
		var comment = new IssueComment(); comment.setIssue(issue); comment.setUser(author); comment.setContent("comment");
		session.persist(comment); issue.getComments().add(comment);
		var commentReaction = new IssueCommentReaction(); commentReaction.setComment(comment); commentReaction.setUser(author); commentReaction.setEmoji("thumbsup");
		session.persist(commentReaction); comment.getReactions().add(commentReaction);
		var link = link(issue, other, linkSpec(false));
		var schedule = schedule(issue, iteration(project, "iteration"));
		var removedHistory = history(other, "Open");
		var keptHistory = history(other, "Closed");
		session.flush();
		session.clear();
		var resolution = new UndefinedStateResolution(); resolution.setFixType(UndefinedStateResolution.FixType.DELETE_THIS_STATE);
		service(new DefaultIssueService()).fixUndefinedStates(Map.of("Open", resolution));
		session.flush(); session.clear();
		assertNull(session.find(IssueLink.class, link.getId()));
		assertNull(session.find(IssueSchedule.class, schedule.getId()));
		assertNull(session.find(Issue.class, issue.getId()));
		assertNull(session.find(IssueReaction.class, reaction.getId()));
		assertNull(session.find(IssueComment.class, comment.getId()));
		assertNull(session.find(IssueCommentReaction.class, commentReaction.getId()));
		assertNull(session.find(IssueStateHistory.class, removedHistory.getId()));
		assertEquals("Closed", session.find(IssueStateHistory.class, keptHistory.getId()).getState());
		assertEquals("Closed", session.find(Issue.class, other.getId()).getState());
	}

	@Test
	void workflowDeletionStillRejectsIssuesWithWorkspaces() throws Exception {
		var issue = issue(project("project"));
		issue.getWorkspaces().add(new Workspace());
		var resolution = new UndefinedStateResolution(); resolution.setFixType(UndefinedStateResolution.FixType.DELETE_THIS_STATE);
		assertThrows(io.onedev.server.exception.NotAcceptableException.class,
				() -> service(new DefaultIssueService()).fixUndefinedStates(Map.of("Open", resolution)));
		assertTrue(session.contains(issue));
	}

	private LinkSpec linkSpec(boolean directed) {
		var spec = new LinkSpec();
		spec.setName("test-" + UUID.randomUUID());
		spec.setMultiple(true);
		if (directed) {
			var opposite = new io.onedev.server.model.support.issue.LinkSpecOpposite();
			opposite.setName("opposite-" + UUID.randomUUID());
			opposite.setMultiple(true);
			spec.setOpposite(opposite);
		}
		session.persist(spec);
		return spec;
	}

	private IssueLink link(Issue source, Issue target, LinkSpec spec) {
		var link = new IssueLink();
		link.setSource(source);
		link.setTarget(target);
		link.setSpec(spec);
		link.setPosition(1);
		session.persist(link);
		return link;
	}

	private Iteration iteration(Project project, String name) {
		var iteration = new Iteration();
		iteration.setProject(project);
		iteration.setName(name);
		session.persist(iteration);
		return iteration;
	}

	private IssueSchedule schedule(Issue issue, Iteration iteration) {
		var schedule = issue.addSchedule(iteration);
		iteration.getSchedules().add(schedule);
		session.persist(schedule);
		return schedule;
	}

	private Workspace workspace(Project project, Project numberScope) {
		var workspace = new Workspace();
		workspace.setProject(project);
		workspace.setNumberScope(numberScope);
		workspace.setNumber(++fixtureNumber);
		workspace.setUser(author);
		workspace.setSpecName("test");
		workspace.setCommitHash(HASH);
		workspace.setToken(UUID.randomUUID().toString());
		session.persist(workspace);
		return workspace;
	}

	private IssueStateHistory history(Issue issue, String state) {
		var history = new IssueStateHistory(); history.setIssue(issue); history.setState(state); history.setDate(new Date());
		session.persist(history); issue.getStateHistories().add(history); return history;
	}

	private User user(String name) {
		var user = new User();
		user.setName(name);
		session.persist(user);
		return user;
	}

	private Project project(String name) {
		var activity = new ProjectLastActivityDate();
		session.persist(activity);
		var project = new Project();
		project.setName(name);
		project.setPath(name);
		project.setLastActivityDate(activity);
		session.persist(project);
		return project;
	}

	private LastActivity activity() {
		var activity = new LastActivity();
		activity.setUser(author);
		activity.setDate(new Date());
		activity.setDescription("created");
		return activity;
	}

	private PullRequest request(Project project) {
		var request = new PullRequest();
		request.setTitle("request");
		request.setSubmitter(author);
		request.setTargetProject(project);
		request.setNumberScope(project);
		request.setSourceProject(project);
		request.setTargetBranch("main");
		request.setSourceBranch("feature");
		request.setBaseCommitHash(HASH);
		request.setMergeStrategy(MergeStrategy.CREATE_MERGE_COMMIT);
		request.setLastActivity(activity());
		request.setNumber(++fixtureNumber);
		session.persist(request);
		return request;
	}

	private Issue issue(Project project) throws Exception {
		var issue = new Issue();
		issue.setProject(project);
		issue.setNumberScope(project);
		issue.setTitle("issue");
		issue.setSubmitter(author);
		issue.setLastActivity(activity());
		var state = Issue.class.getDeclaredField("state");
		state.setAccessible(true);
		state.set(issue, "Open");
		issue.setNumber(++fixtureNumber);
		session.persist(issue);
		return issue;
	}

	private <T extends BaseEntityService<?>> T service(T service) throws Exception {
		// Keep persistence real; isolate external side effects and unrelated services.
		for (Class<?> type = service.getClass(); type != Object.class; type = type.getSuperclass()) {
			for (Field field : type.getDeclaredFields()) {
				if (field.isAnnotationPresent(Inject.class)) {
					field.setAccessible(true);
					var dependency = mock(field.getType());
					if (dependency instanceof SettingService settings) {
						when(settings.getIssueSetting()).thenReturn(issueSetting);
						when(settings.onDeleteProject(anyString())).thenReturn(new Usage());
						when(settings.onDeleteUser(anyString())).thenReturn(new Usage());
						when(settings.onDeleteGroup(anyString())).thenReturn(new Usage());
						when(settings.onDeleteLink(anyString())).thenReturn(new Usage());
						when(settings.onDeleteRole(anyString())).thenReturn(new Usage());
					}
					if (dependency instanceof io.onedev.server.service.ProjectService projects) {
						when(projects.getIds()).thenAnswer(it -> session.createQuery("select id from Project", Long.class).getResultList());
						when(projects.getSubtreeIds(any())).thenAnswer(it -> {
							var project = session.find(Project.class, (Long) it.getArgument(0));
							var subtree = new ArrayList<>(project.getDescendants());
							subtree.add(project);
							return subtree.stream().map(Project::getId).toList();
						});
					}
					if (dependency instanceof io.onedev.server.service.UserService users)
						when(users.getSystem()).thenReturn(author);
					field.set(service, dependency);
				}
			}
		}
		service.dao = mock(Dao.class);
		when(service.dao.getSession()).thenReturn(session);
		doAnswer(it -> { session.remove(it.getArgument(0)); return null; }).when(service.dao).remove(any());
		doAnswer(it -> {
			AbstractEntity entity = it.getArgument(0);
			if (entity.isNew())
				session.persist(entity);
			else if (!session.contains(entity))
				session.merge(entity);
			return null;
		}).when(service.dao).persist(any());
		return service;
	}

}
