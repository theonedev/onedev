package io.onedev.server.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;

import io.onedev.agent.workspace.ProvisionMode;
import io.onedev.server.model.support.workspace.spec.CacheConfig;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;

class WorkspaceCachePathValidationTest {

	@Test
	void rejectsLiteralAbsoluteCachePathsOnlyForBareboneWorkspaces() {
		for (var mode : ProvisionMode.values()) {
			for (var path : List.of("cache", "/opt/cache", "C:\\cache", "/opt/@project_path@/cache")) {
				var spec = new WorkspaceSpec();
				spec.setProvisionMode(mode);
				var cache = new CacheConfig();
				cache.setPaths(List.of(path));
				spec.setCacheConfigs(List.of(cache));
				var context = mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);
				var absoluteLiteral = path.equals("/opt/cache") || path.equals("C:\\cache");
				assertEquals(mode != ProvisionMode.HOST || !absoluteLiteral, spec.isValid(context),
						mode + ": " + path);
			}
		}
	}

	@Test
	void rejectsDuplicateCachePathsInEveryMode() {
		for (var mode : ProvisionMode.values()) {
			var spec = new WorkspaceSpec();
			spec.setProvisionMode(mode);
			var cache = new CacheConfig();
			cache.setPaths(List.of("cache", "cache"));
			spec.setCacheConfigs(List.of(cache));
			var context = mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);
			assertFalse(spec.isValid(context));
			verify(context).buildConstraintViolationWithTemplate("Item #1: duplicate path 'cache'");
		}
	}

}
