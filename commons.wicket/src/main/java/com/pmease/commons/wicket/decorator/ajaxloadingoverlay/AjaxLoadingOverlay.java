package com.pmease.commons.wicket.decorator.ajaxloadingoverlay;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

/**
 * Ajax loading overlay is used to froze the page to prevent users from clicking anything to avoid 
 * the wicket ComponentNotFoundException in case an ajax request has replaced part of the 
 * page at server side, but the page at browser side has not been updated yet. 
 * To use it, overwrite updateAjaxAttributes() method of various ajax components or 
 * behaviors and add below statement:<br>
 * <code>attributes.getAjaxCallListeners().add(new AjaxLoadingOverlay());</code> 
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class AjaxLoadingOverlay implements IAjaxCallListener, IComponentAwareHeaderContributor {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				AjaxLoadingOverlay.class, "ajax-loading-overlay.js")));
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				AjaxLoadingOverlay.class, "ajax-loading-overlay.css")));
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
		return "$('#ajax-loading-overlay').show();";
	}

	@Override
	public CharSequence getAfterHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getSuccessHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getFailureHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getCompleteHandler(Component component) {
		return "$('#ajax-loading-overlay').hide();";
	}

}
