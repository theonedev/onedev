package com.pmease.commons.util;

import java.util.Collection;
import java.util.regex.Matcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;

import com.google.common.collect.Lists;

public class JsoupUtils {

	private static final String[] SAFE_TAGS = new String[] {
		"h1", "h2", "h3", "h4", "h5", "h6", "h7", "h8", "br", "b", "i", 
		"strong", "em", "a", "pre", "code", "img", "tt", "div", "ins", "del",
		"sup", "sub", "p", "ol", "ul", "li", "table", "thead", "tbody", "tfoot",
		"th", "tr", "td", "rt", "rp", "blockquote", "dl", "dt", "dd", "kbd", "q",
		"hr", "strike", "caption", "cite", "col", "colgroup", "small", 
		"span", "u", "input"
	};

	private static final String[] SAFE_ATTRIBUTES = new String[] {
		"abbr", "accept", "accept-charset",
		"accesskey", "action", "align", "alt", "axis",
		"border", "cellpadding", "cellspacing", "char",
		"charoff", "charset", "checked", "cite",
		"clear", "cols", "colspan", "color",
		"compact", "coords", "datetime", "details", "dir",
		"disabled", "enctype", "for", "frame",
		"headers", "height", "hreflang",
		"hspace", "ismap", "label", "lang",
		"longdesc", "maxlength", "media", "method",
		"multiple", "name", "nohref", "noshade",
		"nowrap", "prompt", "readonly", "rel", "rev",
		"rows", "rowspan", "rules", "scope",
		"selected", "shape", "size", "span",
		"start", "style", "summary", "tabindex", "target",
		"title", "type", "usemap", "valign", "value",
		"vspace", "width", "itemprop", "class", 
		"data-mdstart", "data-mdend" 
	};
	
	private static final String[] SAFE_ANCHOR_SCHEMES = new String[] {
		"http", "https", "mailto",
	};
	
	public static final Whitelist WHITE_LIST = new Whitelist()
		// Tags ---
	    .addTags(SAFE_TAGS)
	    
	    // Attributes ---
	    .addAttributes("a", "href", "title")
	    .addAttributes("img", "align", "alt", "height", "src", "title", "width")
	    .addAttributes("div", "itemscope", "itemtype")
	    .addAttributes(":all", SAFE_ATTRIBUTES)
	
	    // Protocols ---
	    .addProtocols("a", "href", SAFE_ANCHOR_SCHEMES)
	    .addProtocols("blockquote", "cite", "http", "https")
	    .addProtocols("cite", "cite", "http", "https")
	    .addProtocols("img", "src", "http", "https")
	    .addProtocols("q", "cite", "http", "https")
	    
	    .preserveRelativeLinks(true)
	    ;

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

	public static String sanitize(String bodyHtml) {
		return Jsoup.clean(bodyHtml, WHITE_LIST);
	}
	
	public static void appendReplacement(Matcher matcher, Node node, String replacement) {
		StringBuffer buffer = new StringBuffer();
		matcher.appendReplacement(buffer, "");
		if (buffer.length() != 0)
			node.before(new TextNode(buffer.toString(), node.baseUri()));
		node.before(new DataNode(replacement, node.baseUri()));
	}
	
	public static void appendTail(Matcher matcher, Node node) {
		StringBuffer buffer = new StringBuffer();
		matcher.appendTail(buffer);
		if (buffer.length() != 0)
			node.before(new TextNode(buffer.toString(), node.baseUri()));
		node.remove();
	}
}
