package com.pmease.gitop.web.page.repository.source.blob.language;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MediaType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.pmease.gitop.web.util.MediaTypeUtils;
import com.pmease.gitop.web.util.StandardObjectMapper;

public enum Languages {
	INSTANCE;
	
	Languages() {
		init();
	}
	
	private Map<String, Language> languages;
	private Map<String, Language> mediaTypeIndex = Maps.newHashMap();
	
	private void init() {
		InputStream in = Languages.class.getResourceAsStream("languages.json");
		try {
			List<Language> list = StandardObjectMapper.getInstance().readValue(in, new TypeReference<List<Language>>(){});
			languages = Maps.newHashMap();
			for (Language each : list) {
				languages.put(each.getName(), each);
				for (String mime : each.getMediaTypes()) {
					mediaTypeIndex.put(mime.toLowerCase(), each);
				}
			}
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
	
	public @Nullable Language findByMediaType(String mime) {
		MediaType m = MediaType.parse(mime);
		return findByMediaType(m);
	}
	
	public @Nullable Language findByMediaType(MediaType mime) {
		Language lang = mediaTypeIndex.get(mime.toString());
		if (lang == null && MediaTypeUtils.isXMLType(mime)) {
			return languages.get("xml");
		}
		
		return lang;
	}
}
