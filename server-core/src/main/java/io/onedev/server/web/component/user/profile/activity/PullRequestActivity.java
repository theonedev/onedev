package io.onedev.server.web.component.user.profile.activity;

import java.util.Date;

import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;

public abstract class PullRequestActivity extends UserActivity {

    public PullRequestActivity(Date date) {
        super(date);
    }

    public abstract PullRequest getPullRequest();

    public boolean isAccessible() {
        return SecurityUtils.canReadCode(getPullRequest().getProject());
    }

    @Override
    public Type getType() {
        return Type.PULL_REQUEST_AND_CODE_REVIEW;
    }

}