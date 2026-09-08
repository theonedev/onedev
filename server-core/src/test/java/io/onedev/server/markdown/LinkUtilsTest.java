package io.onedev.server.markdown;

import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.junit.Test;

public class LinkUtilsTest {
	@Test
	public void rendersOutsideWikiImagesWithRawQueryAndOriginalSuffix() {
		var document = Jsoup.parse("<img src='../images/logo%20dark.png?v=2#preview'>"
				+ "<a href='../downloads/manual.pdf#page=3'>manual</a>");
		LinkUtils.resolveRelativeLinks(document, "wiki",
				(path, url) -> LinkUtils.appendSuffix("/project/~files/main/" + path, url),
				(path, url) -> LinkUtils.appendSuffix("/project/~files/main/" + path + "?raw=true", url),
				path -> false, path -> null, null);
		assertEquals("/project/~files/main/images/logo dark.png?raw=true&v=2#preview",
				document.selectFirst("img").attr("src"));
		assertEquals("/project/~files/main/downloads/manual.pdf#page=3",
				document.selectFirst("a").attr("href"));
		assertEquals("/image?raw=true#preview", LinkUtils.appendSuffix("/image?raw=true", "image#preview"));
	}

	@Test
	public void resolvesRepositoryPathsAndPreservesUrlPolicies() {
		var document = Jsoup.parse("<a href='../Missing%20file.md?q=1#heading'>file</a>"
				+ "<img src='image.png#preview'><a href='locked.md'>locked</a>"
				+ "<a href='#local'>anchor</a><a href='https://example.com'>external</a>");
		LinkUtils.resolveRelativeLinks(document, "docs/guide",
				(path, url) -> LinkUtils.appendSuffix("/blob/" + path, url),
				(path, url) -> LinkUtils.appendSuffix("/raw/" + path, url),
				path -> true, path -> path.endsWith("locked.md") ? null : "/new/" + path, "/icons#plus");
		assertEquals("/blob/docs/Missing file.md?q=1#heading", document.selectFirst("a").attr("href"));
		assertEquals("/raw/docs/guide/image.png#preview", document.selectFirst("img").attr("src"));
		assertEquals(3, document.select("span.missing").size());
		assertEquals(1, document.select("a.add-missing").size());
		assertEquals("/new/docs/Missing file.md", document.selectFirst("a.add-missing").attr("href"));
		assertEquals(1, document.select("a[href=#local]").size());
		assertEquals(1, document.select("a[href=https://example.com]").size());
	}

	@Test
	public void leavesUnresolvableLinksUntouched() {
		var document = Jsoup.parse("<a href='../../outside.md'>outside</a><a href='skip.md'>skip</a>");
		LinkUtils.resolveRelativeLinks(document, "docs", (path, url) -> null,
				(path, url) -> null, path -> true, path -> "/new/" + path, "/icons#plus");
		assertEquals("../../outside.md", document.selectFirst("a").attr("href"));
		assertTrue(document.select("span.missing").isEmpty());
	}
}
