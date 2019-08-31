package io.onedev.server.web.component.markdown.emoji;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;

public class EmojiOnes {
	
	private final Map<String, String> codes;
	
	private static class LazyHolder {
        private static final EmojiOnes INSTANCE = new EmojiOnes();
    }
 
    public static EmojiOnes getInstance() {
        return LazyHolder.INSTANCE;
    }
    
	private EmojiOnes() {
		try (InputStream in = EmojiOnes.class.getResourceAsStream("emoji.json")) {
			JsonNode emojis = OneDev.getInstance(ObjectMapper.class).readTree(in);
			
			Map<String, String> map = new HashMap<>();
			
			for (Iterator<Entry<String, JsonNode>> it = emojis.fields(); it.hasNext();) {
				Entry<String, JsonNode> entry = it.next();
				String code = entry.getValue().get("unicode").asText();
				map.put(entry.getKey(), code);
				String aliases = entry.getValue().get("aliases").asText();
				for (String alias : Splitter.on(" ").omitEmptyStrings().split(aliases)) {
					String name = alias.substring(1, alias.length() - 1);
					map.put(name, code);
				}
			}
			
			codes = new LinkedHashMap<>();
			List<String> keys = new ArrayList<>(map.keySet());
			Collections.sort(keys);
			for (String key: keys)
				codes.put(key, map.get(key));
			
		} catch (IOException e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	public Map<String, String> all() {
		return codes;
	}
	
}
