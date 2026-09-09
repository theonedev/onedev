package io.onedev.server.web.editable.polymorphic;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;

import java.util.Set;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.jsoup.Jsoup;
import org.junit.Test;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.DefaultImplementationRegistry;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.wiki.WikiSetting;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.translation.Translation;
import io.onedev.server.web.util.ProjectAware;

public class PolymorphicPropertyEditorTest {

	@Test
	public void displaysProjectSpecificFolderPlaceholder() {
		var tester = new WicketTester();
		tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(false);
		try (var loader = mockStatic(AppLoader.class); var translation = mockStatic(Translation.class)) {
			loader.when(() -> AppLoader.getInstance(ImplementationRegistry.class))
					.thenReturn(new DefaultImplementationRegistry(Set.of()));
			translation.when(() -> Translation._T("Inherit from parent")).thenReturn("Inherit from parent");
			translation.when(() -> Translation._T("Use wiki folder")).thenReturn("Use wiki folder");
			var parent = new Project();
			var child = new Project();
			child.setParent(parent);
			for (var project : new Project[] {parent, child}) {
				var editor = new ProjectFolderEditor(project);
				tester.startComponentInPage(editor);
				var document = Jsoup.parse(tester.getLastResponseAsString());
				assertEquals(project == parent ? "Use wiki folder" : "Inherit from parent",
						document.selectFirst("select option[value='']").text());
			}
		} finally {
			tester.destroy();
		}
	}
	private static class ProjectFolderEditor extends PolymorphicPropertyEditor implements ProjectAware {

		private final Project project;

		ProjectFolderEditor(Project project) {
			super("editor", new PropertyDescriptor(WikiSetting.class, "folder"), Model.of());
			this.project = project;
		}

		@Override
		public Project getProject() {
			return project;
		}

		@Override
		public void renderHead(IHeaderResponse response) {
		}
	}

}
