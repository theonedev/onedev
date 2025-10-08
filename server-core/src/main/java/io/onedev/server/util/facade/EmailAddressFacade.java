package io.onedev.server.util.facade;

import org.jspecify.annotations.Nullable;

public class EmailAddressFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long ownerId;
	
	private final String value;
	
	private final boolean primary;
	
	private final boolean git;

	private final boolean open;
	
	private final String verificationCode;
	
	public EmailAddressFacade(Long id, Long ownerId, String value, boolean primary, 
							  boolean git, boolean open, @Nullable String verificationCode) {
		super(id);
		this.ownerId = ownerId;
		this.value = value;
		this.primary = primary;
		this.git = git;
		this.open = open;
		this.verificationCode = verificationCode;
	}

	public Long getOwnerId() {
		return ownerId;
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

	public boolean isOpen() {
		return open;
	}
	
	@Nullable
	public String getVerificationCode() {
		return verificationCode;
	}

	public boolean isVerified() {
    	return getVerificationCode() == null;
    }
	
}
