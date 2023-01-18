package io.onedev.server.markdown;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.gitlab.GitLabExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.formatter.Formatter.Builder;
import com.vladsch.flexmark.formatter.Formatter.FormatterExtension;
import com.vladsch.flexmark.formatter.NodeFormatter;
import com.vladsch.flexmark.formatter.NodeFormatterFactory;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.resource.AttachmentResource;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
		extensions.add(GitLabExtension.create());
		extensions.addAll(contributedExtensions);

		return new MutableDataSet()
				.set(HtmlRenderer.GENERATE_HEADER_ID, true)
				.set(AnchorLinkExtension.ANCHORLINKS_SET_NAME, true)
				.set(AnchorLinkExtension.ANCHORLINKS_WRAP_TEXT, false)
				.set(AnchorLinkExtension.ANCHORLINKS_TEXT_PREFIX, "<span class='header-anchor'></span>")
				.set(Parser.SPACE_IN_LINK_URLS, true)
				.set(TablesExtension.COLUMN_SPANS, false)
				.set(TablesExtension.APPEND_MISSING_COLUMNS, true)
				.set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
				.set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
				.set(TablesExtension.CLASS_NAME, "table")
				.set(Parser.EXTENSIONS, extensions);
	}
	
	@Override
	public String render(String markdown) {
		Node node = parse(markdown);
		return HtmlRenderer.builder(setupOptions()).build().render(node);
	}

	@Override
	public Document process(Document document, @Nullable Project project, @Nullable BlobRenderContext blobRenderContext, 
			@Nullable SuggestionSupport suggestionSupport, boolean forExternal) {
		document = HtmlUtils.sanitize(document);
		for (MarkdownProcessor htmlTransformer: htmlTransformers)
			htmlTransformer.process(document, project, blobRenderContext, suggestionSupport, forExternal);
		
		if (forExternal) {
			for (Element element: document.body().getElementsByTag("img")) {
				element.attr("src", toExternalUrl(element.attr("src")));
				String style = element.attr("style");
				if (!style.endsWith(";"))
					style += ";";
				style += "max-width:100%";
				element.attr("style", style);
			}
			for (Element element: document.body().getElementsByTag("a")) 
				element.attr("href", toExternalUrl(element.attr("href")));
		}
		document.outputSettings().prettyPrint(false);
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

	@Override
	public String toExternalUrl(String url) {
		if (url.startsWith("/")) 
			return AttachmentResource.authorizeGroup(settingManager.getSystemSetting().getServerUrl() + url);
		else
			return url;
	}

	@Override
	public String format(String markdown, Set<NodeFormattingHandler<?>> handlers) {
		Parser parser = Parser.builder().build();
		Node node = parser.parse(markdown);

		Collection<FormatterExtension> extensions = new ArrayList<>();
		extensions.add(new FormatterExtension() {

			@Override
			public void rendererOptions(MutableDataHolder options) {
			}

			@Override
			public void extend(Builder formatterBuilder) {
				formatterBuilder.nodeFormatterFactory(new NodeFormatterFactory() {

					@Override
					public @NotNull NodeFormatter create(@NotNull DataHolder options) {
						return new NodeFormatter() {

							@Override
							public @Nullable Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
								return handlers;
							}

							@Override
							public @Nullable Set<Class<?>> getNodeClasses() {
								return null;
							}
							
						};
					}
					
				});
			}
			
		});		
		return Formatter.builder().extensions(extensions).build().render(node);	
	}
	
}
