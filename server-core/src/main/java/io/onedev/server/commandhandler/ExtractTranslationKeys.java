package io.onedev.server.commandhandler;

import static org.apache.commons.text.StringEscapeUtils.unescapeJava;
import static org.unbescape.java.JavaEscape.escapeJava;
import static org.unbescape.java.JavaEscapeLevel.LEVEL_1_BASIC_ESCAPE_SET;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.Editable;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Pair;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.translation.Translation;
import io.onedev.server.web.util.TextUtils;

@Singleton
public class ExtractTranslationKeys extends CommandHandler {

	private static final Logger logger = LoggerFactory.getLogger(ExtractTranslationKeys.class);

	public static final String COMMAND = "extract-translation-keys";

	private static final Pattern _T_PATTERN = Pattern.compile("_T\\(\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*\\)");

	private static final Pattern TRANSLATION_PATTERN = Pattern.compile("m\\s*\\.\\s*put\\s*\\(\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*,\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*\\)\\s*;");

	private static final String TRANSLATE_PLACEHOLDER = "**** translate this ****";
	
	@Inject
	public ExtractTranslationKeys(HibernateConfig hibernateConfig) {
		super(hibernateConfig);
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();
		
		if (Bootstrap.command.getArgs().length == 0) {
			logger.error("Missing OneDev project directory. Usage: {} <path to onedev project directory>", Bootstrap.command.getScript());
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
			Set<String> localizationKeys = new TreeSet<>();

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
						if (!className.endsWith("Panel") && !className.endsWith("Page") && !className.endsWith("Behavior")) {
							try {
								var clazz = Class.forName(className);
								var editable = clazz.getAnnotation(Editable.class);
								if (editable != null) {
									localizationKeys.add(EditableUtils.getDisplayName(clazz));
									var description = editable.description();
									if (description.length() != 0) {
										localizationKeys.add(description);
									}
								}
								for (var method: clazz.getDeclaredMethods()) {
									var annotation = method.getAnnotation(Editable.class);
									if (annotation != null) {
										localizationKeys.add(EditableUtils.getDisplayName(method));
										var description = annotation.description();
										if (description.length() != 0) {
											localizationKeys.add(description);
										}
									}
								}
								if (clazz.isEnum()) {
									for (var constant: clazz.getEnumConstants()) {
										localizationKeys.add(TextUtils.getDisplayValue((Enum<?>) constant));
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
						localizationKeys.addAll(extract_T(Files.readString(it)));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});

			Files.walk(projectDir.toPath())
				.filter(it -> it.toString().contains("src/main/java/io/onedev/server"))
				.filter(it -> it.toString().endsWith(".html"))
				.forEach(it -> {
					try {
						localizationKeys.addAll(extract_wicket_t(Files.readString(it)));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});

			for (var file: new File(projectDir, "server-core/src/main/java/" + Translation.class.getPackageName().replace(".", "/")).listFiles()) {
				if (file.getName().startsWith(Translation.class.getSimpleName() + "_")) {
					var newLines = new ArrayList<String>();
					var lines = Files.readAllLines(file.toPath());
					var translations = new TreeMap<String, String>();
					var inAutoContentsBlock = false;
					for (var line: lines) {
						if (inAutoContentsBlock) {
							if (line.trim().equals("// Manual contents")) {
								inAutoContentsBlock = false;
								translations.keySet().retainAll(localizationKeys);
								for (var entry: translations.entrySet()) {
									var key = entry.getKey();
									var value = entry.getValue();
									newLines.add("		m.put(\"" + escapeJava(key, LEVEL_1_BASIC_ESCAPE_SET) + "\", \"" + escapeJava(value, LEVEL_1_BASIC_ESCAPE_SET) + "\");");
								}
								localizationKeys.removeAll(translations.keySet());
								for (var key: localizationKeys) {
									newLines.add("		m.put(\"" + escapeJava(key, LEVEL_1_BASIC_ESCAPE_SET) + "\", \"**** translate this ****\");");
								}
								newLines.add("");
								newLines.add(line);
							} else if (line.trim().length() != 0) {
								var translation = parseTranslation(line);
								if (!translation.getRight().equals(TRANSLATE_PLACEHOLDER)) {
									translations.put(translation.getLeft(), translation.getRight());
								}
							}
						} else if (line.trim().equals("// Auto contents")) {
							inAutoContentsBlock = true;
							newLines.add(line);
						} else {
							newLines.add(line);
						}
					}
					Files.write(file.toPath(), newLines, StandardCharsets.UTF_8);
				}
			}
		} catch (Throwable e) {
			logger.error("Error extracting translation keys", e);
			System.exit(1);
		}

		System.exit(0);
	}

	public static Collection<String> extract_T(String content) {
		var extracted = new ArrayList<String>();
		Matcher matcher = _T_PATTERN.matcher(content);
		while (matcher.find()) {
			String key = matcher.group(1);
			key = unescapeJava(key);
			if (!key.isEmpty()) {
				extracted.add(key);
			}
		}
		return extracted;
	}

	public static Collection<String> extract_wicket_t(String content) {
		var extracted = new ArrayList<String>();
		Document doc = Jsoup.parse(content);
		Elements wicketMessages = doc.select("wicket\\:t");
		for (Element element : wicketMessages) {
			String key = element.html().trim();
			key = key.replaceAll("\\s+", " ");
			if (!key.isEmpty()) {
				extracted.add(key);
			}
		}
		return extracted;
	}

	public static Pair<String, String> parseTranslation(String line) {
		Matcher matcher = TRANSLATION_PATTERN.matcher(line);
		if (matcher.find()) {
			return new Pair<>(unescapeJava(matcher.group(1)), unescapeJava(matcher.group(2)));
		} else {
			throw new RuntimeException("Invalid translation line: " + line);
		}
	}

	@Override
	public void stop() {
	}
	
}
