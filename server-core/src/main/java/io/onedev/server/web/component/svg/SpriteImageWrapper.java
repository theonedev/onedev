package io.onedev.server.web.component.svg;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;

public class SpriteImageWrapper extends Panel {

	private static final long serialVersionUID = 1L;

	private final String href;
	
	private SpriteImage image;
	
	public SpriteImageWrapper(String id, @Nullable String href) {
		super(id);
		this.href = href;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(image = new SpriteImage("image", href));
		image.setVisible(href != null);
	}

	public SpriteImage getImage() {
		return image;
	}
	
}
