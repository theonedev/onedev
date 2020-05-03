package io.onedev.server.util.markdown;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.ObjectId;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Splitter;

import io.onedev.commons.jsyntax.TokenUtils;
import io.onedev.commons.jsyntax.Tokenized;
import io.onedev.commons.jsyntax.Tokenizer;
import io.onedev.commons.jsyntax.TokenizerRegistry;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.web.page.project.commits.CommitDetailPage;

public class CodeProcessor implements MarkdownProcessor {
	
	protected String toHtml(Project project, ObjectId commitId) {
		CharSequence url = RequestCycle.get().urlFor(
				CommitDetailPage.class, CommitDetailPage.paramsOf(project, commitId.name())); 
		return String.format("<a href='%s' class='commit reference' data-reference='%s'>%s</a>", url, commitId.name(), 
				GitUtils.abbreviateSHA(commitId.name()));
	}

	@Override
	public void process(@Nullable Project project, Document rendered, Object context) {
		Collection<Element> codeElements = new ArrayList<>();
		new NodeTraversor(new NodeVisitor() {

			@Override
			public void head(Node node, int depth) {
			}

			@Override
			public void tail(Node node, int depth) {
				if (node instanceof Element) {
					Element element = (Element) node;
					if (element.tagName().equals("code") 
							&& element.parent() != null 
							&& element.parent().tagName().equals("pre")) {
						codeElements.add(element);
					}
				}
			}
			
		}).traverse(rendered);
		
		for (Element codeElement: codeElements) {
			String code = HtmlEscape.unescapeHtml(codeElement.html());
			
			String language = null;
			String cssClasses = codeElement.attr("class");
			for (String cssClass: Splitter.on(" ").trimResults().omitEmptyStrings().split(cssClasses)) {
				if (cssClass.startsWith("language-")) {
					language = cssClass.substring("language-".length());
					break;
				}
			}
			
			Tokenizer tokenizer = null;
			if (language != null) 
				tokenizer = TokenizerRegistry.getTokenizerByMode(language);
			
			if (tokenizer != null) {
				List<String> lines = Splitter.on("\n").splitToList(code);
				StringBuilder highlighted = new StringBuilder();
				for (Tokenized tokenized: tokenizer.tokenize(lines)) {
					for (long token: tokenized.getTokens())
						highlighted.append(TokenUtils.toHtml(tokenized.getText(), token, null, null));
					highlighted.append("\n");
				}			
				codeElement.html(highlighted.toString());
			}
			codeElement.addClass("cm-s-eclipse").parent().addClass("highlight");
		}
		
	}
	
}
