package io.onedev.server.search.commit;

import static io.onedev.server.search.commit.CommitCriteria.getRuleName;
import static io.onedev.server.search.commit.CommitQueryLexer.BRANCH;
import static io.onedev.server.search.commit.CommitQueryLexer.BUILD;
import static io.onedev.server.search.commit.CommitQueryLexer.COMMIT;
import static io.onedev.server.search.commit.CommitQueryLexer.DefaultBranch;
import static io.onedev.server.search.commit.CommitQueryLexer.SINCE;
import static io.onedev.server.search.commit.CommitQueryLexer.TAG;
import static io.onedev.server.search.commit.CommitQueryLexer.UNTIL;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.service.BuildService;
import io.onedev.server.entityreference.BuildReference;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;

public class Revision implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public enum Type {BRANCH, TAG, BUILD, COMMIT};
	
	private final Type type;

	private final String value;
	
	private final boolean since;
	
	public Revision(Type type, @Nullable String value, boolean since) {
		this.type = type;
		this.value = value;
		this.since = since;
	}
	
	public Type getType() {
		return type;
	}

	@Nullable
	public String getValue() {
		return value;
	}

	public boolean isSince() {
		return since;
	}
	
	public Build getValueAsBuild(Project project) {
		var buildReference = BuildReference.of(value, project);
		var buildService = OneDev.getInstance(BuildService.class);
		var build = buildService.find(buildReference.getProject(), buildReference.getNumber());
		if (build == null)
			throw new ExplicitException("Unable to find build: " + value);
		else
			return build;
	}

	public RevCommit getRevCommit(Project project) {
		if (type == Type.BRANCH) {
			var branch = value != null? value: project.getDefaultBranch();
			return project.getRevCommit(GitUtils.branch2ref(branch), true);
		} else if (type == Type.TAG) {
			return project.getRevCommit(GitUtils.tag2ref(value), true);
		} else if (type == Type.COMMIT) {
			return project.getRevCommit(value, true);
		} else if (type == Type.BUILD) {
			return project.getRevCommit(getValueAsBuild(project).getCommitHash(), true);
		} else {
			throw new RuntimeException("Unknown revision type: " + type);
		}
	}

	public boolean matchesRef(Project project, String refName) {
		if (type == Type.BRANCH) {
			var branch = value != null? value: project.getDefaultBranch();
			return refName.equals(GitUtils.branch2ref(branch));
		} else if (type == Type.TAG) {
			return refName.equals(GitUtils.tag2ref(value));
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		String revision;
		if (value == null) {
			revision = getRuleName(DefaultBranch);
		} else {
			int typeRuleName;
			switch (type) {
				case BRANCH:
					typeRuleName = BRANCH;
					break;
				case TAG:
					typeRuleName = TAG;
					break;
				case BUILD:
					typeRuleName = BUILD;
					break;
				case COMMIT:
					typeRuleName = COMMIT;
					break;
				default:
					throw new RuntimeException("Unknown revision type: " + type);
			}
			revision = getRuleName(typeRuleName) + "(" + value + ")";
		}
		if (since)
			revision = getRuleName(SINCE) + " " + revision;
		else
			revision = getRuleName(UNTIL) + " " + revision;
		return revision;
	}
	
}