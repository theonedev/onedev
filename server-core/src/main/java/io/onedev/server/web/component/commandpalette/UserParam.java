package io.onedev.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.service.UserService;
import io.onedev.server.model.User;
import io.onedev.server.web.page.user.UserPage;

public class UserParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public UserParam(boolean optional) {
		super(UserPage.PARAM_USER, optional);
	}
	
	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		if (matchWith.length() == 0) 
			matchWith = null;
		UserService userService = OneDev.getInstance(UserService.class);
		for (User user: userService.query(matchWith, 0, count))
			suggestions.put(user.getDisplayName(), String.valueOf(user.getId()));
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		UserService userService = OneDev.getInstance(UserService.class);
		try {
			Long userId = Long.valueOf(matchWith);
			if (userService.get(userId) != null) 
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}
		
}
