package io.onedev.server.commandhandler;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;
import static org.unbescape.java.JavaEscape.escapeJava;
import static org.unbescape.java.JavaEscape.unescapeJava;
import static org.unbescape.java.JavaEscapeLevel.LEVEL_1_BASIC_ESCAPE_SET;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.translation.Translation;
import io.onedev.server.web.translation.Translation_zh_CN;

@Singleton
public class TranslateOtherLanguages extends CommandHandler {

	private static final Logger logger = LoggerFactory.getLogger(TranslateOtherLanguages.class);

	public static final String COMMAND = "translate-other-languages";

	private static final Pattern TRANSLATION_PATTERN = Pattern.compile(
			"m\\s*\\.\\s*put\\s*\\(\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*,\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*\\)\\s*;");

	private static final Pattern MAP_CLEAR_PATTERN = Pattern.compile("\n\\s*m\\s*\\.\\s*clear\\s*\\(\\s*\\)\\s*;", Pattern.DOTALL);

	private static final Pattern INIT_END_PATTERN = Pattern.compile("\n\\s*}", Pattern.DOTALL);

	private static final int TRANSLATE_BATCH = 500;

	@Inject
	public TranslateOtherLanguages(HibernateConfig hibernateConfig) {
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

		try {
			var englishContent = Files.readString(new File(projectDir, "server-core/src/main/java/" + Translation.class.getPackageName().replace(".", "/")).toPath());
			var chineseContent = Files.readString(new File(projectDir, "server-core/src/main/java/" + Translation_zh_CN.class.getPackageName().replace(".", "/")).toPath());
			for (var file : new File(projectDir, "server-core/src/main/java/" + Translation.class.getPackageName().replace(".", "/")).listFiles()) {
				logger.info("Translating file \"{}\"...", file.getName());
				if (file.getName().startsWith(Translation.class.getSimpleName() + "_") && !file.getName().equals(Translation_zh_CN.class.getSimpleName())) {
					var content = translate(englishContent, chineseContent, Files.readString(file.toPath()), (it) -> it, TRANSLATE_BATCH);
					Files.writeString(file.toPath(), content);
				}
			}
		} catch (Exception e) {
			logger.error("Error translating", e);
			System.exit(1);
		}
		System.exit(0);
	}

	public static String translate(String englishContent, String chineseContent, String content, 
			Function<List<String>, List<String>> translator, int batchSize) {
		var translations = readTranslations(chineseContent);
		translations.keySet().removeAll(readTranslations(content).keySet());
		var englishTranslations = readTranslations(englishContent);
		var entries = new ArrayList<>(translations.entrySet());
		for (var entry: entries) {
			var value = englishTranslations.get(entry.getKey());
			if (value != null) {
				entry.setValue(value);
			} else {
				entry.setValue(null);
			}
		}

		var matcher = MAP_CLEAR_PATTERN.matcher(content);
		if (!matcher.find()) {
			throw new IllegalStateException("Map clear not found");
		}
		var mapClearEnd = matcher.end();
		matcher = INIT_END_PATTERN.matcher(content.substring(mapClearEnd));
		if (!matcher.find()) {
			throw new IllegalStateException("Init end not found");
		}
		var insertAt = mapClearEnd + matcher.start();
		var builder = new StringBuilder(content.substring(0, insertAt));
		Lists.partition(entries, batchSize).forEach(batch -> {
			var translated = translator.apply(batch.stream().map(it-> {
				if (it.getValue() != null) {
					return it.getValue() + "<<<" + StringUtils.substringBefore(it.getKey(), ":") + ">>>";
				} else {
					return it.getKey();
				}
			}).collect(toList()));			
			checkState(translated.size() == batch.size());
			for (int i=0; i<batch.size(); i++) {
				var entry = batch.get(i);
				var key = escapeJava(entry.getKey(), LEVEL_1_BASIC_ESCAPE_SET);
				var value = escapeJava(translated.get(i), LEVEL_1_BASIC_ESCAPE_SET);
				builder.append("\n\t\tm.put(\"" + key + "\", ");
				if (key.length() > 80)
					builder.append("\n\t\t\t");
				builder.append("\"" + value + "\");");		
			}
		});

		builder.append(content.substring(insertAt));

		return builder.toString();
	}

	private static TreeMap<String, String> readTranslations(String content) {
		var translations = new TreeMap<String, String>();
		var translationMatcher = TRANSLATION_PATTERN.matcher(content);
		while (translationMatcher.find()) {
			var key = unescapeJava(translationMatcher.group(1));
			var value = unescapeJava(translationMatcher.group(2));
			translations.put(key, value);
		}
		return translations;
	}

	@Override
	public void stop() {
	}

}
