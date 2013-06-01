package com.pmease.commons.wicket.decorator.ajaxloadingindicator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

/**
 * Display an ajax loading indicator at top of the page. To use it, overwrite updateAjaxAttributes() method 
 * of various ajax components or behaviors and add below statement:<br>
 * <code>attributes.getAjaxCallListeners().add(new AjaxLoadingIndicator());</code>   
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class AjaxLoadingIndicator implements IAjaxCallListener, IComponentAwareHeaderContributor {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				AjaxLoadingIndicator.class, "ajax-loading-indicator.js")));
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				AjaxLoadingIndicator.class, "ajax-loading-indicator.css")));
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
		return "$('#ajax-loading-indicator').show();";
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
		return "$('#ajax-loading-indicator').hide();";
	}

}
