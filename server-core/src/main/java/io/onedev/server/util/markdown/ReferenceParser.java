package io.onedev.server.util.markdown;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;

import io.onedev.commons.utils.HtmlUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.TextNodeVisitor;
import io.onedev.server.util.validation.ProjectNameValidator;

public class ReferenceParser {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("code", "a");
	
	private final Pattern pattern;
	
	private final String referenceType;
	
	public ReferenceParser(Class<? extends AbstractEntity> referenceClass) {
		referenceType = referenceClass.getSimpleName();
		String[] words = StringUtils.split(WordUtils.uncamel(referenceType).toLowerCase(), " ");
		StringBuilder builder = new StringBuilder("(^|\\W+)(");
		for (int i=0; i<words.length-1; i++) 
			builder.append(words[i]).append("\\s*");
		builder.append(words[words.length-1]).append("\\s+)(");
		builder.append(ProjectNameValidator.PATTERN.pattern());
		builder.append(")?#(\\d+)(?=$|\\W+)");
		pattern = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
	}
	
	public Collection<ProjectScopedNumber> parseReferences(Project project, String rendered) {
		return parseReferences(Jsoup.parseBodyFragment(rendered), project);		
	}
	
	public Collection<ProjectScopedNumber> parseReferences(Document document, @Nullable Project project) {
		Collection<ProjectScopedNumber> references = new HashSet<>();
		
		TextNodeVisitor visitor = new TextNodeVisitor() {
			
			@Override
			protected boolean isApplicable(TextNode node) {
				if (HtmlUtils.hasAncestor(node, IGNORED_TAGS))
					return false;
				
				String text = StringUtils.deleteWhitespace(node.getWholeText()).toLowerCase();
				return text.contains(referenceType.toLowerCase()) && text.contains("#"); // fast scan here, do pattern match later
			}
		};
		
		NodeTraversor tranversor = new NodeTraversor(visitor);
		tranversor.traverse(document);
		
		for (TextNode node : visitor.getMatchedNodes()) {
			Matcher matcher = pattern.matcher(node.getWholeText());
			while (matcher.find()) {
				String referenceText = matcher.group(2);
				String referenceProjectName = matcher.group(3);
				Long referenceNumber = Long.valueOf(matcher.group(5));

				Project referenceProject;
				if (referenceProjectName != null) {
					referenceProject = OneDev.getInstance(ProjectManager.class).find(referenceProjectName);
					referenceText += referenceProjectName;
				} else {
					referenceProject = project;
				}
				referenceText += "#" + referenceNumber;
				
				String referenceTag;
				if (referenceProject != null) {
					ProjectScopedNumber referenceable = new ProjectScopedNumber(referenceProject, referenceNumber);
					references.add(referenceable);
					referenceTag = toHtml(referenceable, referenceText);
				} else {
					referenceTag = referenceText; 
				}
				HtmlUtils.appendReplacement(matcher, node, matcher.group(1) + referenceTag);
			}
			HtmlUtils.appendTail(matcher, node);
		}

		return references;
	}

	protected String toHtml(ProjectScopedNumber referenceable, String referenceText) {
		return referenceText;
	}
	
}
