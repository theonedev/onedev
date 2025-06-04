package io.onedev.server.web.component.user.profile.activity;

import java.io.Serializable;
import java.util.Date;

import org.apache.wicket.Component;

public abstract class UserActivity implements Serializable {
    
    public enum Type {ISSUE, CODE_COMMIT, PULL_REQUEST_AND_CODE_REVIEW}
    
    private final Date date;

    public UserActivity(Date date) {
        this.date = date;
    }
    
    public final Date getDate() {
        return date;
    }
    
    public abstract boolean isAccessible();

    public abstract Type getType();

    public abstract Component render(String id);
    
}
