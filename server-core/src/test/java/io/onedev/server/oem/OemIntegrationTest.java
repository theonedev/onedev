package io.onedev.server.oem;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.translation.TranslationTagHandler;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupFactory;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static io.onedev.server.web.translation.Translation._T;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OemIntegrationTest {

	@TempDir
	Path installDir;

	@Test
	public void customBrandingWorksInTranslationsAnnotationsAndMarkup() throws Exception {
		Files.createDirectories(installDir.resolve("conf"));
		Files.writeString(installDir.resolve("conf/oem.properties"), """
				product_name=Acme Dev
				website=https://example.com/product/
				docsite=https://help.example.com/manual/
				""");
		var previousInstallDir = Bootstrap.installDir;
		Bootstrap.installDir = installDir.toFile();
		try (var oneDev = mockStatic(OneDev.class)) {
			var service = new DefaultOemService();
			oneDev.when(() -> OneDev.getInstance(OemService.class)).thenReturn(service);
			var tester = new WicketTester(new OemApplication());
			try {
				tester.getSession().bind();
				tester.getSession().setLocale(Locale.SIMPLIFIED_CHINESE);
				assertEquals("Untranslated OneDev message", _T("Untranslated OneDev message"));
				assertEquals("Untranslated Acme Dev message", _T("Untranslated ${productName} message"));
				var webhookDescription = EditableUtils.getDescription(
						io.onedev.server.model.support.WebHook.class.getMethod("getSecret"));
				assertTrue(webhookDescription.contains("Acme Dev"), webhookDescription);
				assertTrue(webhookDescription.contains("X-OneDev-Signature"), webhookDescription);
				var description = EditableUtils.getDescription(Job.class.getMethod("getSteps"));
				assertTrue(description.contains("https://help.example.com/manual/concepts#job-workdir"));
				assertFalse(description.contains("${docsite}"), description);
				assertEquals("Visit https://onedev.io and https://docs.onedev.io",
						_T("Visit https://onedev.io and https://docs.onedev.io"));
				assertEquals("Visit https://example.com/product/new-page?x=1#section",
						_T("Visit ${website}/new-page?x=1#section"));
				assertEquals("https://help.example.com/manual/new-page",
						_T("nt: ${docsite}/new-page"));

				tester.startPage(new OemPage());
				var html = tester.getLastResponseAsString();
				assertTrue(html.contains("href=\"https://example.com/product/pricing\""));
				assertTrue(html.contains("action=\"https://example.com/product/support-request\""));
				assertTrue(html.contains("href=\"https://help.example.com/manual/guide#setup\""));
				assertTrue(html.contains("https://help.example.com/manual/concepts#"), html);
				assertFalse(html.contains("${website}"));
				assertFalse(html.contains("${docsite}"));
				assertFalse(html.contains(OemService.DEFAULT_WEBSITE));
			} finally {
				tester.destroy();
			}
		} finally {
			Bootstrap.installDir = previousInstallDir;
		}
	}

	@Test
	public void productNamesAreLiteralAndMarkupIsEscaped() {
		var productName = "Acme <Dev> & ${website} $1 \\ Suite";
		try (var oneDev = mockStatic(OneDev.class)) {
			var service = mock(OemService.class);
			when(service.getProductName()).thenReturn(productName);
			when(service.getWebsite()).thenReturn("https://example.com");
			oneDev.when(() -> OneDev.getInstance(OemService.class)).thenReturn(service);
			assertEquals(productName + " https://example.com", OemUtils.interpolate("${productName} ${website}"));
			assertEquals("OneDev", OemUtils.translateHtml("OneDev"));
			assertEquals("Acme &lt;Dev&gt; &amp; ${website} $1 \\ Suite", OemUtils.translateHtml("${productName}"));
			var tester = new WicketTester(new OemApplication());
			try {
				tester.getSession().setLocale(Locale.ENGLISH);
				assertEquals(productName, _T("${productName}"));
				assertEquals("Welcome to OneDev", _T("Welcome to OneDev"));
				assertEquals("io.onedev.server.OneDev OneDev.class OneDevAuthorization X-OneDev-Signature "
						+ "$OneDev-Secret-Literal$ /tmp/OneDev OneDev.singleFile",
						_T("io.onedev.server.OneDev OneDev.class OneDevAuthorization X-OneDev-Signature "
								+ "$OneDev-Secret-Literal$ /tmp/OneDev OneDev.singleFile"));
				tester.startPage(new ProductPage());
				var html = tester.getLastResponseAsString();
				assertTrue(html.contains("Acme &lt;Dev&gt; &amp; ${website} $1 \\ Suite"), html);
				assertFalse(html.contains("<Dev>"), html);
				assertFalse(html.contains("&amp;lt;"), html);
			} finally {
				tester.destroy();
			}
		}
	}

	@Test
	public void missingAndBlankPropertiesKeepIndependentDefaults() throws Exception {
		var previousInstallDir = Bootstrap.installDir;
		Bootstrap.installDir = installDir.toFile();
		try {
			var service = new DefaultOemService();
			assertEquals("OneDev", service.getProductName());
			assertEquals(OemService.DEFAULT_WEBSITE, service.getWebsite());
			assertEquals(OemService.DEFAULT_DOCSITE, service.getDocsite());
			Files.createDirectories(installDir.resolve("conf"));
			Files.writeString(installDir.resolve("conf/oem.properties"), "website=https://example.com/\ndocsite= \n");
			service = new DefaultOemService();
			assertEquals("OneDev", service.getProductName());
			assertEquals("https://example.com", service.getWebsite());
			assertEquals(OemService.DEFAULT_DOCSITE, service.getDocsite());
		} finally {
			Bootstrap.installDir = previousInstallDir;
		}
	}

	private static class OemApplication extends MockApplication {
		@Override
		protected void init() {
			super.init();
			getMarkupSettings().setMarkupFactory(new MarkupFactory() {
				@Override
				public MarkupParser newMarkupParser(MarkupResourceStream resource) {
					var parser = new MarkupParser(newXmlPullParser(), resource);
					parser.add(new TranslationTagHandler(resource));
					return parser;
				}
			});
			getPageSettings().addComponentResolver(new OemTranslationResolver());
			getPageSettings().addComponentResolver(new TranslationTagHandler());
			getResourceSettings().getStringResourceLoaders().add(0, new OemStringResourceLoader());
		}
	}

	public static class ProductPage extends WebPage implements IMarkupResourceStreamProvider {
		public ProductPage() {
			add(new Label("product", _T("${productName}")));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
			return new StringResourceStream("""
					<html xmlns:wicket="http://wicket.apache.org"><body>
					<span wicket:id="product"></span><wicket:t>${productName}</wicket:t>
					</body></html>
					""");
		}
	}

	public static class OemPage extends WebPage implements IMarkupResourceStreamProvider {
		public OemPage() {
			add(new WebMarkupContainer("existing"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
			return new StringResourceStream("""
					<html xmlns:wicket="http://wicket.apache.org"><body>
					<a t:href="nt: ${website}/pricing">Order</a>
					<form t:action="nt: ${website}/support-request" method="post"></form>
					<a wicket:id="existing" t:href="nt: ${docsite}/guide#setup">Help</a>
					<wicket:t>No previous successful build on <a href="${docsite}/concepts#build-stream" class="link-primary" target="_blank">same stream</a> to calculate fixed issues since</wicket:t>
					</body></html>
					""");
		}
	}

}
