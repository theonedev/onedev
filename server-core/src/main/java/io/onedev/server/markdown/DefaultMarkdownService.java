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
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Project;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.resource.AttachmentResource;
import org.jsoup.nodes.Element;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Singleton
public class DefaultMarkdownService implements MarkdownService {
	
	private final SettingService settingService;
	
	private final Set<Extension> contributedExtensions;
	
	private final Set<HtmlProcessor> htmlTransformers;
	
	@Inject
	public DefaultMarkdownService(SettingService settingService, Set<Extension> contributedExtensions,
								  Set<HtmlProcessor> htmlTransformers) {
		this.settingService = settingService;
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
		extensions.add(CalloutExtension.create());
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
		var options = setupOptions();
		Parser parser = Parser.builder(options).build();
		return HtmlRenderer.builder(options).build().render(parser.parse(markdown));
	}
	
	@Override
	public String process(String html, Project project,
						  @Nullable BlobRenderContext blobRenderContext,
						  @Nullable SuggestionSupport suggestionSupport,
						  boolean forExternal) {
		var doc = HtmlUtils.parse(html);
		doc = HtmlUtils.sanitize(doc);
		for (var htmlTransformer: htmlTransformers)
			htmlTransformer.process(doc, project, blobRenderContext, suggestionSupport, forExternal);

		if (forExternal) {
			for (Element element: doc.body().getElementsByTag("img")) {
				element.attr("src", toExternal(element.attr("src")));
				String style = element.attr("style");
				if (!style.endsWith(";"))
					style += ";";
				style += "max-width:100%";
				element.attr("style", style);
			}
			for (Element element: doc.body().getElementsByTag("a"))
				element.attr("href", toExternal(element.attr("href")));
		}
		doc.outputSettings().prettyPrint(false);
		return doc.body().html();
	}

	@Override
	public String toExternal(String url) {
		if (url.startsWith("/")) 
			return AttachmentResource.authorizeGroup(settingService.getSystemSetting().getServerUrl() + url);
		else
			return url;
	}

	@Override
	public String format(String markdown, Set<NodeFormattingHandler<?>> handlers) {
		Collection<FormatterExtension> extensions = new ArrayList<>();
		extensions.add(new FormatterExtension() {

			@Override
			public void rendererOptions(MutableDataHolder options) {
			}

			@Override
			public void extend(Builder formatterBuilder) {
				formatterBuilder.nodeFormatterFactory(options -> new NodeFormatter() {

					@Override
					public @Nullable Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
						return handlers;
					}

					@Override
					public @Nullable Set<Class<?>> getNodeClasses() {
						return null;
					}
					
				});
			}
			
		});
		var parser = Parser.builder().build();
		return Formatter.builder().extensions(extensions).build().render(parser.parse(markdown));	
	}
	
}
