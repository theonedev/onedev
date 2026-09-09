package io.onedev.server.web.page.project.wiki;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import io.onedev.server.OneDev;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;

public class ProjectWikiPageTest {

	@Test
	public void resolvesSubmoduleBeforeReadingHomeAndAlwaysDeniesEdits() throws Exception {
		var owner = mock(Project.class);
		var target = mock(Project.class);
		var git = mock(GitService.class);
		var projects = mock(ProjectService.class);
		var settings = mock(SettingService.class);
		var system = mock(SystemSetting.class);
		when(settings.getSystemSetting()).thenReturn(system);
		when(system.getServerUrl()).thenReturn("https://server");
		when(owner.getPath()).thenReturn("app");
		when(target.getId()).thenReturn(2L);
		when(projects.load(2L)).thenReturn(target);
		var ownerCommit = ObjectId.fromString("1111111111111111111111111111111111111111");
		var targetCommit = ObjectId.fromString("2222222222222222222222222222222222222222");
		var ident = new BlobIdent(ownerCommit.name(), "wiki", FileMode.GITLINK.getBits());
		when(git.getBlobIdent(owner, ownerCommit, "wiki")).thenReturn(ident);
		when(target.getObjectId(targetCommit.name(), false)).thenReturn(targetCommit);
		when(target.getRevCommit(targetCommit, false)).thenReturn(mock(RevCommit.class));
		try (var oneDev = mockStatic(OneDev.class); var security = mockStatic(SecurityUtils.class)) {
			oneDev.when(() -> OneDev.getInstance(GitService.class)).thenReturn(git);
			oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
			oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
			for (String scenario : new String[] {"external", "missing", "denied", "allowed"}) {
				clearInvocations(target);
				String url = scenario.equals("external") ? "https://other/docs" : "https://server/docs";
				byte[] content = (url + ":" + targetCommit.name()).getBytes(StandardCharsets.UTF_8);
				when(owner.getBlob(ident, true)).thenReturn(new Blob(ident, ownerCommit, content, content.length));
				when(projects.findByPath("docs")).thenReturn(scenario.equals("missing") ? null : target);
				security.when(() -> SecurityUtils.canReadCode(target)).thenReturn(scenario.equals("allowed"));
				var page = mock(ProjectWikiPage.class, CALLS_REAL_METHODS);
				doReturn(owner).when(page).getProject();
				set(page, "folder", "wiki");
				set(page, "commitId", ownerCommit);
				invoke(page, "resolveWikiProject");
				assertEquals(false, invoke(page, "canEdit", "Home.md"));
				invoke(page, "read", "Home");
				if (scenario.equals("allowed")) {
					assertNull(get(page, "wikiWarning"));
					verify(target).getBlob(argThat(it -> targetCommit.name().equals(it.revision)
							&& "Home.md".equals(it.path)), eq(false));
				} else {
					assertNotNull(get(page, "wikiWarning"));
					verifyNoInteractions(target);
				}
			}
		}
	}

	private static void set(ProjectWikiPage page, String name, Object value) throws Exception {
		var field = ProjectWikiPage.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(page, value);
	}

	private static Object get(ProjectWikiPage page, String name) throws Exception {
		var field = ProjectWikiPage.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(page);
	}

	private static Object invoke(ProjectWikiPage page, String name, String... args) throws Exception {
		var method = args.length == 0 ? ProjectWikiPage.class.getDeclaredMethod(name)
				: ProjectWikiPage.class.getDeclaredMethod(name, String.class);
		method.setAccessible(true);
		return method.invoke(page, (Object[]) args);
	}
}
