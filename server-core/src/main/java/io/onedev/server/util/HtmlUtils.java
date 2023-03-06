package io.onedev.server.util;

import java.util.Collection;
import java.util.regex.Matcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

import com.google.common.collect.Lists;

public class HtmlUtils {

	private static final String[] SAFE_TAGS = new String[] { "h1", "h2", "h3", "h4", "h5", "h6", "h7", "h8", "br", "b",
			"i", "strong", "em", "a", "pre", "code", "img", "tt", "div", "ins", "del", "sup", "sub", "p", "ol", "ul",
			"li", "table", "thead", "tbody", "tfoot", "th", "tr", "td", "rt", "rp", "blockquote", "dl", "dt", "dd",
			"kbd", "q", "hr", "strike", "caption", "cite", "col", "colgroup", "small", "span", "u", "input", "video", "source"};

	private static final String[] SAFE_ATTRIBUTES = new String[] { "abbr", "accept", "accept-charset", "accesskey",
			"action", "align", "alt", "axis", "border", "cellpadding", "cellspacing", "char", "charoff", "charset",
			"checked", "cite", "clear", "cols", "colspan", "color", "compact", "coords", "datetime", "details", "dir",
			"disabled", "enctype", "for", "frame", "headers", "height", "hreflang", "hspace", "ismap", "label", "lang",
			"longdesc", "maxlength", "media", "method", "multiple", "name", "nohref", "noshade", "nowrap", "prompt",
			"readonly", "rel", "rev", "rows", "rowspan", "rules", "scope", "selected", "shape", "size", "span", "start",
			"style", "summary", "tabindex", "target", "title", "type", "usemap", "valign", "value", "vspace", "width",
			"itemprop", "class", "controls", "id"};

	private static final String[] SAFE_ANCHOR_SCHEMES = new String[] { "http", "https", "mailto", };

	private static final Safelist whiteList;
	
	static {
		whiteList = new Safelist() {

			@Override
			protected boolean isSafeAttribute(String tagName, Element el, Attribute attr) {
				if (attr.getKey().startsWith("data-"))
					return true;
				else
					return super.isSafeAttribute(tagName, el, attr);
			}

		};

		whiteList.addTags(SAFE_TAGS)
				.addAttributes("a", "href", "title")
				.addAttributes("img", "align", "alt", "height", "src", "title", "width")
				.addAttributes("div", "itemscope", "itemtype")
				.addAttributes("source", "src")
				.addAttributes(":all", SAFE_ATTRIBUTES)
				.addProtocols("a", "href", SAFE_ANCHOR_SCHEMES)
				.addProtocols("blockquote", "cite", "http", "https")
				.addProtocols("cite", "cite", "http", "https")
				.addProtocols("img", "src", "http", "https")
				.addProtocols("q", "cite", "http", "https")
				.preserveRelativeLinks(true);
	}

	public static boolean hasAncestor(Node node, Collection<String> tags) {
		Node parent = node.parentNode();
		while (parent != null) {
			if (parent instanceof Element) {
				Element e = (Element) parent;
				if (tags.contains(e.tagName().toLowerCase())) {
					return true;
				}
			}
			
			parent = parent.parentNode();
		}
		
		return false;
	}

	public static boolean hasAncestor(Node node, String tag) {
		return hasAncestor(node, Lists.newArrayList(tag));
	}

	public static void appendReplacement(Matcher matcher, Node node, String replacement) {
		StringBuffer buffer = new StringBuffer();
		matcher.appendReplacement(buffer, "");
		if (buffer.length() != 0)
			node.before(new TextNode(buffer.toString()));
		node.before(replacement);
	}
	
	public static void appendTail(Matcher matcher, Node node) {
		StringBuffer buffer = new StringBuffer();
		matcher.appendTail(buffer);
		if (buffer.length() != 0)
			node.before(new TextNode(buffer.toString()));
		node.remove();
	}
	
	public static Document sanitize(Document doc) {
		return new Cleaner(whiteList).clean(doc);
	}
	
	public static Document parse(String html) {
		// Use a faked baseURI, otherwise all relative urls will be stripped out
		return Jsoup.parseBodyFragment(html, "http://localhost/sanitize");
	}
	
	public static String sanitize(String html) {
		return sanitize(parse(html)).body().html();
	}
	
}
