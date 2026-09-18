package io.onedev.server.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.SettingService;
import io.onedev.server.validation.HibernateValidationTestSupport;

public class SettingResourceTest extends HibernateValidationTestSupport {

	@Test
	public void acceptsJsonBoardColumnsBeforeValidatingEditColumns() throws Exception {
		var setting = issueSetting("[\"Open\",\"Closed\"]");
		var board = setting.getBoardSpecs().get(0);
		assertTrue(board.getEditColumns().isEmpty());
		assertPaths(validator.validate(setting), "boardSpecs[0].editColumns");

		var settingService = mock(SettingService.class);
		var auditService = mock(AuditService.class);
		var resource = new SettingResource(settingService, auditService, validator);
		// Simulate the parameter validation Jersey performs before invoking the resource.
		var method = SettingResource.class.getMethod("setIssueSetting", GlobalIssueSetting.class);
		assertTrue(validator.forExecutables().validateParameters(resource, method, new Object[] {setting}).isEmpty());

		try (var security = mockStatic(SecurityUtils.class);
				var xml = mockStatic(VersionedXmlDoc.class)) {
			security.when(SecurityUtils::isAdministrator).thenReturn(true);
			when(settingService.getIssueSetting()).thenReturn(new GlobalIssueSetting());
			var document = mock(VersionedXmlDoc.class);
			when(document.toXML()).thenReturn("<settings/>");
			xml.when(() -> VersionedXmlDoc.fromBean(any())).thenReturn(document);

			try (var response = resource.setIssueSetting(setting)) {
				assertEquals(200, response.getStatus());
			}
			verify(settingService).saveIssueSetting(setting);
			assertFalse(setting.isReconciled());
			assertEquals(List.of("Open", "Closed"), board.getColumns());
			assertPaths(validator.validate(setting));
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"[]", "[\"Open\"]"})
	public void rejectsTooFewBoardColumns(String columns) throws Exception {
		assertRejected(issueSetting(columns), "boardSpecs[0].editColumns: At least two columns need to be defined");
	}

	@Test
	public void stillValidatesOtherIssueSettings() throws Exception {
		var setting = issueSetting("[\"Open\",\"Closed\"]");
		setting.setBoardSpecs(null);
		assertRejected(setting, "boardSpecs:");
	}

	private void assertRejected(GlobalIssueSetting setting, String expectedMessage) {
		var settingService = mock(SettingService.class);
		var auditService = mock(AuditService.class);
		var resource = new SettingResource(settingService, auditService, validator);
		try (var security = mockStatic(SecurityUtils.class)) {
			security.when(SecurityUtils::isAdministrator).thenReturn(true);
			var exception = assertThrows(ExplicitException.class, () -> resource.setIssueSetting(setting));
			assertTrue(exception.getMessage().contains(expectedMessage), exception.getMessage());
			verifyNoInteractions(settingService, auditService);
		}
	}

	private GlobalIssueSetting issueSetting(String columns) throws Exception {
		var board = new ObjectMapper().readValue("{\"name\":\"Board\",\"identifyField\":\"State\","
				+ "\"backlogBaseQuery\":null,\"columns\":" + columns + "}", BoardSpec.class);
		var setting = new GlobalIssueSetting();
		setting.setStateSpecs(List.of());
		setting.setTransitionSpecs(List.of());
		setting.setFieldSpecs(List.of());
		setting.setNamedQueries(List.of());
		setting.setBoardSpecs(List.of(board));
		return setting;
	}
}
