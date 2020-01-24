package de.agilecoders.wicket.core.markup.html.bootstrap.behavior;

import org.apache.wicket.markup.head.IHeaderResponse;

import de.agilecoders.wicket.core.settings.IBootstrapSettings;

/*
 * Remove bootstrap resource reference, as otherwise it causes issue #14 in production mode
 */
public class BootstrapJavascriptBehavior extends BootstrapBaseBehavior {

	private static final long serialVersionUID = 1L;

    @Override
    public void renderHead(IBootstrapSettings settings, IHeaderResponse headerResponse) {
        super.renderHead(settings, headerResponse);
    }
}
