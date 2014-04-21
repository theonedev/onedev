package com.pmease.gitop.web.component.link;

import static org.parboiled.common.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public abstract class AvatarLink extends Panel {

	public enum Mode {NAME, AVATAR, NAME_AND_AVATAR}

	protected final Mode mode;
	
	protected TooltipConfig tooltipConfig;

	public AvatarLink(String id, Mode mode) {
		super(id);
		this.mode = checkNotNull(mode, "mode");
	}

	public AvatarLink(String id, IModel<?> model, Mode mode) {
		super(id, checkNotNull(model, "model"));
		this.mode = checkNotNull(mode, "mode");
	}

	public AvatarLink withTooltipConfig(@Nullable TooltipConfig tooltipConfig) {
		this.tooltipConfig = tooltipConfig;
		return this;
	}

}
