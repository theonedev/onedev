package io.onedev.server.plugin.sso.discord.oauth2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
	private static final String CHARSET = "utf-8";
	
	public static Object getValue(JSONObject jsonObject, String key) throws JSONException {
		return jsonObject.has(key) ? jsonObject.get(key) : null;
	}
	
	public static String serializeParameters(final Map<String,String> params) {
		if (params == null || params.isEmpty())
			return "";
		
		StringBuilder sb = new StringBuilder();
		
		for (Map.Entry<String,String> entry: params.entrySet()) {
			
			if (entry.getKey() == null || entry.getValue() == null)
				continue;
			
			try {
				String encodedKey = URLEncoder.encode(entry.getKey(), CHARSET);
				String encodedValue = URLEncoder.encode(entry.getValue(), CHARSET);
				
				if (sb.length() > 0)
					sb.append('&');
				
				sb.append(encodedKey);
				sb.append('=');
				sb.append(encodedValue);
	
			} catch (UnsupportedEncodingException e) {
				// UTF-8 should always be supported
			}
		}
		
		return sb.toString();
	}
}
