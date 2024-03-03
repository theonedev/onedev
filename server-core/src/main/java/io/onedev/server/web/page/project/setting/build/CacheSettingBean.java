package io.onedev.server.web.page.project.setting.build;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.model.support.build.ProjectBuildSetting;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

@Editable
public class CacheSettingBean implements Serializable {
	
	private Integer preserveDays;

	@Editable(placeholder = "Inherit from parent", rootPlaceholder = ProjectBuildSetting.DEFAULT_CACHE_PRESERVE_DAYS + " days", 
			description = "Cache will be deleted to save space if not accessed for this number of days")
	@OmitName
	@Min(1)
	@Max(365)
	public Integer getPreserveDays() {
		return preserveDays;
	}

	public void setPreserveDays(Integer preserveDays) {
		this.preserveDays = preserveDays;
	}
	
}
