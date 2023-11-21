package io.onedev.server.web.component.pack.side;

import io.onedev.server.model.Pack;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class PackSidePanel extends Panel {

	public PackSidePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var build = getPack().getBuild();
		if (build != null) {
			var label = build.getReference(getPack().getProject());
			var buildLink = new BookmarkablePageLink<Void>("publisher",
					BuildDashboardPage.class, BuildDashboardPage.paramsOf(build)) {
				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					if (!isEnabled())
						tag.setName("span");
				}

				@Override
				public IModel<?> getBody() {
					return Model.of(label);
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setEnabled(SecurityUtils.canAccessBuild(build));
				}
			};
			add(buildLink);
		} else {
			add(new UserIdentPanel("publisher", getPack().getUser(), Mode.NAME));
		}

		add(new Label("publishDate", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return DateUtils.formatAge(getPack().getPublishDate());
			}

		}));

		if (SecurityUtils.canWritePack(getPack().getProject()))
			add(newDeleteLink("delete"));
		else
			add(new WebMarkupContainer("delete").setVisible(false));
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new PackSideCssResourceReference()));
	}

	protected abstract Pack getPack();

	protected abstract Component newDeleteLink(String componentId);
	
}
