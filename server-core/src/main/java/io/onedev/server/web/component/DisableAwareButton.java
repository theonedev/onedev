package io.onedev.server.web.component;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Button;

public class DisableAwareButton extends Button {
	
	public DisableAwareButton(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		if (!isEnabled()) {
			var script = String.format("" +
							"var $button = $('#%s');" +
							"$button.attr('disabled', 'disabled');" +
							"$button.closest('form').removeClass('dirty');",
					getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}
	
}
