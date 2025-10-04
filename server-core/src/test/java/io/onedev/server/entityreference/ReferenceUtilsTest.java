package io.onedev.server.entityreference;

import static io.onedev.server.entityreference.ReferenceUtils.extractReferences;
import static io.onedev.server.entityreference.ReferenceUtils.transformReferences;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import org.unbescape.html.HtmlEscape;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;

public class ReferenceUtilsTest extends AppLoaderMocker {

    @Test
    public void test() {
		var projectService = mock(ProjectService.class);
		var settingService = mock(SettingService.class);
		Mockito.when(AppLoader.getInstance(ProjectService.class)).thenReturn(projectService);
		Mockito.when(AppLoader.getInstance(SettingService.class)).thenReturn(settingService);
		var project1 = new Project();
		project1.setId(1L);
		project1.setKey("ONE");
		project1.setPath("test/project1");
		when(projectService.load(1L)).thenReturn(project1);
		when(projectService.findByKey("ONE")).thenReturn(project1);
		when(projectService.findByPath("test/project1")).thenReturn(project1);
		var project2 = new Project();
		project2.setId(2L);
		project2.setKey("TWO");
		project2.setPath("test/project2");
		when(projectService.load(2L)).thenReturn(project2);
		when(projectService.findByKey("TWO")).thenReturn(project2);
		when(projectService.findByPath("test/project2")).thenReturn(project2);
		when(settingService.getIssueSetting()).thenReturn(new GlobalIssueSetting());

		assertEquals("<a href='https://example.com'>A test issue</a>", transformReferences("A test issue", project1, ((reference, text) -> {
			if (reference != null)
				return text;
			else
				return "<a href='https://example.com'>" + text + "</a>";
		})));
		
		assertEquals("hello - world", transformReferences("hello #1 world", project1, (reference, text) -> {
			if (reference != null)
				return "-";
			else 
				return text;
		}));
		assertEquals(
				"<a href='https://example.com'>&lt;hello </a><a href='https://example.com/1'>ONE-1</a><a href='https://example.com'>,</a><a href='https://example.com/2'>build TWO-2</a><a href='https://example.com'> world&gt;</a>", 
				transformReferences("<hello ONE-1,build TWO-2 world>", project1, (reference, text) -> {
			if (reference != null)
				return String.format("<a href='https://example.com/%d'>%s</a>", reference.getNumber(), text);
			else
				return "<a href='https://example.com'>" + HtmlEscape.escapeHtml5(text) + "</a>";
		}));
		assertEquals("hello world", transformReferences("hello world", null, (reference, text) -> text));
		
		var references = extractReferences("#1 pull  request  #2 issue #3", project1);
		assertEquals(3, references.size());
		assertTrue(references.get(0) instanceof IssueReference);
		assertEquals(1L, references.get(0).getNumber().longValue());
		assertTrue(references.get(1) instanceof PullRequestReference);
		assertEquals(2L, references.get(1).getNumber().longValue());
		assertTrue(references.get(2) instanceof IssueReference);
		assertEquals(3L, references.get(2).getNumber().longValue());
		
		references = extractReferences("feat(docs): this is doc (build  #3)", project1);
		assertEquals(1, references.size());
		assertTrue(references.get(0) instanceof BuildReference);
		assertEquals(3L, references.get(0).getNumber().longValue());

		references = extractReferences("feat(docs): this is doc (test/project2#3)", project1);
		assertEquals(1, references.size());
		assertTrue(references.get(0) instanceof IssueReference);
		assertEquals(3L, references.get(0).getNumber().longValue());
		
 		references = extractReferences("feat(docs): this is doc (pr ONE-3)", project1);
		assertEquals(1, references.size());
		assertTrue(references.get(0) instanceof PullRequestReference);
		assertEquals(3L, references.get(0).getNumber().longValue());
    }

	@Override
	protected void setup() {
		
	}

	@Override
	protected void teardown() {

	}
}