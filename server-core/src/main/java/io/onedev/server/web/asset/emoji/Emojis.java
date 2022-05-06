package io.onedev.server.web.asset.emoji;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;

public class Emojis {
	
	private static final Pattern PATTERN = Pattern.compile(":(\\S+?):");
	
	private final List<Emoji> emojis;
	
	private final Map<String, String> unicodes;
	
	private static class LazyHolder {
        private static final Emojis INSTANCE = new Emojis();
    }
 
    public static Emojis getInstance() {
        return LazyHolder.INSTANCE;
    }
    
	private Emojis() {
		try (InputStream in = Emojis.class.getResourceAsStream("emoji.json")) {
			JsonNode emojisNode = OneDev.getInstance(ObjectMapper.class).readTree(in);
			
			emojis = new ArrayList<Emoji>();
			unicodes = new LinkedHashMap<>();
			
			for (JsonNode emojiNode: emojisNode) {
				String unicode = emojiNode.get("emoji").asText();
				JsonNode aliasesNode = emojiNode.get("aliases");
				String alias = null;
				if (aliasesNode != null) {
					for (JsonNode aliasNode: aliasesNode) { 
						if (alias == null)
							alias = aliasNode.asText();
						unicodes.put(aliasNode.asText(), unicode);
					}
				}
				if (alias != null)
					emojis.add(new Emoji(unicode, alias));
			}
		} catch (IOException e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	public List<Emoji> list() {
		return emojis;
	}
	
	public String getUnicode(String alias) {
		return unicodes.get(alias);
	}
	
	public Collection<String> getAliases() {
		return unicodes.keySet();
	}
	
	public String apply(String content) {
		Matcher matcher = PATTERN.matcher(content);
	    StringBuffer buffer = new StringBuffer();
	    while (matcher.find()) {
	    	String alias = matcher.group(1);
	    	String unicode = getUnicode(alias);
	    	if (unicode != null)
	    		matcher.appendReplacement(buffer, Matcher.quoteReplacement(unicode));
	    	else
	    		matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group()));
	    }
	    matcher.appendTail(buffer);
	    
	    return buffer.toString();
	}
	
	public static class Emoji {
		
		private final String unicode;
		
		private final String name;
		
		public Emoji(String unicode, String name) {
			this.unicode = unicode;
			this.name = name;
		}

		public String getUnicode() {
			return unicode;
		}

		public String getName() {
			return name;
		}
		
	}
	
}
