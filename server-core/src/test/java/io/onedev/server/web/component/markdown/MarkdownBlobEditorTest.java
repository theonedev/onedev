package io.onedev.server.web.component.markdown;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;

import java.nio.charset.StandardCharsets;
import static org.mockito.Mockito.*;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Test;

import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;

public class MarkdownBlobEditorTest {

	@Test
	public void repositoryPickerInsertsRelativeLinksWithDefaultAndExplicitLabels() throws Exception {
		BlobRenderContext context = mock(BlobRenderContext.class);
		Project project = mock(Project.class);
		when(context.getProject()).thenReturn(project);
		when(project.getPath()).thenReturn("project");
		when(context.getBlobIdent()).thenReturn(new BlobIdent("main", "docs/Guide.md"));
		when(context.getDirectoryUrl()).thenReturn("/project/~files/main/docs");
		BlobMarkdownEditor editor = mock(BlobMarkdownEditor.class, CALLS_REAL_METHODS);
		var contextField = BlobMarkdownEditor.class.getDeclaredField("context");
		contextField.setAccessible(true);
		contextField.set(editor, context);
		doReturn("/project/~files/main/assets/image.png").when(editor)
				.urlFor(eq(ProjectBlobPage.class), any(PageParameters.class));
		doNothing().when(editor).insertUrl(any(), anyBoolean(), anyString(), anyString(), isNull());
		AjaxRequestTarget target = mock(AjaxRequestTarget.class);

		var support = editor.getRepositoryFileSelectionSupport();
		support.onSelect(target, "assets/image.png", true, null);
		verify(editor).insertUrl(target, true, "../assets/image.png", "image.png", null);
		support.onSelect(target, "assets/image.png", false, "Download");
		verify(editor).insertUrl(target, false, "../assets/image.png", "Download", null);
	}

	@Test
	public void conversionPreservesLineEndingsWhitespaceAndUnicode() throws Exception {
		MarkdownEditor input = mock(MarkdownEditor.class);
		BlobMarkdownEditor editor = mock(BlobMarkdownEditor.class, CALLS_REAL_METHODS);
		var field = BlobMarkdownEditor.class.getDeclaredField("input");
		field.setAccessible(true);
		field.set(editor, input);

		when(input.getModelObject()).thenReturn("Original\n");
		when(input.getConvertedInput()).thenReturn("  文本  \r\n\r\n");
		editor.convertInput();
		assertEquals("  文本  \n\n", new String(editor.getConvertedInput(), StandardCharsets.UTF_8));

		when(input.getModelObject()).thenReturn("Original\r\n");
		editor.convertInput();
		assertEquals("  文本  \r\n\r\n", new String(editor.getConvertedInput(), StandardCharsets.UTF_8));

		when(input.getConvertedInput()).thenReturn(null);
		editor.convertInput();
		assertArrayEquals(new byte[0], editor.getConvertedInput());
	}
}
