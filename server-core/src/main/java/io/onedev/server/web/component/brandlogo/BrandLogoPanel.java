package io.onedev.server.web.component.brandlogo;

import java.io.File;
import java.io.Serializable;

import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.server.web.page.base.BasePage;

public class BrandLogoPanel extends Panel {
	
	private final Boolean darkMode;
	
	public BrandLogoPanel(String id, @Nullable Boolean darkMode) {
		super(id);
		this.darkMode = darkMode;
	}

	public BrandLogoPanel(String id) {
		this(id, null);
	}
	
	private File getLogoFile() {
		if (isDarkMode()) 
			return new File(Bootstrap.getSiteDir(), "assets/logo-dark.png");
		else
			return new File(Bootstrap.getSiteDir(), "assets/logo.png");
	}
	
	private boolean isDarkMode() {
		if (darkMode != null)
			return darkMode;
		else
			return ((BasePage) getPage()).isDarkMode();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ExternalImage("logo", new LoadableDetachableModel<>() {
			@Override
			protected Serializable load() {
				if (getLogoFile().exists())
					return "/" + getLogoFile().getName() + "?v=" + getLogoFile().lastModified();
				else
					return "/~img/logo.png";
			}

		}));

		setRenderBodyOnly(true);
	}

}
