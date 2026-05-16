package io.onedev.server.web.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

public class DisableAwareBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

	@Override
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		if (!component.isEnabled()) {
			var script = String.format("" +
							"var $button = $('#%s');" +
							"$button.attr('disabled', 'disabled');" +
							"$button.closest('form').removeClass('dirty');",
					component.getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}

}
