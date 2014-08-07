package com.pmease.commons.git;

import org.eclipse.jgit.lib.FileMode;

public class GitUtils {
	
	public static String getTypeName(int blobType) {
		if (blobType == FileMode.TYPE_FILE)
			return "File";
		else if (blobType == FileMode.TYPE_GITLINK)
			return "Sub module";
		else if (blobType == FileMode.TYPE_SYMLINK)
			return "Symbol link";
		else 
			return "Folder";
	}
	
}
