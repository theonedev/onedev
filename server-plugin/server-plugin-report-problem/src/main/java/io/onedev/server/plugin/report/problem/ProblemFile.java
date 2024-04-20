package io.onedev.server.plugin.report.problem;

import io.onedev.server.codequality.CodeProblem;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import static io.onedev.server.codequality.CodeProblem.NON_REPO_FILE_PREFIX;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public class ProblemFile implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String blobPath;
	
	private final String moreInfo;
	
	private final boolean inRepo;
	
	private final Collection<CodeProblem> problems = new ArrayList<>();
	
	public ProblemFile(String blobPath) {
		if (blobPath.startsWith(NON_REPO_FILE_PREFIX)) {
			blobPath = blobPath.substring(NON_REPO_FILE_PREFIX.length());
			inRepo = false;
		} else {
			inRepo = true;
		}
		if (blobPath.endsWith(")") && blobPath.contains("(")) {
			moreInfo = substringBeforeLast(substringAfterLast(blobPath, "("), ")");
			blobPath = substringBeforeLast(blobPath, "(").trim();
		} else {
			moreInfo = null;
		}
		this.blobPath = blobPath;
	}

	public String getBlobPath() {
		return blobPath;
	}

	@Nullable
	public String getMoreInfo() {
		return moreInfo;
	}

	public boolean isInRepo() {
		return inRepo;
	}

	public Collection<CodeProblem> getProblems() {
		return problems;
	}
	
	@Override
	public String toString() {
		var builder = new StringBuilder();
		if (!inRepo)
			builder.append(NON_REPO_FILE_PREFIX);
		builder.append(blobPath);
		if (moreInfo != null)
			builder.append(" (").append(moreInfo).append(")");
		return builder.toString();
	}
	
}