package io.onedev.server.service.support;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.User;

public class AuditQuery implements Serializable {
    
    private final List<User> users;

    private final Date sinceDate;

    private final Date untilDate;

    private final String action;

    public AuditQuery(List<User> users, @Nullable Date sinceDate, @Nullable Date untilDate, @Nullable String action) {
        this.users = users;
        this.sinceDate = sinceDate;
        this.untilDate = untilDate;
        this.action = action;
    }

    public List<User> getUsers() {
        return users;
    }

    @Nullable
    public Date getSinceDate() {
        return sinceDate;
    }

    @Nullable
    public Date getUntilDate() {
        return untilDate;
    }

    @Nullable
    public String getAction() {
        return action;
    }

}
