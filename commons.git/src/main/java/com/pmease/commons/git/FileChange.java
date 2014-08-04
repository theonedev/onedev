package com.pmease.commons.git;

import java.io.Serializable;
import java.util.StringTokenizer;

import org.eclipse.jgit.util.QuotedString;

import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public class FileChange implements Serializable {

	public enum Status {ADD, MODIFY, DELETE, RENAME, TYPE}
	
	private final String oldPath;
	
	private final String newPath;
	
	private final int oldMode;
	
	private final int newMode;
	
	private final Status status;
	
	public FileChange(Status status, String oldPath, String newPath, int oldMode, int newMode) {
		this.status = status;
		this.oldPath = oldPath;
		this.newPath = newPath;
		this.oldMode = oldMode;
		this.newMode = newMode;
	}
	
	public FileChange(FileChange copy) {
		this.status = copy.status;
		this.oldPath = copy.oldPath;
		this.newPath = copy.newPath;
		this.oldMode = copy.oldMode;
		this.newMode = copy.newMode;
	}

	public String getOldPath() {
		return oldPath;
	}
	
	public String getNewPath() {
		return newPath;
	}

	public int getOldMode() {
		return oldMode;
	}

	public int getNewMode() {
		return newMode;
	}

	public Status getStatus() {
		return status;
	}

	@Override
	public String toString() {
		if (status == Status.RENAME)
			return status.name() + "\t" + oldPath + "->" + newPath;
		else if (status == Status.DELETE)
			return status.name() + "\t" + oldPath;
		else 
			return status.name() + "\t" + newPath;
	}
	
	private static String dequoteFileName(String quotedFileName) {
		return QuotedString.GIT_PATH.dequote(quotedFileName);
	}

	public static FileChange parseRawLine(String rawLine) {
		Preconditions.checkArgument(rawLine.startsWith(":"));
		
		StringTokenizer tokenizer = new StringTokenizer(rawLine.substring(1));
		int oldMode = Integer.parseInt(tokenizer.nextToken(), 8);
		int newMode = Integer.parseInt(tokenizer.nextToken(), 8);
		tokenizer.nextToken();
		tokenizer.nextToken();
		String statusCode = tokenizer.nextToken();
		if (statusCode.startsWith("R")) {
			String oldPath = dequoteFileName(tokenizer.nextToken("\t"));
			String newPath = dequoteFileName(tokenizer.nextToken("\t"));
			return new FileChange(FileChange.Status.RENAME, oldPath, newPath, oldMode, newMode);
		} else if (statusCode.equals("M")) {
			String oldPath, newPath;
			oldPath = newPath = dequoteFileName(tokenizer.nextToken("\t"));
			return new FileChange(FileChange.Status.MODIFY, oldPath, newPath, oldMode, newMode);
		} else if (statusCode.equals("D")) {
			String oldPath = dequoteFileName(tokenizer.nextToken("\t"));
			return new FileChange(FileChange.Status.DELETE, oldPath, null, oldMode, newMode);
		} else if (statusCode.equals("T")) {
			String oldPath, newPath;
			oldPath = newPath = dequoteFileName(tokenizer.nextToken("\t"));
			return new FileChange(FileChange.Status.TYPE, oldPath, newPath, oldMode, newMode);
		} else if (statusCode.equals("A")) {
			String newPath = dequoteFileName(tokenizer.nextToken("\t"));
			return new FileChange(FileChange.Status.ADD, null, newPath, oldMode, newMode);
		} else {
			throw new RuntimeException("Unexpected status code: " + statusCode);
		}
	}
}
