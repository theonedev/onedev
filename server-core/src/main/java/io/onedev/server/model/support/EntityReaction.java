package io.onedev.server.model.support;

import javax.persistence.*;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.User;

@MappedSuperclass
public abstract class EntityReaction extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    public static final String PROP_USER = "user";

    @Column(nullable = false)
    private String emoji;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable=false)
    private User user;

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    protected abstract AbstractEntity getEntity();
    
} 