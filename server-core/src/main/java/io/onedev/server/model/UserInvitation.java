package io.onedev.server.model;

import static io.onedev.server.model.UserInvitation.PROP_EMAIL_ADDRESS;
import static io.onedev.server.model.UserInvitation.PROP_INVITATION_CODE;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.onedev.server.annotation.Editable;

@Entity
@Table(indexes={@Index(columnList=PROP_EMAIL_ADDRESS), @Index(columnList=PROP_INVITATION_CODE)})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class UserInvitation extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_EMAIL_ADDRESS = "emailAddress";
	
	public static final String PROP_INVITATION_CODE = "invitationCode";
	
	@Column(unique=true, nullable=false)
    private String emailAddress;
	
	@Column(unique=true, nullable=false)
    @JsonIgnore
    private String invitationCode = UUID.randomUUID().toString();
	
	private boolean inviteAsGuest;

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getInvitationCode() {
		return invitationCode;
	}

	public void setInvitationCode(String invitationCode) {
		this.invitationCode = invitationCode;
	}

	public boolean isInviteAsGuest() {
		return inviteAsGuest;
	}

	public void setInviteAsGuest(boolean inviteAsGuest) {
		this.inviteAsGuest = inviteAsGuest;
	}
}
