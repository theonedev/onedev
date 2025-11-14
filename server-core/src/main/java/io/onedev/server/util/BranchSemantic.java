package io.onedev.server.util;

import org.jspecify.annotations.Nullable;

public class BranchSemantic {

    private final boolean workInProgress;

    private final String workType;

    private final String workDescription;

    public BranchSemantic(boolean workInProgress, @Nullable String workType, @Nullable  String workDescription) {
        this.workInProgress = workInProgress;
        this.workType = workType;
        this.workDescription = workDescription;
    }

    public boolean isWorkInProgress() {
        return workInProgress;
    }

    @Nullable
    public String getWorkType() {
        return workType;
    }

    @Nullable
    public String getWorkDescription() {
        return workDescription;
    }
    
}