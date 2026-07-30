package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Min;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;

public class AiSetting implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public enum PullRequestAssigneeResponsibility {
        MERGE_IF_ACCEPTABLE, FIX_FAILED_BUILDS, RESOLVE_MERGE_CONFLICTS
    }

    private static final int DEFAULT_MAX_LOOP_COUNT = 3;

    private AiModelSetting modelSetting;

    private boolean entitleToAll;

    private String systemPrompt;   
    
    private List<PullRequestAssigneeResponsibility> pullRequestAssigneeResponsibilities = Lists.newArrayList(
        PullRequestAssigneeResponsibility.FIX_FAILED_BUILDS,
        PullRequestAssigneeResponsibility.RESOLVE_MERGE_CONFLICTS);

    private boolean proactive;

    private int maxLoopCount = DEFAULT_MAX_LOOP_COUNT;

    public AiModelSetting getModelSetting() {
        return modelSetting;
    }

    public void setModelSetting(AiModelSetting modelSetting) {
        this.modelSetting = modelSetting;
    }

    public boolean isEntitleToAll() {
        return entitleToAll;
    }

    public void setEntitleToAll(boolean entitleToAll) {
        this.entitleToAll = entitleToAll;
    }

    @Nullable
    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(@Nullable String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public List<PullRequestAssigneeResponsibility> getPullRequestAssigneeResponsibilities() {
        return pullRequestAssigneeResponsibilities;
    }

    public void setPullRequestAssigneeResponsibilities(List<PullRequestAssigneeResponsibility> pullRequestAssigneeResponsibilities) {
        this.pullRequestAssigneeResponsibilities = pullRequestAssigneeResponsibilities;
    }

    public boolean isProactive() {
        return proactive;
    }

    public void setProactive(boolean proactive) {
        this.proactive = proactive;
    }

    @Min(value=1, message="At least 1 should be specified")
    public int getMaxLoopCount() {
        return maxLoopCount > 0 ? maxLoopCount : DEFAULT_MAX_LOOP_COUNT;
    }

    public void setMaxLoopCount(int maxLoopCount) {
        this.maxLoopCount = maxLoopCount;
    }

}
