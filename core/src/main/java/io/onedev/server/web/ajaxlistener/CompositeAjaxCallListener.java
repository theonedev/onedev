package io.onedev.server.web.ajaxlistener;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;

@SuppressWarnings("serial")
public class CompositeAjaxCallListener implements IAjaxCallListener, IComponentAwareHeaderContributor {

	private IAjaxCallListener[] listeners;
	
	public CompositeAjaxCallListener(IAjaxCallListener... listeners) {
		this.listeners = listeners;
	}
	
	@Override
	public CharSequence getBeforeHandler(Component component) {
		StringBuffer mergedBeforeHandler = new StringBuffer();
		for (IAjaxCallListener listener: listeners) {
			CharSequence beforeHandler = listener.getBeforeHandler(component);
			if (beforeHandler != null)
				mergedBeforeHandler.append(beforeHandler);
		}
		if (mergedBeforeHandler.length() != 0)
			return mergedBeforeHandler;
		else
			return null;
	}

	@Override
	public CharSequence getPrecondition(Component component) {
		StringBuffer mergedPrecondition = new StringBuffer();
		for (IAjaxCallListener listener: listeners) {
			CharSequence precondition = listener.getPrecondition(component);
			if (precondition != null)
				mergedPrecondition.append(precondition);
		}
		if (mergedPrecondition.length() != 0)
			return mergedPrecondition;
		else
			return null;
	}

	@Override
	public CharSequence getBeforeSendHandler(Component component) {
		StringBuffer mergedBeforeSendHandler = new StringBuffer();
		for (IAjaxCallListener listener: listeners) {
			CharSequence beforeSendHandler = listener.getBeforeSendHandler(component);
			if (beforeSendHandler != null)
				mergedBeforeSendHandler.append(beforeSendHandler);
		}
		if (mergedBeforeSendHandler.length() != 0)
			return mergedBeforeSendHandler;
		else
			return null;
	}

	@Override
	public CharSequence getAfterHandler(Component component) {
		StringBuffer mergedAfterHandler = new StringBuffer();
		for (IAjaxCallListener listener: listeners) {
			CharSequence afterHandler = listener.getAfterHandler(component);
			if (afterHandler != null)
				mergedAfterHandler.append(afterHandler);
		}
		if (mergedAfterHandler.length() != 0)
			return mergedAfterHandler;
		else
			return null;
	}

	@Override
	public CharSequence getSuccessHandler(Component component) {
		StringBuffer mergedSuccessHandler = new StringBuffer();
		for (IAjaxCallListener listener: listeners) {
			CharSequence successHandler = listener.getSuccessHandler(component);
			if (successHandler != null)
				mergedSuccessHandler.append(successHandler);
		}
		if (mergedSuccessHandler.length() != 0)
			return mergedSuccessHandler;
		else
			return null;
	}

	@Override
	public CharSequence getFailureHandler(Component component) {
		StringBuffer mergedFailureHandler = new StringBuffer();
		for (IAjaxCallListener listener: listeners) {
			CharSequence failureHandler = listener.getFailureHandler(component);
			if (failureHandler != null)
				mergedFailureHandler.append(failureHandler);
		}
		if (mergedFailureHandler.length() != 0)
			return mergedFailureHandler;
		else
			return null;
	}

	@Override
	public CharSequence getCompleteHandler(Component component) {
		StringBuffer mergedCompleteHandler = new StringBuffer();
		for (IAjaxCallListener listener: listeners) {
			CharSequence completeHandler = listener.getCompleteHandler(component);
			if (completeHandler != null)
				mergedCompleteHandler.append(completeHandler);
		}
		if (mergedCompleteHandler.length() != 0)
			return mergedCompleteHandler;
		else
			return null;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		for (IAjaxCallListener listener: listeners) {
			if (listener instanceof IComponentAwareHeaderContributor) {
				IComponentAwareHeaderContributor headerContributor = (IComponentAwareHeaderContributor) listener;
				headerContributor.renderHead(component, response);
			}
		}
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
