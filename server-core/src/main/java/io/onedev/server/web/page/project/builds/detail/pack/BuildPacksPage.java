package io.onedev.server.web.page.project.builds.detail.pack;

import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.pack.PackQuery;
import io.onedev.server.search.entity.pack.PackQueryLexer;
import io.onedev.server.search.entity.pack.PublishedViaBuildCriteria;
import io.onedev.server.search.entity.pack.TypeCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.web.component.pack.list.PackListPanel;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class BuildPacksPage extends BuildDetailPage {
	
	private static final String PARAM_PACK_TYPE = "type";
	
	private final String packType;
	
	private final IModel<List<Pack>> packsModel;
	
	public BuildPacksPage(PageParameters params) {
		super(params);
		
		packType = params.get(PARAM_PACK_TYPE).toString();
		
		packsModel = new LoadableDetachableModel<>() {
			@Override
			protected List<Pack> load() {
				return getBuild().getPacks().stream()
						.filter(it -> it.getType().equals(packType) && SecurityUtils.canReadPack(it.getProject()))
						.sorted().collect(Collectors.toList());
			}
		};
	}

	public String getPackType() {
		return packType;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PackListPanel("packs", Model.of((String)null), false) {

			@Nullable
			@Override
			protected Project getProject() {
				return null;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected PackQuery getBaseQuery() {
				return new PackQuery(new AndCriteria<>(
						new PublishedViaBuildCriteria(getBuild()), 
						new TypeCriteria(packType, PackQueryLexer.Is)));
			}
			
		});
	}

	@Override
	protected void onDetach() {
		packsModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Build build, String packType) {
		PageParameters params = paramsOf(build);
		params.add(PARAM_PACK_TYPE, packType);
		return params;
	}
	
}
