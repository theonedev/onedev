package com.gitplex.server.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

import com.gitplex.server.manager.MarkdownManager;
import com.gitplex.server.util.markdown.HtmlTransformer;
import com.gitplex.server.util.markdown.UrlResolveExtension;
import com.google.common.base.Preconditions;
import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;

@Singleton
public class DefaultMarkdownManager implements MarkdownManager {
	
	private static final String[] SAFE_TAGS = new String[] { "h1", "h2", "h3", "h4", "h5", "h6", "h7", "h8", "br", "b",
			"i", "strong", "em", "a", "pre", "code", "img", "tt", "div", "ins", "del", "sup", "sub", "p", "ol", "ul",
			"li", "table", "thead", "tbody", "tfoot", "th", "tr", "td", "rt", "rp", "blockquote", "dl", "dt", "dd",
			"kbd", "q", "hr", "strike", "caption", "cite", "col", "colgroup", "small", "span", "u", "input" };

	private static final String[] SAFE_ATTRIBUTES = new String[] { "abbr", "accept", "accept-charset", "accesskey",
			"action", "align", "alt", "axis", "border", "cellpadding", "cellspacing", "char", "charoff", "charset",
			"checked", "cite", "clear", "cols", "colspan", "color", "compact", "coords", "datetime", "details", "dir",
			"disabled", "enctype", "for", "frame", "headers", "height", "hreflang", "hspace", "ismap", "label", "lang",
			"longdesc", "maxlength", "media", "method", "multiple", "name", "nohref", "noshade", "nowrap", "prompt",
			"readonly", "rel", "rev", "rows", "rowspan", "rules", "scope", "selected", "shape", "size", "span", "start",
			"style", "summary", "tabindex", "target", "title", "type", "usemap", "valign", "value", "vspace", "width",
			"itemprop", "class" };

	private static final String[] SAFE_ANCHOR_SCHEMES = new String[] { "http", "https", "mailto", };

	private final Whitelist whiteList;

	private final Set<Extension> contributedExtensions;
	
	private final Set<HtmlTransformer> htmlTransformers;
	
	@Inject
	public DefaultMarkdownManager(Set<Extension> contributedExtensions, Set<HtmlTransformer> htmlTransformers) {
		this.contributedExtensions = contributedExtensions;
		this.htmlTransformers = htmlTransformers;

		whiteList = new Whitelist() {

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
				.addAttributes(":all", SAFE_ATTRIBUTES)
				.addProtocols("a", "href", SAFE_ANCHOR_SCHEMES)
				.addProtocols("blockquote", "cite", "http", "https")
				.addProtocols("cite", "cite", "http", "https")
				.addProtocols("img", "src", "http", "https")
				.addProtocols("q", "cite", "http", "https")
				.preserveRelativeLinks(true);
	}

	@Override
	public String render(String markdown, @Nullable String baseUrl, boolean postProcess) {
		Preconditions.checkArgument(baseUrl == null || baseUrl.startsWith("/"));
		
		List<Extension> extensions = new ArrayList<>();
		extensions.add(AnchorLinkExtension.create());
		extensions.add(TablesExtension.create());
		extensions.add(TaskListExtension.create());
		extensions.add(DefinitionExtension.create());
		extensions.add(TocExtension.create());
		extensions.add(AutolinkExtension.create());
		extensions.add(new UrlResolveExtension());
		extensions.addAll(contributedExtensions);

		MutableDataHolder options = new MutableDataSet()
				.set(HtmlRenderer.GENERATE_HEADER_ID, true)
				.set(AnchorLinkExtension.ANCHORLINKS_SET_NAME, true)
				.set(AnchorLinkExtension.ANCHORLINKS_WRAP_TEXT, false)
				.set(AnchorLinkExtension.ANCHORLINKS_TEXT_PREFIX, "<span class='header-anchor'></span>")
				.set(Parser.SPACE_IN_LINK_URLS, true)
				.setFrom(ParserEmulationProfile.GITHUB_DOC)
				.set(TablesExtension.COLUMN_SPANS, false)
				.set(TablesExtension.APPEND_MISSING_COLUMNS, true)
				.set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
				.set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
				.set(UrlResolveExtension.BASE_URL, baseUrl)
				.set(Parser.EXTENSIONS, extensions);

		Parser parser = Parser.builder(options).build();

		HtmlRenderer htmlRenderer = HtmlRenderer.builder(options).build();
		
		Node document = parser.parse(markdown);
		String html = htmlRenderer.render(document);

		if (postProcess)
			html = postProcess(html);
		
		return html;
	}

	@Override
	public String postProcess(String html) {
		// Use a faked baseURI, otherwise all relative urls will be stripped out
		Document body = Jsoup.parseBodyFragment(html, "http://localhost/sanitize");
		
		Cleaner cleaner = new Cleaner(whiteList);
		body = cleaner.clean(body);

		for (HtmlTransformer transformer : htmlTransformers)
			transformer.transform(body);
		return body.body().html();
	}

	@Override
	public String escape(String markdown) {
		markdown = StringEscapeUtils.escapeHtml4(markdown);
		markdown = StringUtils.replace(markdown, "\n", "<br>");
		return markdown;
	}

}
