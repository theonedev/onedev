package io.onedev.server.web.component.brandlogo;

import java.io.File;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public class BrandLogoPanel extends Panel {

	public BrandLogoPanel(String id) {
		super(id);
	}

	private File getCustomLogoFile() {
		return new File(Bootstrap.getSiteDir(), "assets/logo.png");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ExternalImage("custom", "/logo.png?v=" + getCustomLogoFile().lastModified()) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getCustomLogoFile().exists());
			}
			
		});
		add(new SpriteImage("onedev", "logo") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getCustomLogoFile().exists());
			}
			
		});

		setRenderBodyOnly(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BrandLogoCssResourceReference()));
	}

}
