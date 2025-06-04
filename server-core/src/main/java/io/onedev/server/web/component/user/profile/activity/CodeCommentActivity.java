package io.onedev.server.web.component.user.profile.activity;

import java.util.Date;

import io.onedev.server.model.CodeComment;
import io.onedev.server.security.SecurityUtils;

public abstract class CodeCommentActivity extends UserActivity {

    public CodeCommentActivity(Date date) {
        super(date);
    }

    public abstract CodeComment getComment();

    public boolean isAccessible() {
        return SecurityUtils.canReadCode(getComment().getProject());
    }

    public Type getType() {
        return Type.PULL_REQUEST_AND_CODE_REVIEW;
    }

}