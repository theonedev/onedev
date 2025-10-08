package io.onedev.server.web.ajaxlistener;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.img.ImageScope;
import io.onedev.server.web.page.base.BasePage;

public class AttachAjaxIndicatorListener implements IAjaxCallListener {

	public enum AttachMode {PREPEND, APPEND};
	
	private final Component attachTo;

	private final AttachMode attachMode;
	
	private final boolean indicateSuccessful;
	
	private static final ResourceReference DARK_INDICATOR = new PackageResourceReference(
			ImageScope.class, "dark-ajax-indicator.gif");
	
	public AttachAjaxIndicatorListener(@Nullable Component attachTo, AttachMode attachMode, boolean indicateSuccessful) {
		this.attachTo = attachTo;
		this.attachMode = attachMode;
		this.indicateSuccessful = indicateSuccessful;
	}
	
	public AttachAjaxIndicatorListener(boolean indicateSuccessful) {
		this(null, AttachMode.APPEND, indicateSuccessful);
	}
	
	@Override
	public CharSequence getBeforeHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getPrecondition(Component component) {
		return null;
	}
	
	@Override
	public CharSequence getBeforeSendHandler(Component component) {
		Component attachTo = this.attachTo;
		if (attachTo == null)
			attachTo = component;
		IRequestHandler handler;
		BasePage page = (BasePage) component.getPage();
		if (page.isDarkMode())
			handler = new ResourceReferenceRequestHandler(DARK_INDICATOR);
		else
			handler = new ResourceReferenceRequestHandler(AbstractPostAjaxBehavior.INDICATOR);
			
		CharSequence url = RequestCycle.get().urlFor(handler);
		String insertAt = attachMode==AttachMode.APPEND?"after":"before";
		return String.format(""
				+ "$('#%s').siblings('.working-indicator').remove(); "
				+ "$('#%s').addClass('with-working-indicator').%s('<img src=\"%s\" width=\"16\" height=\"16\" class=\"working-indicator\"></img>');", 
				attachTo.getMarkupId(), attachTo.getMarkupId(), insertAt, url);
	}

	@Override
	public CharSequence getAfterHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getSuccessHandler(Component component) {
		Component attachTo = this.attachTo;
		if (attachTo == null)
			attachTo = component;
		if (indicateSuccessful) {
			String insertAt = attachMode==AttachMode.APPEND?"after":"before";
			return String.format(""
					+ "$('#%s').removeClass('with-working-indicator').siblings('.working-indicator').remove();"
					+ "$('#%s').%s('<svg class=\"icon working-indicator text-success\"><use xlink:href=\"%s\"/></svg>');", 
					attachTo.getMarkupId(), attachTo.getMarkupId(), insertAt, 
					SpriteImage.getVersionedHref(IconScope.class, "tick"));
		} else {
			return String.format("$('#%s').removeClass('with-working-indicator').siblings('.working-indicator').remove();", 
					attachTo.getMarkupId());
		}
	}

	@Override
	public CharSequence getFailureHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getCompleteHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getInitHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getDoneHandler(Component component) {
		return null;
	}

}
