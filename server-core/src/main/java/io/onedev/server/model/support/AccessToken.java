package io.onedev.server.model.support;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.User;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.facade.AccessTokenFacade;
import org.apache.wicket.markup.html.basic.Label;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

@Editable
public class AccessToken implements Serializable {
    
    private static final long serialVersionUID = 1L;
	
    private String value = CryptoUtils.generateSecret();

	private String description;
	
	private Date createDate = new Date();
	
	private Date expireDate;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable(order=100, description = "Optionally specify description of the token")
	@Multiline
	@Nullable
	public String getDescription() {
		return description;
	}

	public void setDescription(@Nullable String description) {
		this.description = description;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Editable(order=200, placeholder = "Never expire", description = "Optionally specify " +
			"expiration date of the token. Leave empty to never expire")
	@Nullable
	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(@Nullable Date expireDate) {
		this.expireDate = expireDate;
	}

	public boolean isExpired() {
		return getExpireDate() != null && getExpireDate().before(new Date());
	}
	
	public String getMaskedValue() {
		var maskedValue = new StringBuilder();
		for (int i=0; i<value.length(); i++) {
			var ch = value.charAt(i);
			if (i >= 6)
				maskedValue.append("*");
			else
				maskedValue.append(ch);
		}
		return maskedValue.toString();
	}
	
}
