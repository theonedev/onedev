package io.onedev.server.markdown;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.misc.Extension;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.resource.AttachmentResource;

@Singleton
public class DefaultMarkdownManager implements MarkdownManager {
	
	private final SettingManager settingManager;
	
	private final Set<Extension> contributedExtensions;
	
	private final Set<MarkdownProcessor> htmlTransformers;
	
	@Inject
	public DefaultMarkdownManager(SettingManager settingManager, Set<Extension> contributedExtensions, 
			Set<MarkdownProcessor> htmlTransformers) {
		this.settingManager = settingManager;
		this.contributedExtensions = contributedExtensions;
		this.htmlTransformers = htmlTransformers;
	}

	private MutableDataHolder setupOptions() {
		List<Extension> extensions = new ArrayList<>();
		extensions.add(AnchorLinkExtension.create());
		extensions.add(TablesExtension.create());
		extensions.add(TaskListExtension.create());
		extensions.add(DefinitionExtension.create());
		extensions.add(TocExtension.create());
		extensions.add(AutolinkExtension.create());
		extensions.addAll(contributedExtensions);

		return new MutableDataSet()
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
	}
	
	@Override
	public String render(String markdown) {
		MutableDataHolder options = setupOptions();
		Node node = parse(markdown);
		return HtmlRenderer.builder(options).softBreak("<br>").build().render(node);
	}

	@Override
	public Document process(Document document, @Nullable Project project, @Nullable BlobRenderContext blobRenderContext, 
			@Nullable SuggestionSupport suggestionSupport, boolean forExternal) {
		document = HtmlUtils.sanitize(document);
		for (MarkdownProcessor htmlTransformer: htmlTransformers)
			htmlTransformer.process(document, project, blobRenderContext, suggestionSupport);
		
		if (forExternal) {
			for (Element element: document.body().getElementsByTag("img")) {
				String src = element.attr("src");
				if (src.startsWith("/")) {
					src = settingManager.getSystemSetting().getServerUrl() + src;
					element.attr("src", AttachmentResource.authorizeGroup(src));
				}
				String style = element.attr("style");
				if (!style.endsWith(";"))
					style += ";";
				style += "max-width:100%";
				element.attr("style", style);
			}
			for (Element element: document.body().getElementsByTag("a")) {
				String href = element.attr("href");
				if (href.startsWith("/")) {
					href = settingManager.getSystemSetting().getServerUrl() + href;
					element.attr("href", AttachmentResource.authorizeGroup(href));
				}
			}
		}
		
		return document;
	}
	
	@Override
	public String process(String html, Project project, 
			@Nullable BlobRenderContext blobRenderContext, 
			@Nullable SuggestionSupport suggestionSupport, 
			boolean forExternal) {
		return process(HtmlUtils.parse(html), project, blobRenderContext, suggestionSupport, forExternal).body().html();
	}

	@Override
	public Node parse(String markdown) {
		MutableDataHolder options = setupOptions();
		Parser parser = Parser.builder(options).build();
		return parser.parse(markdown);
	}

}
