package com.pmease.gitplex.web.component.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.ContentQuery;

@SuppressWarnings("serial")
public class BlobSearcher extends Panel {

	private final IModel<Repository> repoModel;
	
	private final IModel<String> commitHashModel;
	
	private TextField<String> input;
	
	public BlobSearcher(String id, IModel<Repository> repoModel, IModel<String> commitHashModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.commitHashModel = commitHashModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final WebMarkupContainer matchesContainer = new WebMarkupContainer("matchesContainer");
		matchesContainer.setOutputMarkupId(true);
		matchesContainer.add(new ListView<QueryHit>("matches", new LoadableDetachableModel<List<QueryHit>>() {

			@Override
			protected List<QueryHit> load() {
				String symbol = input.getInput();

				if (StringUtils.isNotBlank(symbol)) {
					SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
					ContentQuery query = new ContentQuery(symbol, true, 100);
					try {
						return searchManager.search(repoModel.getObject(), commitHashModel.getObject(), query);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				} else {
					return new ArrayList<>();
				}
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<QueryHit> item) {
				item.add(item.getModelObject().render("match"));
			}
			
		});
		add(matchesContainer);
		
		input = new TextField<>("input", Model.of(""));
		input.add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(matchesContainer);
			}
			
		});
		add(input);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		commitHashModel.detach();
		
		super.onDetach();
	}

}
