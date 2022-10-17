package io.onedev.server.web.page.help;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.commandhandler.Upgrade;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public class IncompatibilitiesPage extends LayoutPage {

	private final IModel<String> incompatibilitiesSinceUpgradedVersionModel = new LoadableDetachableModel<String>() {

		@Override
		protected String load() {
			try {
				File incompatibilitiesSinceUpgradedVersionFile = 
						new File(Bootstrap.installDir, Upgrade.INCOMPATIBILITIES_SINCE_UPGRADED_VERSION);
				File checkedIncompatibilitiesSinceUpgradedVersionFile = 
						new File(Bootstrap.installDir, Upgrade.CHECKED_INCOMPATIBILITIES_SINCE_UPGRADED_VERSION);
				if (incompatibilitiesSinceUpgradedVersionFile.exists()) {
					String incompatibilitiesSinceUpgradedVersion = FileUtils.readFileToString(
							incompatibilitiesSinceUpgradedVersionFile, StandardCharsets.UTF_8);
					FileUtils.copyFile(incompatibilitiesSinceUpgradedVersionFile, 
							checkedIncompatibilitiesSinceUpgradedVersionFile);
					FileUtils.deleteFile(incompatibilitiesSinceUpgradedVersionFile);
					return incompatibilitiesSinceUpgradedVersion;
				} else if (checkedIncompatibilitiesSinceUpgradedVersionFile.exists()) {
					return FileUtils.readFileToString(
							checkedIncompatibilitiesSinceUpgradedVersionFile, StandardCharsets.UTF_8);
				} else {
					return null;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	};
	
	public IncompatibilitiesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onDetach() {
		incompatibilitiesSinceUpgradedVersionModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WebMarkupContainer("warning") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(incompatibilitiesSinceUpgradedVersionModel.getObject() != null);
			}
			
		});
		
		add(new MarkdownViewer("incompatibilities", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				try {
					if (incompatibilitiesSinceUpgradedVersionModel.getObject() != null) {
						return incompatibilitiesSinceUpgradedVersionModel.getObject();
					} else {
						return FileUtils.readFileToString(
								new File(Bootstrap.installDir, Upgrade.INCOMPATIBILITIES), 
								StandardCharsets.UTF_8);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		}, null));
		
		add(new Link<Void>("back") {

			@Override
			public void onClick() {
				continueToOriginalDestination();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(RestartResponseAtInterceptPageException.getOriginalUrl() != null);
			}
			
		});
		
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Incompatibilities");
	}

}
