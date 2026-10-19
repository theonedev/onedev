package io.onedev.server.web.page.admin.brandingsetting;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Image;
import io.onedev.server.oem.OemUtils;

@Editable
public class BrandSettingEditBean implements Serializable {
	
	private String name;
		
	private String logoData;
	
	private String darkLogoData;

	@Editable(order=100, placeholderProvider="getNamePlaceholder")
	@Nullable
	public String getName() {
		return name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	@SuppressWarnings("unused")
	private static String getNamePlaceholder() {
		return OemUtils.getProductName();
	}
		
	@Editable(order=200, name="Logo for Light Mode", description = "Upload a 128x128 transparent png file to be used " +
			"as logo for light mode")
	@Image(accept = "image/png")
	@NotEmpty
	public String getLogoData() {
		return logoData;
	}

	public void setLogoData(String logoData) {
		this.logoData = logoData;
	}

	@Editable(order=300, name="Logo for Dark Mode", description = "Upload a 128x128 transparent png file to be used " +
			"as logo for dark mode")
	@Image(accept="image/png", backgroundColor = "#23232d")
	@NotEmpty
	public String getDarkLogoData() {
		return darkLogoData;
	}

	public void setDarkLogoData(String darkLogoData) {
		this.darkLogoData = darkLogoData;
	}
	
}
