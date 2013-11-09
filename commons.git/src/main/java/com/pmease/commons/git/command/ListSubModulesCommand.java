package com.pmease.commons.git.command;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.pmease.commons.git.Git;
import com.pmease.commons.util.StringUtils;

public class ListSubModulesCommand extends GitCommand<Map<String, String>> {

	private String revision;
	
	public ListSubModulesCommand(File repoDir) {
		super(repoDir);
	}
	
	public ListSubModulesCommand revision(String revision) {
		this.revision = revision;
		return this;
	}

	@Override
	public Map<String, String> call() {
		Map<String, String> modules = new HashMap<>();
		
		String content = new String(new Git(repoDir).show(revision, ".gitmodules"));
		
		String path = null;
		String url = null;
		
		for (String line: StringUtils.splitAndTrim(content, "\r\n")) {
			if (line.startsWith("[") && line.endsWith("]")) {
				if (path != null && url != null)
					modules.put(path, url);
				
				path = url = null;
			} else if (line.startsWith("path")) {
				path = StringUtils.substringAfter(line, "=").trim();
			} else if (line.startsWith("url")) {
				url = StringUtils.substringAfter(line, "=").trim();
			}
		}
		
		if (path != null && url != null)
			modules.put(path, url);
		
		return modules;
	}

}
