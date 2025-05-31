package io.onedev.server.commandhandler;

import static org.apache.commons.text.StringEscapeUtils.unescapeJava;
import static org.unbescape.java.JavaEscape.escapeJava;
import static org.unbescape.java.JavaEscapeLevel.LEVEL_1_BASIC_ESCAPE_SET;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.Editable;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.translation.Translation;
import io.onedev.server.web.util.TextUtils;

@Singleton
public class ExtractTranslationKeys extends CommandHandler {

	private static final Logger logger = LoggerFactory.getLogger(ExtractTranslationKeys.class);

	public static final String COMMAND = "extract-translation-keys";

	private static final Pattern METHOD_PATTERN = Pattern
			.compile("_T\\s*\\(\\s*\"((?:[^\"\\\\]|\\\\.)*)\"(?:\\s*\\+\\s*\"((?:[^\"\\\\]|\\\\.)*)\")*\\s*\\)");

	private static final Pattern TAG_PATTERN = Pattern.compile("<wicket:t>(.+?)</wicket:t>",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("\\Wt:[a-zA-Z]+\\s*=\\s*(\"[^\"]*?\"|'[^']*?')",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");

	public static final Pattern EXTRACTED_KEYS_BLOCK_PATTERN = Pattern.compile(
			"(\n\\s*//\\s*extracted\\s*keys\\s*\n)(.*)(\n\\s*//\\s*manually\\s*added\\s*keys)",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	private static final Pattern TRANSLATION_PATTERN = Pattern.compile(
			"m\\s*\\.\\s*put\\s*\\(\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*,\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*\\)\\s*;");

	private static final String TRANSLATE_PLACEHOLDER = "**** translate this ****";

	@Inject
	public ExtractTranslationKeys(HibernateConfig hibernateConfig) {
		super(hibernateConfig);
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();

		if (Bootstrap.command.getArgs().length == 0) {
			logger.error("Missing OneDev project directory. Usage: {} <path to onedev project directory>",
					Bootstrap.command.getScript());
			System.exit(1);
		}

		var projectDir = new File(Bootstrap.command.getArgs()[0]);
		if (!projectDir.exists()) {
			logger.error("Unable to find directory: {}", projectDir.getAbsolutePath());
			System.exit(1);
		}

		if (!new File(projectDir, "server-core").exists()) {
			logger.error("Unable to find server-core under project dir: {}", projectDir.getAbsolutePath());
			System.exit(1);
		}

		logger.info("Extracting localization keys...");

		try {
			Set<String> extractedTranslationKeys = new TreeSet<>();

			Files.walk(projectDir.toPath())
					.filter(it -> it.toString().endsWith(".class"))
					.forEach(it -> {
						var classPath = it;
						String relative = null;
						var parentPath = classPath.getParent();
						while (!parentPath.equals(projectDir.toPath())) {
							if (parentPath.getFileName().toString().equals("classes")) {
								relative = parentPath.relativize(classPath).toString();
								break;
							}
							parentPath = parentPath.getParent();
						}

						if (relative != null && relative.startsWith("io/onedev/server/")) {
							var className = StringUtils.substringBeforeLast(relative, ".").replace("/", ".");
							if (!className.endsWith("Panel") && !className.endsWith("Page")
									&& !className.endsWith("Behavior")) {
								try {
									var clazz = Class.forName(className);
									var editable = clazz.getAnnotation(Editable.class);
									if (editable != null) {
										extractedTranslationKeys.add(EditableUtils.getDisplayName(clazz));
										var description = editable.description();
										if (description.length() != 0) {
											extractedTranslationKeys.add(description);
										}
									}
									for (var method : clazz.getDeclaredMethods()) {
										var annotation = method.getAnnotation(Editable.class);
										if (annotation != null) {
											extractedTranslationKeys.add(EditableUtils.getDisplayName(method));
											var group = EditableUtils.getGroup(method);
											if (group != null)
												extractedTranslationKeys.add(group);
											var description = annotation.description();
											if (description.length() != 0) {
												extractedTranslationKeys.add(description);
											}
											var placeholder = annotation.placeholder();
											if (placeholder.length() != 0) {
												extractedTranslationKeys.add(placeholder);
											}
											var rootPlaceholder = annotation.rootPlaceholder();
											if (rootPlaceholder.length() != 0) {
												extractedTranslationKeys.add(rootPlaceholder);
											}
										}
										var notEmpty = method.getAnnotation(NotEmpty.class);
										if (notEmpty != null && notEmpty.message().length() != 0) {
											extractedTranslationKeys.add(notEmpty.message());
										}
										var notNull = method.getAnnotation(NotNull.class);
										if (notNull != null && notNull.message().length() != 0) {
											extractedTranslationKeys.add(notNull.message());
										}
										var size = method.getAnnotation(Size.class);
										if (size != null && size.message().length() != 0) {
											extractedTranslationKeys.add(size.message());
										}
									}
									if (clazz.isEnum()) {
										for (var constant : clazz.getEnumConstants()) {
											extractedTranslationKeys.add(TextUtils.getDisplayValue((Enum<?>) constant));
										}
									}
								} catch (ClassNotFoundException e) {
									throw new RuntimeException(e);
								}
							}
						}
					});

			Files.walk(projectDir.toPath())
					.filter(it -> it.toString().contains("src/main/java/io/onedev/server"))
					.filter(it -> it.toString().endsWith(".java"))
					.forEach(it -> {
						try {
							extractedTranslationKeys.addAll(extractFromMethods(Files.readString(it)));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});

			Files.walk(projectDir.toPath())
					.filter(it -> it.toString().contains("src/main/java/io/onedev/server"))
					.filter(it -> it.toString().endsWith(".html"))
					.forEach(it -> {
						try {
							var content = Files.readString(it);
							extractedTranslationKeys.addAll(extractFromTags(content));
							extractedTranslationKeys.addAll(extractFromAttributes(content));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});

			for (var file : new File(projectDir,
					"server-core/src/main/java/" + Translation.class.getPackageName().replace(".", "/")).listFiles()) {
				if (file.getName().startsWith(Translation.class.getSimpleName() + "_")) {
					var content = updateTranslationKeys(Files.readString(file.toPath()), extractedTranslationKeys);
					Files.writeString(file.toPath(), content);
				}
			}
		} catch (Throwable e) {
			logger.error("Error extracting translation keys", e);
			System.exit(1);
		}

		System.exit(0);
	}

	public static Collection<String> extractFromMethods(String content) {
		var extracted = new ArrayList<String>();
		Matcher matcher = METHOD_PATTERN.matcher(content);
		while (matcher.find()) {
			StringBuilder key = new StringBuilder();
			String match = matcher.group(0);
			Matcher literalMatcher = STRING_LITERAL_PATTERN.matcher(match);
			while (literalMatcher.find()) {
				key.append(unescapeJava(literalMatcher.group(1)));
			}
			if (key.length() != 0)
				extracted.add(key.toString());
		}
		return extracted;
	}

	public static Collection<String> extractFromTags(String content) {
		var extracted = new ArrayList<String>();
		Matcher matcher = TAG_PATTERN.matcher(content);
		while (matcher.find()) {
			var key = matcher.group(1).trim().replaceAll("\\s+", " ");
			if (key.length() != 0)
				extracted.add(key);
		}
		return extracted;
	}

	public static Collection<String> extractFromAttributes(String content) {
		var extracted = new ArrayList<String>();
		Matcher matcher = ATTRIBUTE_PATTERN.matcher(content);
		while (matcher.find()) {
			var key = matcher.group(1);
			key = HtmlEscape.unescapeHtml(key.substring(1, key.length() - 1).trim());
			if (key.length() != 0)
				extracted.add(key);
		}
		return extracted;
	}

	public static String updateTranslationKeys(String content, Collection<String> extractedTranslationKeys) {
		extractedTranslationKeys = new TreeSet<>(extractedTranslationKeys);
		var matcher = EXTRACTED_KEYS_BLOCK_PATTERN.matcher(content);
		if (!matcher.find())
			throw new RuntimeException("Unable to find extracted keys block");
		var newContent = new StringBuffer();
		var translations = new StringBuffer();
		var existingTranslations = new TreeMap<String, String>();
		String extractedKeysBlock = matcher.group(2);
		var translationMatcher = TRANSLATION_PATTERN.matcher(extractedKeysBlock);
		while (translationMatcher.find()) {
			var key = unescapeJava(translationMatcher.group(1));
			var value = unescapeJava(translationMatcher.group(2));
			if (!value.equals(TRANSLATE_PLACEHOLDER))
				existingTranslations.put(key, value);
		}
		existingTranslations.keySet().retainAll(extractedTranslationKeys);
		for (var entry : existingTranslations.entrySet()) {
			addToTranslations(translations, entry.getKey(), entry.getValue());
		}
		extractedTranslationKeys.removeAll(existingTranslations.keySet());
		for (var key : extractedTranslationKeys) {
			addToTranslations(translations, key, null);
		}
		matcher.appendReplacement(newContent,
				matcher.group(1) + Matcher.quoteReplacement(translations.toString()) + matcher.group(3));
		matcher.appendTail(newContent);
		return newContent.toString();
	}

	private static void addToTranslations(StringBuffer translations, String key, @Nullable String value) {
		key = escapeJava(key, LEVEL_1_BASIC_ESCAPE_SET);
		if (value != null)
			value = escapeJava(value, LEVEL_1_BASIC_ESCAPE_SET);
		else
			value = "**** translate this ****";
		translations.append("\t\tm.put(\"" + key + "\", ");
		if (key.length() > 80)
			translations.append("\n\t\t\t");
		translations.append("\"" + value + "\");\n");
	}

	@Override
	public void stop() {
	}

}
