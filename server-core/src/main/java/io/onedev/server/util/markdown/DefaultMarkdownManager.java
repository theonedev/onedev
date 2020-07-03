package io.onedev.server.util.markdown;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Document;

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

import io.onedev.commons.utils.HtmlUtils;
import io.onedev.server.model.Project;

@Singleton
public class DefaultMarkdownManager implements MarkdownManager {
	
	private final Set<Extension> contributedExtensions;
	
	private final Set<MarkdownProcessor> htmlTransformers;
	
	@Inject
	public DefaultMarkdownManager(Set<Extension> contributedExtensions, Set<MarkdownProcessor> htmlTransformers) {
		this.contributedExtensions = contributedExtensions;
		this.htmlTransformers = htmlTransformers;
	}

	@Override
	public String render(String markdown) {
		List<Extension> extensions = new ArrayList<>();
		extensions.add(AnchorLinkExtension.create());
		extensions.add(TablesExtension.create());
		extensions.add(TaskListExtension.create());
		extensions.add(DefinitionExtension.create());
		extensions.add(TocExtension.create());
		extensions.add(AutolinkExtension.create());
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
				.set(Parser.EXTENSIONS, extensions);

		Parser parser = Parser.builder(options).build();

		HtmlRenderer htmlRenderer = HtmlRenderer.builder(options).build();
		
		Node document = parser.parse(markdown);
		return htmlRenderer.render(document);
	}

	@Override
	public String escape(String markdown) {
		markdown = StringEscapeUtils.escapeHtml4(markdown);
		markdown = StringUtils.replace(markdown, "\n", "<br>");
		return markdown;
	}

	@Override
	public Document process(Document document, @Nullable Project project, @Nullable Object context) {
		document = HtmlUtils.sanitize(document);
		for (MarkdownProcessor htmlTransformer: htmlTransformers)
			htmlTransformer.process(document, project, context);
		return document;
	}

	@Override
	public String process(String html, Project project, Object context) {
		return process(HtmlUtils.parse(html), project, context).body().html();
	}

}
