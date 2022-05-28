package io.onedev.server.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.onedev.server.model.support.BaseGpgKey;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@Entity
@Table(indexes={@Index(columnList="o_emailAddress_id"), @Index(columnList="keyId")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class GpgKey extends BaseGpgKey {
    
    private static final long serialVersionUID = 1L;
    
    public static final String PROP_KEY_ID = "keyId";
    
    @JsonIgnore
    @Column(unique=true)
    private long keyId;

    @JsonIgnore
    @Column(nullable=false)
    private Date createdAt;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable=false)
    private EmailAddress emailAddress;
    
    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

    public EmailAddress getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(EmailAddress emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
}
