package io.onedev.server.model.support.administration;

import java.io.Serial;
import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import io.onedev.server.oem.OemUtils;

public class BrandingSetting implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
			
	private String name;
		
	@Nullable
	public String getName() {
		return name;
	}
	
	public void setName(@Nullable String name) {
		this.name = name;
	}

	public String getEffectiveName() {
		return name != null ? name : OemUtils.getProductName();
	}
		
}
