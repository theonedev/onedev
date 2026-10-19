package io.onedev.server.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.onedev.server.OneDev;
import io.onedev.server.model.support.administration.BrandingSetting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.oem.OemUtils;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.facade.UserFacade;

class ProductDisplayNameTest {

	@Test
	void systemDisplayNameDoesNotChangeLoginOrGitIdentity() {
		var settings = mock(SettingService.class);
		var systemSetting = new SystemSetting();
		systemSetting.setNoreplyEmailDomain("example.com");
		when(settings.getSystemSetting()).thenReturn(systemSetting);
		try (var oem = mockStatic(OemUtils.class); var oneDev = mockStatic(OneDev.class)) {
			oem.when(OemUtils::getProductName).thenReturn("Acme");
			oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
			var user = new User();
			user.setId(User.SYSTEM_ID);
			user.setName(User.SYSTEM_NAME);
			assertEquals("Acme", user.getDisplayName());
			assertEquals("OneDev", user.getName());
			assertEquals("OneDev", user.asPerson().getName());
			assertEquals("onedev@example.com", user.asPerson().getEmailAddress());
			var facade = new UserFacade(User.SYSTEM_ID, User.SYSTEM_NAME, null,
					User.Type.SERVICE, false, false, false);
			assertEquals("Acme", facade.getDisplayName());
			assertEquals("OneDev", facade.getName());
		}
	}

	@Test
	void ordinaryUsersAndCustomBrandingKeepTheirNames() {
		try (var oem = mockStatic(OemUtils.class)) {
			oem.when(OemUtils::getProductName).thenReturn("Acme");
			var user = new User();
			user.setId(10L);
			user.setName("OneDev");
			assertEquals("OneDev", user.getDisplayName());
			user.setFullName("OneDev Community");
			assertEquals("OneDev Community", user.getDisplayName());
			var facade = new UserFacade(10L, "OneDev", "OneDev Community",
					User.Type.ORDINARY, false, false, false);
			assertEquals("OneDev Community", facade.getDisplayName());
			var branding = new BrandingSetting();
			assertEquals("Acme", branding.getName());
			branding.setName("OneDev Community");
			assertEquals("OneDev Community", branding.getName());
			branding.setName("OneDev");
			assertEquals("Acme", branding.getName());
		}
	}

}
