package com.pmease.gitop.web.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public enum Languages {
	INSTANCE;
	
	private Map<String, Language> nameIndex;
	private Map<String, Language> aliasIndex = Maps.newHashMap();
	private Map<String, Language> primaryExtensionIndex = Maps.newHashMap();
	private Map<String, Language> fileNameIndex = Maps.newHashMap();
	private Map<String, Language> extensionIndex = Maps.newHashMap();
	
	Languages() {
		initLanguages();
	}
	
	private void initLanguages() {
		InputStream in = null;
		try {
			in = Languages.class.getResourceAsStream("languages.yml");
			
			ObjectMapper mapper = getObjectMapper();
			Map<String, Language> languages = mapper.readValue(in, new TypeReference<Map<String, Language>>() {});
			
			for (Entry<String, Language> each : languages.entrySet()) {
				Language language = each.getValue();
				language.setId(each.getKey());
				
				if (language.getAliases() != null) {
					for (String alias : language.getAliases()) {
						aliasIndex.put(alias, language);
					}
				}

				primaryExtensionIndex.put(language.getPrimaryExtension(), language);
				
				if (language.getFilenames() != null) {
					for (String fileName : language.getFilenames()) {
						fileNameIndex.put(fileName, language);
					}
				}
				
				if (language.getExtensions() != null) {
					for (String ext : language.getExtensions()) {
						extensionIndex.put(ext, language);
					}
				}
			}
			
			nameIndex = languages;
		} catch (JsonParseException e) {
			throw Throwables.propagate(e);
		} catch (JsonMappingException e) {
			throw Throwables.propagate(e);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private ObjectMapper getObjectMapper() {
		YAMLFactory jsonFactory = new YAMLFactory();
		jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
		// to allow (non-standard) unquoted field names in JSON:
		jsonFactory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
		// to allow use of apostrophes (single quotes), non standard
		jsonFactory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
		jsonFactory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
		jsonFactory
				.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
		
		ObjectMapper mapper = new ObjectMapper(jsonFactory);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
		mapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
		mapper.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
		return mapper;
	}
	
	public Collection<Language> getLanguages() {
		return nameIndex.values();
	}
	
	public Optional<Language> findLanguageByName(String name) {
		Language l = nameIndex.get(name);
		return Optional.fromNullable(l);
	}
	
	public Optional<Language> findLanguageByExtension(final String ext) {
		String e = ext;
		if (!ext.startsWith(".")) {
			e = "." + ext;
		}
		
		if (primaryExtensionIndex.containsKey(e)) {
			return Optional.of(primaryExtensionIndex.get(e));
		}
		
		return Optional.fromNullable(extensionIndex.get(e));
	}
	
	public Optional<Language> guessLanguage(String fileName) {
		String name = FilenameUtils.getName(fileName);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
		
		if (fileNameIndex.containsKey(name)) {
			return Optional.of(fileNameIndex.get(name));
		}
		
		String ext = FilenameUtils.getExtension(fileName);
		if (Strings.isNullOrEmpty(ext)) {
			return Optional.absent();
		}
		
		ext = "." + ext.toLowerCase();
		
		if (primaryExtensionIndex.containsKey(ext)) {
			return Optional.of(primaryExtensionIndex.get(ext));
		}
		
		if (extensionIndex.containsKey(ext)) {
			return Optional.of(extensionIndex.get(ext));
		}
		
		return Optional.absent();
	}
}
