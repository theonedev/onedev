package io.onedev.server.plugin.sso.discord;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.commons.lang.StringUtils;

public class DiscordAuthorizationCodeResponse {
	private static final String CHARSET = "utf-8";
	private static final String DEFAULT_ERROR = "Unable to get authorization code";
	private static final String QUERY_CODE = "code";
	private static final String QUERY_STATE = "state";
	private static final String QUERY_ERROR = "error";
	
	private String code;
	private String state;
	private String error;
	
	public DiscordAuthorizationCodeResponse(String query) {
		Map<String,String> params = parseParameters(query);
		
		this.code = params.containsKey(QUERY_CODE) ? params.get(QUERY_CODE) : null;
		this.state = params.containsKey(QUERY_STATE) ? params.get(QUERY_STATE) : null;
		this.error = params.containsKey(QUERY_ERROR) ? params.get(QUERY_ERROR) : null;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getError() {
		return StringUtils.isEmpty(error) ? DEFAULT_ERROR : error;
	}
	
	public boolean hasValidCode() {
		return !StringUtils.isEmpty(code);
	}
	
	public boolean hasState(String state) {
		return StringUtils.equals(this.state, state);
	}
	
	private static Map<String,String> parseParameters(final String query) {
		
		Map<String,String> params = new HashMap<>();
		
		if (query == null || query.trim().isEmpty()) {
			return params; // empty map
		}
		
		try {
			StringTokenizer st = new StringTokenizer(query.trim(), "&");

			while(st.hasMoreTokens()) {

				String param = st.nextToken();

				String pair[] = param.split("=");

				String key = URLDecoder.decode(pair[0], CHARSET);
				
				// Save the first value only
				if (params.containsKey(key))
					continue;

				String value = "";

				if (pair.length > 1)
					value = URLDecoder.decode(pair[1], CHARSET);
				
				params.put(key, value);
			}
			
		} catch (UnsupportedEncodingException e) {
			
			// UTF-8 should always be supported
		}
		
		return params;
	}
}
