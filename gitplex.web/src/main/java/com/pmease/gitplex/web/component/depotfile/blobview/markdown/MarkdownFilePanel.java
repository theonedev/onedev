package com.pmease.gitplex.web.component.depotfile.blobview.markdown;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.Blob;
import com.pmease.commons.wicket.behavior.ViewStateAwareBehavior;
import com.pmease.commons.wicket.component.PreventDefaultAjaxLink;
import com.pmease.commons.wicket.component.markdown.MarkdownPanel;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext.Mode;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewPanel;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;

@SuppressWarnings("serial")
public class MarkdownFilePanel extends BlobViewPanel {

	public MarkdownFilePanel(String id, BlobViewContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		add(new MarkdownPanel("markdown", Model.of(blob.getText().getContent()), null));
	}

	@Override
	public List<MenuItem> getMenuItems(MenuLink menuLink) {
		List<MenuItem> menuItems = new ArrayList<>();
		menuItems.add(new MenuItem() {
			
			@Override
			public String getIconClass() {
				return context.getMode() == Mode.BLAME?"fa fa-check":null;
			}

			@Override
			public String getLabel() {
				return "Blame";
			}

			@Override
			public AbstractLink newLink(String id) {
				AbstractLink link = new PreventDefaultAjaxLink<Void>(id) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						menuLink.close();
						context.onBlameChange(target, true);									
					}
					
				};
				PageParameters params;
				DepotFilePage.State state = new DepotFilePage.State();
				state.blobIdent = context.getBlobIdent();
				if (context.getMode() != Mode.BLAME)
					state.mode = Mode.BLAME;
				state.mark = context.getMark();
				params = DepotFilePage.paramsOf(context.getDepot(), state);
				CharSequence url = RequestCycle.get().urlFor(DepotFilePage.class, params);
				link.add(AttributeAppender.replace("href", url.toString()));
				link.add(new ViewStateAwareBehavior());
				return link;
			}
			
		});
		
		return menuItems;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new MarkdownFileResourceReference()));
	}

}
