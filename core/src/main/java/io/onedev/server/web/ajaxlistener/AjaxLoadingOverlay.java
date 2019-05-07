package io.onedev.server.web.ajaxlistener;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;

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
public class AjaxLoadingOverlay implements IAjaxCallListener, Serializable {

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

	@Override
	public CharSequence getInitHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getDoneHandler(Component component) {
		return null;
	}

}
