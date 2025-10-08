package io.onedev.server.markdown;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import com.google.common.base.Splitter;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.diff.DiffBlock;
import io.onedev.server.util.diff.DiffRenderer;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.component.markdown.SuggestionSupport.Selection;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;

public class CodeProcessor implements HtmlProcessor {
	
	@Override
	public void process(Document document, @Nullable Project project,
						@Nullable BlobRenderContext blobRenderContext,
						@Nullable SuggestionSupport suggestionSupport,
						boolean forExternal) {
		Collection<Element> codeElements = new ArrayList<>();
		NodeTraversor.traverse(new NodeVisitor() {

			@Override
			public void head(Node node, int depth) {
			}

			@Override
			public void tail(Node node, int depth) {
				if (node instanceof Element) {
					Element element = (Element) node;
					if (element.tagName().equals("code")
							&& element.parent() != null 
							&& element.parent().tagName().equals("pre")
							&& (element.parent().parent() == null || !element.parent().parent().attr("class").equals("pre-outer"))) {
						codeElements.add(element);
					}
				}
			}
			
		}, document);

		for (Element codeElement: codeElements) {
			codeElement.parent().wrap("<div class='pre-outer'></div>");
			String language = null;
			String cssClasses = codeElement.attr("class");
			for (String cssClass: Splitter.on(" ").trimResults().omitEmptyStrings().split(cssClasses)) {
				if (cssClass.startsWith("language-")) {
					language = cssClass.substring("language-".length());
					break;
				}
			}

			if (language != null) {
				codeElement.attr("data-language", language);
				if (language.equals("suggestion")) {
					if (suggestionSupport != null) {
						String suggestionContent = codeElement.wholeText();
						if (suggestionContent.endsWith("\n"))
							suggestionContent = suggestionContent.substring(0, suggestionContent.length()-1);
						List<String> suggestion = StringUtils.splitToLines(suggestionContent);
						Selection selection = suggestionSupport.getSelection();
						List<String> content = selection.getContent();
						List<DiffBlock<String>> diffBlocks = DiffUtils.diff(content, suggestion);
						codeElement.html("<div class='pb-2 mb-2 head font-size-xs mx-n2 px-2'>" + _T("Suggested change") + "</div>" 
									+ new DiffRenderer(diffBlocks).renderDiffs());
						codeElement.attr("data-suggestion", suggestionContent);
						codeElement.attr("data-suggestionfile", suggestionSupport.getFileName());
						codeElement.parent().addClass("suggestion");
						if (suggestionSupport.isOutdated()) 
							codeElement.attr("data-suggestionoutdated", "true");
						if (suggestionSupport.getApplySupport() != null) {
							codeElement.attr("data-suggestionappliable", "true");
							if (suggestionSupport.getApplySupport().getBatchSupport() != null) {
								codeElement.attr("data-suggestionbatchappliable", "true");
								if (suggestionSupport.getApplySupport().getBatchSupport().getInBatch() != null) {
									if (suggestionSupport.getApplySupport().getBatchSupport().getInBatch().equals(suggestion))
										codeElement.attr("data-suggestionapplyinbatch", "true");
									else
										codeElement.attr("data-suggestionoutdated", "true");
								}
							}
						}
					} else {
						codeElement.prepend("<p><i>" + _T("Suggested change") + "</i></p>");
					}
				}
			}
			codeElement.parent().addClass("code");
		}
		
	}
	
}
