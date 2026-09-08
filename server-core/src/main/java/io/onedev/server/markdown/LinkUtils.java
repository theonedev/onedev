package io.onedev.server.markdown;

import java.net.URISyntaxException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.ExceptionUtils;

import io.onedev.commons.utils.PathUtils;
import io.onedev.server.git.GitUtils;
import io.onedev.server.util.UrlUtils;

/** Shared repository link traversal; callers supply navigation and creation policies. */
public class LinkUtils {

	private static final Logger logger = LoggerFactory.getLogger(LinkUtils.class);

	public static void resolveRelativeLinks(Document document, String directory,
			BiFunction<String, String, String> linkUrlOf, BiFunction<String, String, String> imageUrlOf,
			Predicate<String> missing, Function<String, String> addUrlOf, String addIconHref) {
		for (Element element : document.select("a[href], img[src]")) {
			try {
				String attribute = element.tagName().equals("img") ? "src" : "href";
				String url = element.attr(attribute).trim();
				if (!url.isEmpty() && UrlUtils.isRelative(url) && !url.startsWith("#")) {
					String relativePath = UrlUtils.decodePath(UrlUtils.trimHashAndQuery(url));
					String referencedPath = GitUtils.normalizePath(PathUtils.resolve(directory, relativePath));
					if (referencedPath != null) {
						String resolvedUrl = element.tagName().equals("img")
								? imageUrlOf.apply(referencedPath, url) : linkUrlOf.apply(referencedPath, url);
						if (resolvedUrl != null) {
							element.attr(attribute, resolvedUrl);
							if (missing.test(referencedPath)) {
								markMissing(element, element.tagName().equals("img") ? null : addUrlOf.apply(referencedPath),
										addIconHref, "Add this file");
							}
						}
					}
				}
			} catch (Exception e) {
				if (ExceptionUtils.find(e, URISyntaxException.class) != null)
					logger.error("Error parsing url", e);
				else
					throw ExceptionUtils.unchecked(e);
			}
		}
	}

	public static String appendSuffix(String resolvedUrl, String originalUrl) {
		if (resolvedUrl == null)
			return null;
		int query = originalUrl.indexOf('?');
		int hash = originalUrl.indexOf('#');
		int suffixIndex = query < 0 ? hash : hash < 0 ? query : Math.min(query, hash);
		String suffix = suffixIndex >= 0 ? originalUrl.substring(suffixIndex) : "";
		if (suffix.startsWith("?") && resolvedUrl.contains("?"))
			suffix = "&" + suffix.substring(1);
		return resolvedUrl + suffix;
	}

	public static void markMissing(Element element, String addUrl, String addIconHref, String title) {
		Element marker = new Element("span").addClass("missing").text("!!missing!!");
		element.after(marker);
		if (addUrl != null && addIconHref != null) {
			Element addLink = new Element("a").attr("href", addUrl).attr("title", title).addClass("add-missing");
			addLink.appendElement("svg").addClass("icon").appendElement("use").attr("xlink:href", addIconHref);
			marker.after(addLink);
		}
	}
}
