package io.onedev.server.util.facade;

import javax.annotation.Nullable;

public class EmailAddressFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final boolean primary;
	
	private final boolean git;
	
	private final String verificationCode;
	
	private final Long ownerId;
	
	public EmailAddressFacade(Long id, String value, boolean primary, boolean git, 
			@Nullable String verificationCode, Long ownerId) {
		super(id);
		this.value = value;
		this.primary = primary;
		this.git = git;
		this.verificationCode = verificationCode;
		this.ownerId = ownerId;
	}

	public String getValue() {
		return value;
	}

	public boolean isPrimary() {
		return primary;
	}

	public boolean isGit() {
		return git;
	}

	@Nullable
	public String getVerificationCode() {
		return verificationCode;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public boolean isVerified() {
    	return getVerificationCode() == null;
    }
	
}
