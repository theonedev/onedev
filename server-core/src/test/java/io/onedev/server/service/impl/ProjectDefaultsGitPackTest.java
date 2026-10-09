package io.onedev.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.Set;

import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.hazelcast.map.IMap;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastActivityDate;
import io.onedev.server.model.support.code.GitPackConfig;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;

public class ProjectDefaultsGitPackTest {

	@TempDir
	Path directory;

	private void inject(DefaultProjectService service, String name, Object value) throws Exception {
		var field = DefaultProjectService.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(service, value);
	}

	private Project project(Long id) {
		var project = new Project();
		project.setId(id);
		project.setName("project" + id);
		project.setPath(project.getName());
		project.setLastActivityDate(new ProjectLastActivityDate());
		return project;
	}

	@Test
	void savingDefaultsDoesNotRefreshRepositoryConfigs() throws Exception {
		var service = new DefaultProjectService(Set.of());
		var defaults = project(Project.DEFAULT_ID);
		defaults.getGitPackConfig().setThreads("2");
		var sessions = mock(SessionService.class);
		var transactions = mock(TransactionService.class);
		var cluster = mock(ClusterService.class);
		var replicas = mock(IMap.class);
		inject(service, "sessionService", sessions);
		inject(service, "transactionService", transactions);
		inject(service, "clusterService", cluster);
		inject(service, "replicas", replicas);
		service.on(new EntityPersisted(defaults, false));
		verifyNoInteractions(sessions, transactions, cluster, replicas);
	}

	@Test
	void effectiveValuesAreWrittenAndClearedInRepositoryConfig() throws Exception {
		var service = spy(new DefaultProjectService(Set.of()));
		try (var repository = FileRepositoryBuilder.create(directory.resolve("repo.git").toFile())) {
			repository.create(true);
			doReturn(repository).when(service).getRepository(1L);
			var config = new GitPackConfig();
			config.setThreads("3");
			config.setWindowMemory("128m");
			config.setPackSizeLimit("1g");
			config.setWindow("20");
			service.checkGitConfig(1L, config);
			var stored = repository.getConfig();
			stored.load();
			assertEquals("3", stored.getString("pack", null, "threads"));
			assertEquals("128m", stored.getString("pack", null, "windowMemory"));
			assertEquals("1g", stored.getString("pack", null, "packSizeLimit"));
			assertEquals("20", stored.getString("pack", null, "window"));
			service.checkGitConfig(1L, new GitPackConfig());
			stored.load();
			assertNull(stored.getString("pack", null, "threads"));
			assertNull(stored.getString("pack", null, "windowMemory"));
			assertNull(stored.getString("pack", null, "packSizeLimit"));
			assertNull(stored.getString("pack", null, "window"));
		}
	}
}
