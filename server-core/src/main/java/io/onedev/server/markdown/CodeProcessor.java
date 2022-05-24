package io.onedev.server.markdown;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import com.google.common.base.Splitter;

import io.onedev.server.model.Project;

public class CodeProcessor implements MarkdownProcessor {
	
	@Override
	public void process(Document rendered, @Nullable Project project, Object context) {
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
							&& element.parent().tagName().equals("pre")) {
						codeElements.add(element);
					}
				}
			}
			
		}, rendered);
		
		for (Element codeElement: codeElements) {
			String language = null;
			String cssClasses = codeElement.attr("class");
			for (String cssClass: Splitter.on(" ").trimResults().omitEmptyStrings().split(cssClasses)) {
				if (cssClass.startsWith("language-")) {
					language = cssClass.substring("language-".length());
					break;
				}
			}

			if (language != null)
				codeElement.attr("data-language", language);
			codeElement.addClass("cm-s-eclipse").parent().addClass("highlighted");
		}
		
	}
	
}
