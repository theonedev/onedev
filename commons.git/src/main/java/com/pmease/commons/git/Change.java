package com.pmease.commons.git;

import static com.pmease.commons.git.Change.Status.RENAMED;

import java.io.Serializable;
import java.util.StringTokenizer;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.util.QuotedString;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.WordUtils;

@SuppressWarnings("serial")
public class Change implements Comparable<Change>, Serializable {

	public enum Status {ADDED, MODIFIED, DELETED, RENAMED, UNCHANGED}
	
	private final Status status;
	
	private final BlobIdent oldBlobIdent;
	
	private final BlobIdent newBlobIdent;
	
	public Change(Status status, BlobIdent oldBlobIdent, BlobIdent newBlobIdent) {
		this.status = status;
		this.oldBlobIdent = oldBlobIdent;
		this.newBlobIdent = newBlobIdent;
	}
	
	public Status getStatus() {
		return status;
	}

	public BlobIdent getOldBlobIdent() {
		return oldBlobIdent;
	}

	public BlobIdent getNewBlobIdent() {
		return newBlobIdent;
	}

	@Override
	public String toString() {
		if (status == Status.RENAMED)
			return status.name() + "\t" + oldBlobIdent.path + "->" + newBlobIdent.path;
		else if (status == Status.DELETED)
			return status.name() + "\t" + oldBlobIdent.path;
		else 
			return status.name() + "\t" + newBlobIdent.path;
	}
	
	public boolean isFolder() {
		return oldBlobIdent.mode == FileMode.TYPE_TREE || newBlobIdent.mode == FileMode.TYPE_TREE;
	}
	
	@Override
	public int compareTo(Change other) {
		if (isFolder()) {
			if (other.isFolder())
				return getPath().compareTo(other.getPath());
			else
				return -1;
		} else if (other.isFolder()) {
			return 1;
		} else {
			return getPath().compareTo(other.getPath());
		}
	}
	
	public String getPath() {
		if (status == Status.DELETED)
			return oldBlobIdent.path;
		else
			return newBlobIdent.path;
	}

	private static String dequoteFileName(String quotedFileName) {
		return QuotedString.GIT_PATH.dequote(quotedFileName);
	}

	public static Change parseRawLine(String oldRev, String newRev, String rawLine) {
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
			BlobIdent oldBlobIdent = new BlobIdent(oldRev, oldPath, oldMode);
			BlobIdent newBlobIdent = new BlobIdent(newRev, newPath, newMode);
			return new Change(Change.Status.RENAMED, oldBlobIdent, newBlobIdent);
		} else if (statusCode.equals("M") || statusCode.equals("T")) {
			String oldPath, newPath;
			oldPath = newPath = dequoteFileName(tokenizer.nextToken("\t"));
			BlobIdent oldBlobIdent = new BlobIdent(oldRev, oldPath, oldMode);
			BlobIdent newBlobIdent = new BlobIdent(newRev, newPath, newMode);
			return new Change(Change.Status.MODIFIED, oldBlobIdent, newBlobIdent);
		} else if (statusCode.equals("D")) {
			String oldPath = dequoteFileName(tokenizer.nextToken("\t"));
			BlobIdent oldBlobIdent = new BlobIdent(oldRev, oldPath, oldMode);
			BlobIdent newBlobIdent = new BlobIdent(newRev, null, newMode);
			return new Change(Change.Status.DELETED, oldBlobIdent, newBlobIdent);
		} else if (statusCode.equals("A")) {
			String newPath = dequoteFileName(tokenizer.nextToken("\t"));
			BlobIdent oldBlobIdent = new BlobIdent(oldRev, null, oldMode);
			BlobIdent newBlobIdent = new BlobIdent(newRev, newPath, newMode);
			return new Change(Change.Status.ADDED, oldBlobIdent, newBlobIdent);
		} else {
			throw new RuntimeException("Unexpected status code: " + statusCode);
		}
	}
	
	public String getHint() {
		if (getStatus() == RENAMED)
			return "Renamed from " + oldBlobIdent.path;
		else
			return WordUtils.capitalize(getStatus().name().toLowerCase());
	}

	public String getName() {
		if (getPath().contains("/"))
			return StringUtils.substringAfterLast(getPath(), "/");
		else
			return getPath();
	}
	
}
