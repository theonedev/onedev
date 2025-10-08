package io.onedev.server.entityreference;

import static java.lang.String.format;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Project;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.util.TextNodeVisitor;
import io.onedev.server.validation.validator.ProjectKeyValidator;
import io.onedev.server.validation.validator.ProjectPathValidator;

public class ReferenceUtils {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("code", "a");

	private static final Pattern PATTERN = compile(format("(?<prefix>^|\\W+)(?<type>(issue|build|pr|(pull\\s+request))\\s+)?(?<reference>(((?<projectPath>%s)?#)|((?<projectKey>%s)-))(?<number>\\d+))(?=$|[\\W]+)", ProjectPathValidator.PATTERN.pattern(), ProjectKeyValidator.PATTERN.pattern()), CASE_INSENSITIVE);
	
	public static List<EntityReference> extractReferences(String text, @Nullable Project currentProject) {
		Set<EntityReference> references = new LinkedHashSet<>();
		transformReferences(text, currentProject, (reference, aText) -> {
			if (reference != null)
				references.add(reference);
			return aText;
		});
		return new ArrayList<>(references);
	}

	public static String transformReferences(String text, @Nullable Project currentProject,
									  BiFunction<EntityReference, String, String> transformer) {
		if (mayContainReferences(text)) {
			var projectService = OneDev.getInstance(ProjectService.class);
			var builder = new StringBuilder();
			var index = 0;
			var matcher = PATTERN.matcher(text);
			while (matcher.find()) {
				Project project;
				var projectKey = matcher.group("projectKey");
				if (projectKey != null) {
					project = projectService.findByKey(projectKey);
				} else {
					var projectPath = matcher.group("projectPath");
					if (projectPath != null)
						project = projectService.findByPath(projectPath);
					else
						project = currentProject;
				}
				if (project != null) {
					var type = matcher.group("type");
					var number = Long.valueOf(matcher.group("number"));
					var reference = EntityReference.of(StringUtils.trimToEmpty(type), project, number);
					builder.append(transformer.apply(null, text.substring(index, matcher.start()) + matcher.group("prefix")));
					builder.append(transformer.apply(reference, (type!=null? type: "") + matcher.group("reference")));
					index = matcher.end();
				}
			}
			builder.append(transformer.apply(null, text.substring(index)));
			text = builder.toString();
		} else {
			text = transformer.apply(null, text);
		}
		var issueSetting = OneDev.getInstance(SettingService.class).getIssueSetting();
		for (var entry: issueSetting.getExternalIssueTransformers().getEntries()) {
			text = text.replaceAll(entry.getPattern(), entry.getReplaceWith());
		}
		return text;
	}

	public static void transformReferences(Document document, @Nullable Project currentProject,
									BiFunction<EntityReference, String, String> transformer) {
		var visitor = new TextNodeVisitor();
		NodeTraversor.traverse(visitor, document);

		for (var node : visitor.getMatchedNodes()) {
			if (!HtmlUtils.hasAncestor(node, IGNORED_TAGS)) {
				var text = transformReferences(node.getWholeText(), currentProject, transformer);
				if (text.length() != 0)
					node.before(text);
				node.remove();
			}
		}
	}

	public static List<EntityReference> extractReferences(Document document, @Nullable Project currentProject) {
		Set<EntityReference> references = new LinkedHashSet<>();
		transformReferences(document, currentProject, (reference, text) -> {
			if (reference != null)
				references.add(reference);
			return text;
		});
		return new ArrayList<>(references);
	}

	public static boolean mayContainReferences(String text) {
		var chars = text.toCharArray();
		for (var i=0; i<chars.length; i++) {
			if ((chars[i] == '#' || chars[i] == '-') && i < chars.length-1 && Character.isDigit(chars[i+1]))
				return true;
		}
		return false;
	}
	
}
