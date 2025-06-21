package io.onedev.server.model.support.administration;

import java.io.Serializable;

import javax.validation.constraints.Min;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;

@Editable
public class AuditSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_PRESERVE_DAYS = "preserveDays";
	
	private int preserveDays = 365;

	@Editable(order = 100, name="Preserve Days", description = "Audit log will be preserved for the specified number of days. " + 
			"This setting applies to all audit events, including system level and project level")
	@OmitName
	@Min(value = 7, message = "At least 7 days should be specified")
	public int getPreserveDays() {
		return preserveDays;
	}

	public void setPreserveDays(int preserveDays) {
		this.preserveDays = preserveDays;
	}
	
}
