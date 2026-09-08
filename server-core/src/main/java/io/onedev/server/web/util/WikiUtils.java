package io.onedev.server.web.util;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.PathUtils;
import io.onedev.server.annotation.Path;
import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.git.GitUtils;
import io.onedev.server.markdown.LinkUtils;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.validation.validator.PathValidator;

public class WikiUtils {

	private static final Pattern REFERENCE = Pattern.compile("\\[\\[([^\\]\\r\\n]+)\\]\\]");

	public static boolean isUnderFolder(String folder, String path) {
		return path != null && path.startsWith(folder + "/");
	}

	public static void validatePath(String path) {
		var error = PathValidator.checkPath(Path.Type.RELATIVE, path);
		if (error != null)
			throw new NotAcceptableException(error);
	}

	public static String pagePath(String folder, String page) {
		validatePath(folder);
		validatePath(page);
		return normalizePath(folder + "/" + page + ".md");
	}

	/** Use the same path for permission checks and Git writes. */
	public static String normalizePath(String path) {
		validatePath(path);
		String normalized = GitUtils.normalizePath(path);
		if (normalized == null)
			throw new NotAcceptableException("Invalid wiki path");
		return normalized;
	}

	public static String pageReference(String folder, String currentPath, String path, String text) {
		if (!path.startsWith(folder + "/") || !path.endsWith(".md"))
			throw new IllegalArgumentException("Not a wiki page");
		validatePath(path);
		int slash = currentPath.lastIndexOf('/');
		String directory = slash >= 0 ? currentPath.substring(0, slash) : "";
		String page = PathUtils.relativize(directory, path.substring(0, path.length() - 3)).replace('-', ' ');
		if ((text == null || text.isBlank()) && page.contains("/"))
			text = page.substring(page.lastIndexOf('/') + 1);
		return "[[" + page + (text != null && !text.isBlank() && !text.equals(page) ? "|" + text : "") + "]]";
	}

	/** Resolve references only in prose, never inside code, HTML attributes or existing links. */
	public static String linkPages(String html, Function<String, String> urlOf) {
		return linkPages(html, urlOf, destination -> false);
	}

	public static String linkPages(String html, Function<String, String> urlOf, Predicate<String> missing) {
		return linkPages(html, urlOf, missing, reference -> null, null);
	}

	public static String linkPages(String html, Function<String, String> urlOf, Predicate<String> missing,
			Function<String, String> addUrlOf, String addIconHref) {
		return linkPages(html, reference -> {
			String url = urlOf.apply(reference);
			if (url == null)
				return null;
			boolean isMissing = missing.test(reference);
			return new ResolvedLink(url, isMissing, isMissing ? addUrlOf.apply(reference) : null);
		}, addIconHref);
	}

	private record ResolvedLink(String url, boolean missing, @Nullable String addUrl) {
	}

	private static String linkPages(String html, Function<String, ResolvedLink> resolve, String addIconHref) {
		var document = HtmlUtils.parse(html);
		linkText(document.body(), resolve, addIconHref);
		document.outputSettings().prettyPrint(false);
		return document.body().html();
	}

	/** Resolve wiki names and anchors consistently in both wiki and repository views. */
	public static String resolvePageLinks(String html, Function<String, String> urlOf,
			Predicate<String> missing, Function<String, String> addUrlOf, String addIconHref) {
		return resolvePageLinks(html, Function.identity(), urlOf, missing, addUrlOf, addIconHref);
	}

	public static String resolvePagePath(String currentPath, String destination) {
		int slash = currentPath.lastIndexOf('/');
		String directory = slash >= 0 ? currentPath.substring(0, slash) : "";
		if (destination.startsWith("/"))
			throw new IllegalArgumentException("Absolute wiki page path");
		String path = GitUtils.normalizePath(PathUtils.resolve(directory, destination + ".md"));
		if (path == null)
			throw new IllegalArgumentException("Wiki link is outside repository");
		return path;
	}

	public static String resolvePageLinks(String html, Function<String, String> pathOf, Function<String, String> urlOf,
			Predicate<String> missing, Function<String, String> addUrlOf, String addIconHref) {
		return linkPages(html, reference -> {
			int hash = reference.indexOf('#');
			String destination = hash >= 0 ? reference.substring(0, hash) : reference;
			String anchor = hash >= 0 ? reference.substring(hash) : "";
			if (destination.isEmpty())
				return new ResolvedLink(anchor, false, null);
			destination = destination.replace(' ', '-');
			try {
				destination = pathOf.apply(destination);
				validatePath(destination);
				String url = urlOf.apply(destination);
				if (url == null)
					return null;
				boolean isMissing = missing.test(destination);
				return new ResolvedLink(url + anchor, isMissing, isMissing ? addUrlOf.apply(destination) : null);
			} catch (IllegalArgumentException | NotAcceptableException e) {
				return null;
			}
		}, addIconHref);
	}

	public static String resolveRelativeLinks(String html, String currentPath,
			Function<String, String> linkUrlOf, Function<String, String> imageUrlOf) {
		return resolveRelativeLinks(html, currentPath, linkUrlOf, imageUrlOf,
				referencedPath -> false, referencedPath -> null, null);
	}

	public static String resolveRelativeLinks(String html, String currentPath,
			Function<String, String> linkUrlOf, Function<String, String> imageUrlOf,
			Predicate<String> missing, Function<String, String> addUrlOf, String addIconHref) {
		var document = HtmlUtils.parse(html);
		int slash = currentPath.lastIndexOf('/');
		String directory = slash >= 0 ? currentPath.substring(0, slash) : null;
		LinkUtils.resolveRelativeLinks(document, directory,
				(path, url) -> LinkUtils.appendSuffix(linkUrlOf.apply(path), url),
				(path, url) -> LinkUtils.appendSuffix(imageUrlOf.apply(path), url),
				missing, addUrlOf, addIconHref);
		document.outputSettings().prettyPrint(false);
		return document.body().html();
	}

	private static void linkText(Node node, Function<String, ResolvedLink> resolve, String addIconHref) {
		if (node instanceof Element) {
			String tag = ((Element) node).tagName();
			if (tag.equals("code") || tag.equals("pre") || tag.equals("a") || tag.equals("script") || tag.equals("style"))
				return;
		}
		if (node instanceof TextNode) {
			String text = ((TextNode) node).getWholeText();
			var matcher = REFERENCE.matcher(text);
			int end = 0;
			while (matcher.find()) {
				String reference = matcher.group(1);
				int separator = reference.indexOf('|');
				String destination = separator >= 0 ? reference.substring(0, separator) : reference;
				String title = separator >= 0 ? reference.substring(separator + 1) : reference;
				ResolvedLink link = resolve.apply(destination.trim());
				if (link != null) {
					node.before(new TextNode(text.substring(end, matcher.start())));
					Element element = new Element("a").attr("href", link.url()).text(title);
					node.before(element);
					if (link.missing())
						LinkUtils.markMissing(element, link.addUrl(), addIconHref, "Add this page");
					end = matcher.end();
				}
			}
			if (end != 0) {
				node.before(new TextNode(text.substring(end)));
				node.remove();
			}
		} else {
			for (Node child : new ArrayList<>(node.childNodes()))
				linkText(child, resolve, addIconHref);
		}
	}
}
