package io.onedev.server.web.component.commandpalette;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.web.page.admin.ServerDetailPage;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServerParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public ServerParam() {
		super(ServerDetailPage.PARAM_SERVER, false);
	}
	
	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		if (matchWith.length() == 0) 
			matchWith = null;
		for (String server: OneDev.getInstance(ClusterManager.class).getServerAddresses()) {
			if (matchWith == null || server.toLowerCase().contains(matchWith.toLowerCase()))
				suggestions.put(server, server);
		}
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		return OneDev.getInstance(ClusterManager.class).getServer(matchWith, false) != null;
	}
		
}
