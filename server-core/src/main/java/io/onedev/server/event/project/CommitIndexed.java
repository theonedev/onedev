package io.onedev.server.event.project;

import java.io.Serial;
import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;

public class CommitIndexed extends ProjectEvent {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	private final ObjectId commitId;
	
	public CommitIndexed(Project project, ObjectId commitId) {
		super(null, new Date(), project);
		this.commitId = commitId;
	}

	public ObjectId getCommitId() {
		return commitId;
	}
	
	public static String getChangeObservable(String commitId) {
		return CommitIndexed.class.getName() + ":" + commitId;
	}

	@Override
	public String getActivity() {
		return "commit indexed";
	}
	
}
