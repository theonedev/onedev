package com.pmease.gitop.web.page.project.source.blob.language;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.pmease.gitop.web.util.MimeTypeUtils;
import com.pmease.gitop.web.util.StandardObjectMapper;

public enum Languages {
	INSTANCE;
	
	Languages() {
		init();
	}
	
	private Map<String, Language> languages;
	private Map<String, Language> mimesIndex = Maps.newHashMap();
	
	private void init() {
		InputStream in = Languages.class.getResourceAsStream("languages.json");
		try {
			List<Language> list = StandardObjectMapper.getInstance().readValue(in, new TypeReference<List<Language>>(){});
			languages = Maps.newHashMap();
			for (Language each : list) {
				languages.put(each.getName(), each);
				for (String mime : each.getMimeTypes()) {
					mimesIndex.put(mime.toLowerCase(), each);
				}
			}
//			ObjectMapper mapper = getObjectMapper();
//			this.languages = mapper.readValue(in, new TypeReference<Map<String, Language>>() {});
//			for (Entry<String, Language> entry : languages.entrySet()) {
//				Language lang = entry.getValue();
//				lang.name = entry.getKey();
//				
//				for (String mime : lang.getMimeTypes()) {
//					mimesIndex.put(mime.toLowerCase(), lang);
//				}
//			}
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
	
	public Collection<Language> getLanguages() {
		return languages.values();
	}
	
	public @Nullable Language findByMime(String mime) {
		try {
			MimeType m = MimeTypes.getDefaultMimeTypes().forName(mime);
			return findByMime(m);
		} catch (MimeTypeException e) {
			throw Throwables.propagate(e);
		}
	}
	
	public @Nullable Language findByMime(MimeType mime) {
		Language lang = mimesIndex.get(mime.getType().toString());
		if (lang == null && MimeTypeUtils.isXMLType(mime)) {
			return languages.get("xml");
		}
		
		return lang;
	}
	
	public static void main(String[] args) {
		System.out.println(Languages.INSTANCE.getLanguages());
	}
}
