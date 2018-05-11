package io.onedev.server.util.markdown;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.Referenceable;
import io.onedev.server.util.JsoupUtils;
import io.onedev.server.util.TextNodeVisitor;
import io.onedev.utils.ReflectionUtils;
import io.onedev.utils.StringUtils;
import io.onedev.utils.WordUtils;

public abstract class ReferenceParser<T extends Referenceable> {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");
	
	private final Pattern pattern;
	
	private final String fastScan;
	
	public ReferenceParser() {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(ReferenceParser.class, getClass());
		if (typeArguments.size() == 1 && AbstractEntity.class.isAssignableFrom(typeArguments.get(0))) {
			Class<?> referenceClass = typeArguments.get(0);
			fastScan = referenceClass.getSimpleName().toLowerCase().toLowerCase()+"#";
			String[] words = StringUtils.split(WordUtils.uncamel(referenceClass.getSimpleName()).toLowerCase(), " ");
			StringBuilder builder = new StringBuilder("(^|\\s+)(");
			for (int i=0; i<words.length-1; i++) 
				builder.append(words[i]).append("\\s*");
			builder.append(words[words.length-1]).append("\\s+)#(\\d+)(?=($|\\s+))");
			pattern = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
		} else {
			throw new RuntimeException("Sub class of ReferenceParser must realize the type argument <T>");
		}
	}
	
	public Collection<T> parseReferences(Project project, String rendered) {
		return parseReferences(project, Jsoup.parseBodyFragment(rendered));		
	}
	
	protected abstract T findReferenceable(Project project, long number);
	
	public Collection<T> parseReferences(Project project, Document document) {
		Collection<T> references = new HashSet<>();
		
		TextNodeVisitor visitor = new TextNodeVisitor() {
			
			@Override
			protected boolean isApplicable(TextNode node) {
				if (JsoupUtils.hasAncestor(node, IGNORED_TAGS))
					return false;
				
				return StringUtils.deleteWhitespace(node.getWholeText()).trim().contains(fastScan); // fast scan here, do pattern match later
			}
		};
		
		NodeTraversor tranversor = new NodeTraversor(visitor);
		tranversor.traverse(document);
		
		for (TextNode node : visitor.getMatchedNodes()) {
			Matcher matcher = pattern.matcher(node.getWholeText());
			while (matcher.find()) {
				Long referenceNumber = Long.valueOf(matcher.group(3));
				String referenceTag;
				T referenceable = findReferenceable(project, referenceNumber);
				if (referenceable != null) {
					references.add(referenceable);
					referenceTag = toHtml(referenceable);
				} else {
					referenceTag = "#" + referenceNumber;
				}
				JsoupUtils.appendReplacement(matcher, node, matcher.group(1) + matcher.group(2) + referenceTag + matcher.group(4));
			}
			JsoupUtils.appendTail(matcher, node);
		}

		return references;
	}

	protected String toHtml(T referenceable) {
		return "#" + referenceable.getNumber();
	}
	
}
