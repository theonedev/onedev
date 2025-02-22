package io.onedev.server.web.page.admin.brandingsetting;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Image;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.model.support.administration.BrandingSetting;
import io.onedev.server.util.EditContext;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Editable
public class BrandSettingEditBean implements Serializable {
	
	private String name;
	
	private String url;
	
	private String logoData;
	
	private String darkLogoData;

	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@SuppressWarnings("unused")
	private static boolean isOEM() {
		return BrandingSetting.isOEM((String) EditContext.get().getInputValue("name"));
	}

	@Editable(order=150, description = "Specify url for your brand")
	@ShowCondition("isOEM")
	@NotEmpty
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
