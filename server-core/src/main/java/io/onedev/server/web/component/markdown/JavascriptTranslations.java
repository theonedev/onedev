package io.onedev.server.web.component.markdown;

import static io.onedev.server.web.translation.Translation._T;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;

public class JavascriptTranslations {
	
    static String get() {
		try {
            var map = new HashMap<String, String>();
            
            map.put("loading", _T("Loading..."));
            map.put("loading-emojis", _T("Loading emojis..."));
            map.put("commented-code-outdated", _T("Commented code is outdated"));
            map.put("suggest-changes", _T("Suggest changes")); 
            map.put("upload-should-be-less-than", _T("Upload should be less than {0} Mb"));
            map.put("uploading-file", _T("Uploading file"));
            map.put("unable-to-connect-to-server", _T("Unable to connect to server"));
            map.put("programming-language", _T("Programming language"));
            map.put("copy-to-clipboard", _T("Copy to clipboard"));
            map.put("suggestion-outdated", _T("Suggestion is outdated either due to code change or pull request close"));
            map.put("remove-from-batch", _T("Remove from batch"));
            map.put("add-to-batch", _T("Add to batch to commit with other suggestions later"));
            map.put("commit-suggestion", _T("Commit suggestion"));
            map.put("issue-not-exist-or-access-denied", _T("Issue not exist or access denied"));
            map.put("pull-request-not-exist-or-access-denied", _T("Pull request not exist or access denied"));
            map.put("build-not-exist-or-access-denied", _T("Build not exist or access denied"));
            map.put("commit-not-exist-or-access-denied", _T("Commit not exist or access denied"));
            map.put("enter-description-here", _T("Enter description here"));

            return OneDev.getInstance(ObjectMapper.class).writeValueAsString(map);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
    }
}