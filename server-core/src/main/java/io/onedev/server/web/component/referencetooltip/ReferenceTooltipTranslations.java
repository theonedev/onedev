package io.onedev.server.web.component.referencetooltip;

import static io.onedev.server.web.translation.Translation._T;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;

public class ReferenceTooltipTranslations {

	public static String get() {
		try {
			var map = new HashMap<String, String>();
			map.put("loading", _T("Loading..."));
			map.put("issue-not-exist-or-access-denied", _T("Issue not exist or access denied"));
			map.put("pull-request-not-exist-or-access-denied", _T("Pull request not exist or access denied"));
			map.put("build-not-exist-or-access-denied", _T("Build not exist or access denied"));
			map.put("commit-not-exist-or-access-denied", _T("Commit not exist or access denied"));
			map.put("workspace-not-exist-or-access-denied", _T("Workspace not exist or access denied"));
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(map);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
