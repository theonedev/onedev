package com.gitplex.commons.wicket.behavior.markdown;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

public class EmojiOnes {
	
	private static final ObjectMapper objectMapper = new ObjectMapper(); 
	
	private final Map<String, String> codes;
	
	private static class LazyHolder {
        private static final EmojiOnes INSTANCE = new EmojiOnes();
    }
 
    public static EmojiOnes getInstance() {
        return LazyHolder.INSTANCE;
    }
    
	private EmojiOnes() {
		try (InputStream in = EmojiOnes.class.getResourceAsStream("emoji.json")) {
			Map<String, Map<String, String>> json = 
					objectMapper.readValue(in, new TypeReference<LinkedHashMap<String, Map<String, String>>>() {});
			
			Map<String, String> map = new HashMap<>();
			
			for (Entry<String, Map<String, String>> each : json.entrySet()) {
				String code = each.getValue().get("unicode");
				map.put(each.getKey(), code);
				String aliases = each.getValue().get("aliases");
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
			throw Throwables.propagate(e);
		}
	}

	public Map<String, String> all() {
		return codes;
	}
	
}
