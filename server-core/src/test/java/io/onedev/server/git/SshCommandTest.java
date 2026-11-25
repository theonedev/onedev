package io.onedev.server.git;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.ChannelOutputStream;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.session.ServerSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.git.hook.HookUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.ssh.SshAuthenticator;
import io.onedev.server.ssh.SshService;
import io.onedev.server.util.concurrent.WorkExecutionService;
import io.onedev.server.util.facade.ProjectFacade;

class SshCommandTest {

	private static final long USER_ID = 2L;
	private static final long PROJECT_ID = 3L;
	private static final String USER_PRINCIPAL = "u:2";
	private static final String PUSH = "git-receive-pack '/example.git'";
	private static final String FORWARDED_USER_ID = "ONEDEV_SSH_USER_ID";

	private final SshAuthenticator authenticator = mock(SshAuthenticator.class);
	private final ServerSession session = mock(ServerSession.class);
	private final ProjectService projects = mock(ProjectService.class);
	private final ClusterService cluster = mock(ClusterService.class);
	private final ExecutorService executor = mock(ExecutorService.class);
	private final ExitCallback exit = mock(ExitCallback.class);
	private final Project project = new Project();

	private MockedStatic<OneDev> oneDev;
	private MockedStatic<SecurityUtils> security;

	@BeforeEach
	void setUp() {
		oneDev = mockStatic(OneDev.class);
		oneDev.when(() -> OneDev.getInstance(SshAuthenticator.class)).thenReturn(authenticator);
		oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
		oneDev.when(() -> OneDev.getInstance(SessionService.class)).thenReturn(mock(SessionService.class));
		oneDev.when(() -> OneDev.getInstance(ClusterService.class)).thenReturn(cluster);
		oneDev.when(() -> OneDev.getInstance(ExecutorService.class)).thenReturn(executor);

		var facade = mock(ProjectFacade.class);
		when(facade.getId()).thenReturn(PROJECT_ID);
		when(projects.findFacadeByPath("example")).thenReturn(facade);
		when(projects.load(PROJECT_ID)).thenReturn(project);
		when(projects.getActiveServer(PROJECT_ID, true)).thenReturn("replica");
		when(cluster.getLocalServerAddress()).thenReturn("entry");
		when(authenticator.getPublicKeyOwnerId(session)).thenReturn(USER_ID);

		var principals = mock(PrincipalCollection.class);
		security = mockStatic(SecurityUtils.class);
		security.when(() -> SecurityUtils.asUserPrincipal(USER_ID)).thenReturn(USER_PRINCIPAL);
		security.when(() -> SecurityUtils.asPrincipals(USER_PRINCIPAL)).thenReturn(principals);
		security.when(() -> SecurityUtils.asSubject(principals)).thenReturn(mock(Subject.class));
	}

	@AfterEach
	void tearDown() {
		ThreadContext.unbindSubject();
		security.close();
		oneDev.close();
	}

	@Test
	void deniesForwardingWithoutProjectAccess() throws Exception {
		start(PUSH, Map.of());

		verify(exit).onExit(-1);
		verifyNoInteractions(cluster, executor);
	}

	@ParameterizedTest
	@ValueSource(strings = {"git-upload-pack", "git-receive-pack"})
	void deniesForwardingWithoutCodePermission(String operation) throws Exception {
		security.when(() -> SecurityUtils.canAccessProject(project)).thenReturn(true);

		start(operation + " '/example.git'", Map.of());

		verify(exit).onExit(-1);
		verifyNoInteractions(cluster, executor);
	}

	@Test
	void forwardsTheAuthenticatedUserAndGitProtocol() throws Exception {
		allowPush();
		var ssh = mock(SshService.class);
		var client = mock(ClientSession.class);
		var channel = mock(ChannelExec.class);
		oneDev.when(() -> OneDev.getInstance(SshService.class)).thenReturn(ssh);
		when(ssh.ssh("replica")).thenReturn(client);
		when(client.createExecChannel(anyString(), isNull(), anyMap())).thenReturn(channel);
		when(channel.open()).thenReturn(mock(OpenFuture.class));
		when(channel.isClosed()).thenReturn(true);
		when(channel.getExitStatus()).thenReturn(0);
		when(executor.submit(any(Runnable.class))).thenAnswer(invocation -> {
			invocation.getArgument(0, Runnable.class).run();
			return null;
		});

		start(PUSH, Map.of(FORWARDED_USER_ID, "1", "GIT_PROTOCOL", "version=2"));

		verify(client).createExecChannel(eq(SshCommand.FORWARD_PREFIX + PUSH), isNull(),
				eq(Map.of(FORWARDED_USER_ID, Long.toString(USER_ID), "GIT_PROTOCOL", "version=2")));
		verify(exit).onExit(0);
	}

	@Test
	void rejectsForwardedIdentityFromAUserConnection() throws Exception {
		start(SshCommand.FORWARD_PREFIX + PUSH, Map.of(FORWARDED_USER_ID, Long.toString(USER_ID)));

		verify(exit).onExit(-1);
		verifyNoInteractions(projects);
	}

	@Test
	void rejectsAnOldStyleClusterRequest() throws Exception {
		when(authenticator.getPublicKeyOwnerId(session)).thenReturn(User.SYSTEM_ID);

		start(PUSH, Map.of());

		verify(exit).onExit(-1);
		verifyNoInteractions(projects);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"-1", "invalid"})
	void rejectsInvalidForwardedUsers(String userId) throws Exception {
		when(authenticator.getPublicKeyOwnerId(session)).thenReturn(User.SYSTEM_ID);

		start(SshCommand.FORWARD_PREFIX + PUSH, userId != null ? Map.of(FORWARDED_USER_ID, userId) : Map.of());

		verify(exit).onExit(-1);
		verifyNoInteractions(projects);
	}

	@Test
	void usesTheForwardedUserForHooksOnTheReplica() throws Exception {
		allowPush();
		when(authenticator.getPublicKeyOwnerId(session)).thenReturn(User.SYSTEM_ID);
		when(cluster.getLocalServerAddress()).thenReturn("replica");
		when(projects.getGitDir(PROJECT_ID)).thenReturn(new File("example.git"));
		var work = mock(WorkExecutionService.class);
		oneDev.when(() -> OneDev.getInstance(WorkExecutionService.class)).thenReturn(work);

		try (var hooks = mockStatic(HookUtils.class)) {
			start(SshCommand.FORWARD_PREFIX + PUSH, Map.of(FORWARDED_USER_ID, Long.toString(USER_ID)));

			security.verify(() -> SecurityUtils.canWriteCode(project));
			hooks.verify(() -> HookUtils.getReceiveHookEnvs(PROJECT_ID, USER_PRINCIPAL));
			verify(work).submit(eq(2), eq("git-over-ssh-3-u:2"), any(Runnable.class));
		}
	}

	private void allowPush() {
		security.when(() -> SecurityUtils.canAccessProject(project)).thenReturn(true);
		security.when(() -> SecurityUtils.canWriteCode(project)).thenReturn(true);
	}

	private void start(String commandString, Map<String, String> environments) throws Exception {
		var command = (SshCommand) new SshCommandCreator().createCommand(commandString, environments);
		command.setSession(session);
		command.setErrorStream(mock(ChannelOutputStream.class));
		command.setExitCallback(exit);
		command.start(null, null);
	}
}
