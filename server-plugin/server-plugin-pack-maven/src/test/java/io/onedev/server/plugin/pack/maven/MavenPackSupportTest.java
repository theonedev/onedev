package io.onedev.server.plugin.pack.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.onedev.server.web.translation.Translation;

class MavenPackSupportTest {

	@ParameterizedTest
	@ValueSource(strings = {"read", "write"})
	void rendersCommandsWithoutInterpretingTranslatedProductName(String permission) throws IOException {
		var productName = "Acme \"Cloud\" $5 ${unbound} \\ tools";
		try (var translation = mockStatic(Translation.class)) {
			translation.when(() -> Translation._T(anyString())).thenAnswer(invocation -> {
				String key = invocation.getArgument(0);
				return key.replace("${productName}", productName)
						.replace("\"deploying\"", "\"en train de déployer\"")
						.replace("\"using\"", "\"en train d'utiliser\"");
			});
			var bindings = Map.<String, Object>of("permission", permission,
					"canAccessAnonymously", false, "url", "http://example.com/project/~maven");
			var mavenCommands = MavenPackSupport.getJobCommands(bindings);
			var gradleCommands = MavenPackSupport.getGradleJobCommands(bindings);
			var action = permission.equals("write") ? "en train de déployer" : "en train d'utiliser";
			for (var commands : new String[] {mavenCommands, gradleCommands}) {
				assertTrue(commands.contains(productName));
				assertTrue(commands.contains("which build is " + action + " packages"));
				assertTrue(commands.contains("package " + permission + " permission"));
				assertFalse(commands.contains("${permission"));
			}
			assertTrue(mavenCommands.contains("<id>onedev</id>"));
			assertTrue(mavenCommands.trim().endsWith(permission.equals("write") ? "deploy" : "test"));
			assertTrue(gradleCommands.contains("onedevUsername=@job_token@"));
			assertTrue(gradleCommands.trim().endsWith(permission.equals("write") ? "publish" : "build"));
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"read", "write"})
	void keepsConditionalMavenSettings(String permission) throws IOException {
		var bindings = Map.<String, Object>of("permission", permission,
				"canAccessAnonymously", true, "url", "http://example.com/project/~maven");
		var commands = MavenPackSupport.getJobCommands(bindings);
		assertFalse(commands.contains("<servers>"));
		assertEquals(permission.equals("read"), commands.contains("<mirrors>"));
	}
}
