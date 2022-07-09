package io.onedev.server.model.support.administration.mailsetting;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ImapAuth extends Serializable {

	@Nullable
	String getUserName(OtherMailSetting otherMailSetting);
	
	@Nullable
	String getPassword(OtherMailSetting otherMailSetting);
	
}
