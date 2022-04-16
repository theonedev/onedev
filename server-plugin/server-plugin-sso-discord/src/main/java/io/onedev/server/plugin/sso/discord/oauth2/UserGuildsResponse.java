package io.onedev.server.plugin.sso.discord.oauth2;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserGuildsResponse extends Response {
	public static final String ENDPOINT = "https://discord.com/api/users/@me/guilds";

	public UserGuildsResponse(String content, int responseCode) {
		super(content, responseCode);
	}
	
	public JSONObject getServerInfo(String serverId) throws JSONException {
		JSONArray guildsArray = getContentAsJSONArray();
		
		for(int idx = 0; idx < guildsArray.length(); ++idx) {
			JSONObject guildInfo = (JSONObject) guildsArray.get(idx);
			
			String guildId = (String) guildInfo.get("id");
			if (StringUtils.equals(guildId, serverId)) {
				return guildInfo;
			}
		}
		
		return null;
	}
}
