package io.onedev.server.git.service;

import org.eclipse.jgit.lib.ObjectId;

public class RevertResult {
    private ObjectId revertedCommitId;
    private ObjectId originalCommitId;

    public RevertResult(ObjectId revertedCommitId, ObjectId originalCommit) {
        this.revertedCommitId = revertedCommitId;
        this.originalCommitId = originalCommit;
    }

    public ObjectId getRevertedCommitId() {
        return revertedCommitId;
    }

    public ObjectId getOriginalCommitId() {
        return originalCommitId;
    }
}