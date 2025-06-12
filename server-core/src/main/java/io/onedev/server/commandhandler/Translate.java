package io.onedev.server.commandhandler;

import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;
import org.unbescape.java.JavaEscape;
import org.unbescape.java.JavaEscapeLevel;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.Editable;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.MetricIndicator;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.translation.SystemPrompt;
import io.onedev.server.web.translation.Translation;
import io.onedev.server.web.util.TextUtils;

@Singleton
public class Translate extends CommandHandler {

	private static final Logger logger = LoggerFactory.getLogger(Translate.class);

	public static final String COMMAND = "translate";

	private static final Pattern METHOD_PATTERN = Pattern
			.compile("_T\\s*\\(\\s*\"((?:[^\"\\\\]|\\\\.)*)\"(?:\\s*\\+\\s*\"((?:[^\"\\\\]|\\\\.)*)\")*\\s*\\)");

	private static final Pattern TAG_PATTERN = Pattern.compile("<wicket:t>(.+?)</wicket:t>",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("\\Wt:[a-zA-Z0-9\\-_]+\\s*=\\s*(\"[^\"]*?\"|'[^']*?')",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");

	private static final Pattern MAP_CLEAR_PATTERN = Pattern.compile("\n\\s*m\\s*\\.\\s*clear\\s*\\(\\s*\\)\\s*;", Pattern.DOTALL);

	private static final Pattern INIT_END_PATTERN = Pattern.compile("\n\\s*}", Pattern.DOTALL);

	private static final Pattern VALID_TRANSLATION_PATTERN = Pattern.compile("^<a>(\\d+)</a>(.*)$");

	private static final int TRANSLATE_BATCH = 100;

	private static final String ENV_BASE_URL = "OPENAI_BASE_URL";

	private static final String ENV_API_KEY = "OPENAI_API_KEY";

	@Inject
	public Translate(HibernateConfig hibernateConfig) {
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

		String language = null;
		if (Bootstrap.command.getArgs().length > 1) {
			language = Bootstrap.command.getArgs()[1].replace("-", "_");
		} 

		var baseUrl = System.getenv(ENV_BASE_URL);
		var apiKey = System.getenv(ENV_API_KEY);
		if (baseUrl == null || apiKey == null) {
			logger.error("Missing environment variables: {} or {}", ENV_BASE_URL, ENV_API_KEY);
			System.exit(1);
		}

		try {
			logger.info("Scanning translation keys...");

			Set<String> scannedTranslationKeys = new TreeSet<>();

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
										scannedTranslationKeys.add(EditableUtils.getDisplayName(clazz));
										var group = EditableUtils.getGroup(clazz);
										if (group != null)
											scannedTranslationKeys.add(group);
										var description = editable.description();
										if (description.length() != 0) {
											scannedTranslationKeys.add(description);
										}
									}
									for (var method : clazz.getDeclaredMethods()) {
										editable = method.getAnnotation(Editable.class);
										if (editable != null) {
											scannedTranslationKeys.add(EditableUtils.getDisplayName(method));
											var group = EditableUtils.getGroup(method);
											if (group != null)
												scannedTranslationKeys.add(group);
											var description = editable.description();
											if (description.length() != 0) {
												scannedTranslationKeys.add(description);
											}
											var placeholder = editable.placeholder();
											if (placeholder.length() != 0) {
												scannedTranslationKeys.add(placeholder);
											}
											var rootPlaceholder = editable.rootPlaceholder();
											if (rootPlaceholder.length() != 0) {
												scannedTranslationKeys.add(rootPlaceholder);
											}
										}
										var notEmpty = method.getAnnotation(NotEmpty.class);
										if (notEmpty != null && notEmpty.message().length() != 0) {
											scannedTranslationKeys.add(notEmpty.message());
										}
										var notNull = method.getAnnotation(NotNull.class);
										if (notNull != null && notNull.message().length() != 0) {
											scannedTranslationKeys.add(notNull.message());
										}
										var size = method.getAnnotation(Size.class);
										if (size != null && size.message().length() != 0) {
											scannedTranslationKeys.add(size.message());
										}
										var metricIndicator = method.getAnnotation(MetricIndicator.class);
										if (metricIndicator != null) {
											if (metricIndicator.name().length() != 0)
												scannedTranslationKeys.add(metricIndicator.name());
											if (metricIndicator.group().length() != 0)
												scannedTranslationKeys.add(metricIndicator.group());
										}
									}
									if (clazz.isEnum()) {
										for (var constant : clazz.getEnumConstants()) {
											scannedTranslationKeys.add(TextUtils.getDisplayValue((Enum<?>) constant));
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
							scannedTranslationKeys.addAll(scanJavaMethods(Files.readString(it)));
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
							scannedTranslationKeys.addAll(scanHtmlTags(content));
							scannedTranslationKeys.addAll(scanHtmlAttributes(content));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});

			var client = OpenAIOkHttpClient.fromEnv();
			
			var javaDir = new File(projectDir, "server-core/src/main/java");

			var englishTranslations = new TreeMap<String, String>();
			Translation.init(englishTranslations);
			for (var key: scannedTranslationKeys) {
				englishTranslations.putIfAbsent(key, key);
			}
			for (var key: Translation.getExtraKeys()) {			
				englishTranslations.putIfAbsent(key, key);
			}

			for (var file : new File(javaDir, Translation.class.getPackageName().replace(".", "/")).listFiles()) {
				var relative = javaDir.toPath().relativize(file.toPath()).toString().replace(".java", "");
				var className = StringUtils.substringBeforeLast(relative, ".").replace("/", ".");
				if (className.startsWith(Translation.class.getName() + "_") && (language == null || className.endsWith("_" + language))) {
					logger.info("Processing file \"{}\"...", file.getName());
					var clazz = Class.forName(className);
					var existingTranslations = new TreeMap<String, String>();
					var initMethod = clazz.getDeclaredMethod("init", Map.class);
					initMethod.invoke(null, existingTranslations);

					String systemPrompt;
					var annotation = initMethod.getAnnotation(SystemPrompt.class);					
					if (annotation != null) { 
						systemPrompt = annotation.value();
					} else {
						var localeName = StringUtils.substringAfter(className, "_").replace("_", "-");
						var locale = Locale.forLanguageTag(localeName);
						systemPrompt = "You are good at translating from English to " + locale.getDisplayLanguage() + " in DevOps software area.";
					}				
					var userPromptPrefix =  
							"Now translate below lines. Each input line should produce exactly one output line. No merge or split. " + 
							"Each line should be translated independently, without using any context from other lines. Html tags " + 
							"and placeholders should be preserved. Content inside html tag <code> should not be translated.";

					var translator = new Function<List<String>, List<String>>() {
						@Override
						public List<String> apply(List<String> input) {	
							var translated = new TreeMap<Integer, String>();
							while (translated.size() < input.size()) {
								var untranslated = new ArrayList<String>();
								for (int i=0; i<input.size(); i++) {
									if (!translated.containsKey(i)) {
										untranslated.add("<a>" + i + "</a>" + input.get(i));
									}
								}
								logger.info("Translating {} lines...", untranslated.size());
								var createParams = ChatCompletionCreateParams.builder()
										.model(ChatModel.GPT_4O)
										.temperature(0.0)
										.addSystemMessage(systemPrompt)
										.addUserMessage(userPromptPrefix + "\n\n\n\n" + Joiner.on("\n\n").join(untranslated));	
								for (var line: Splitter.on('\n').split(client.chat().completions().create(createParams.build()).choices().get(0).message().content().get())) {		
									var matcher = VALID_TRANSLATION_PATTERN.matcher(line);
									if (matcher.matches()) {
										var index = Integer.parseInt(matcher.group(1));
										if (index >= 0 && index < input.size()) {
											var normalized = StringUtils.substringBefore(matcher.group(2), "<<<").trim();
											if (normalized.length() != 0)
												translated.put(index, normalized);
											else
												translated.put(index, input.get(index));	
										}
									}
								}
							}
							return translated.values().stream().collect(Collectors.toList());
						}
					};
					var content = generateFileContent(englishTranslations, existingTranslations, 
							Files.readString(file.toPath()), translator, TRANSLATE_BATCH);
					Files.writeString(file.toPath(), content);
				}
			}
		} catch (Throwable e) {
			logger.error("Error translating", e);
			System.exit(1);
		}					
		System.exit(0);
	}

	public static String generateFileContent(Map<String, String> englishTranslations, Map<String, String> existingTranslations, 
				String oldFileContent, Function<List<String>, List<String>> translator, int batchSize) {
		var matcher = MAP_CLEAR_PATTERN.matcher(oldFileContent);
		if (!matcher.find()) {
			throw new IllegalStateException("Map clear not found");
		}
		var mapClearEnd = matcher.end();
		var builder = new StringBuilder(oldFileContent.substring(0, mapClearEnd));

		for (var entry: existingTranslations.entrySet()) {
			if (englishTranslations.containsKey(entry.getKey())) {
				appendTranslation(builder, entry.getKey(), entry.getValue());
			}
		}		

		var newEntries = new ArrayList<Map.Entry<String, String>>();
		for (var entry: englishTranslations.entrySet()) {
			if (!existingTranslations.containsKey(entry.getKey())) {
				newEntries.add(entry);
			}
		}

		var count = new AtomicInteger(0);
		Lists.partition(newEntries, batchSize).forEach(batch -> {
			var translated = translator.apply(batch.stream().map(it-> {
				if (!it.getValue().equals(it.getKey())) {
					return it.getValue() + " <<<translation context: " + StringUtils.substringBefore(it.getKey(), ":") + ">>>";
				} else {
					return it.getValue();
				}
			}).collect(Collectors.toList()));			

			Preconditions.checkState(translated.size() == batch.size());

			for (int i=0; i<batch.size(); i++) {
				var entry = batch.get(i);
				appendTranslation(builder, entry.getKey(), translated.get(i));
			}			
			logger.info("Translated {}/{} entries", count.addAndGet(batch.size()), newEntries.size());
		});

		matcher = INIT_END_PATTERN.matcher(oldFileContent.substring(mapClearEnd));
		if (!matcher.find()) {
			throw new IllegalStateException("Init end not found");
		}
		builder.append(oldFileContent.substring(mapClearEnd + matcher.start()));

		return builder.toString();
	}

	public static Collection<String> scanJavaMethods(String content) {
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

	public static Collection<String> scanHtmlTags(String content) {
		var extracted = new ArrayList<String>();
		Matcher matcher = TAG_PATTERN.matcher(content);
		while (matcher.find()) {
			var key = matcher.group(1).trim().replaceAll("\\s+", " ");
			if (key.length() != 0)
				extracted.add(key);
		}
		return extracted;
	}

	public static Collection<String> scanHtmlAttributes(String content) {
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

	private static void appendTranslation(StringBuilder builder, String key, String value) {
		key = JavaEscape.escapeJava(key, JavaEscapeLevel.LEVEL_1_BASIC_ESCAPE_SET);
		value = JavaEscape.escapeJava(value, JavaEscapeLevel.LEVEL_1_BASIC_ESCAPE_SET);
		builder.append("\n\t\tm.put(\"" + key + "\", ");
		if (key.length() > 80)
			builder.append("\n\t\t\t");
		builder.append("\"" + value + "\");");
	}

	@Override
	public void stop() {
	}

}
