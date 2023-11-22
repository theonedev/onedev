package io.onedev.server.web.page.project.builds.detail.pack;

import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.packs.detail.PackDetailPage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;
import java.util.stream.Collectors;

public class BuildPacksPage extends BuildDetailPage {
	
	private static final String PARAM_TYPE = "type";
	
	private final IModel<List<Pack>> packsModel;
	
	public BuildPacksPage(PageParameters params) {
		super(params);
		
		var packType = params.get(PARAM_TYPE).toString();
		packsModel = new LoadableDetachableModel<>() {
			@Override
			protected List<Pack> load() {
				return getBuild().getPacks().stream()
						.filter(it -> it.getType().equals(packType) && SecurityUtils.canReadPack(it.getProject()))
						.sorted().collect(Collectors.toList());
			}
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<>("packs", packsModel) {

			@Override
			protected void populateItem(ListItem<Pack> item) {
				var pack = item.getModelObject();
				var label = pack.getVersion();
				if (!pack.getProject().equals(getProject()))
					label = pack.getProject().getPath() + ":" + label;
				var link = new BookmarkablePageLink<Void>("title", PackDetailPage.class, 
						PackDetailPage.paramsOf(pack));
				link.add(new Label("label", label));
				item.add(link);
				item.add(pack.getSupport().render("body", pack));
			}
		});
	}

	@Override
	protected void onDetach() {
		packsModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildPacksCssResourceReference()));
	}

	public static PageParameters paramsOf(Build build, String packType) {
		PageParameters params = paramsOf(build);
		params.add(PARAM_TYPE, packType);
		return params;
	}
	
}
