package io.onedev.server.buildspec.step;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.onedev.server.annotation.ReservedOptions;
import io.onedev.server.validation.validator.ReservedOptionsValidator;
import jakarta.validation.ConstraintValidatorContext;

class PruneBuilderCacheStepTest {
	@Test
	void rejectsBothFormsOfReservedOptions() throws Exception {
		var validator = new ReservedOptionsValidator();
		validator.initialize(PruneBuilderCacheStep.class.getMethod("getOptions").getAnnotation(ReservedOptions.class));
		var context = mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);
		for (var options : List.of("--builder other", "--builder=other", "--all --builder=other",
				"--force", "--force=false", "-f", "-f=false")) {
			assertFalse(validator.isValid(options, context), options);
		}
		assertTrue(validator.isValid(null, context));
		assertTrue(validator.isValid("--all --filter=until=24h", context));
	}
}
