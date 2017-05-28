package com.gitplex.server.web.page.project.blob.search.quick;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.commons.io.Charsets;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.gitplex.server.GitPlex;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.support.TextRange;
import com.gitplex.server.search.SearchManager;
import com.gitplex.server.search.hit.FileHit;
import com.gitplex.server.search.hit.QueryHit;
import com.gitplex.server.search.query.BlobQuery;
import com.gitplex.server.search.query.FileQuery;
import com.gitplex.server.search.query.SymbolQuery;
import com.gitplex.server.search.query.TooGeneralQueryException;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.web.behavior.AbstractPostAjaxBehavior;
import com.gitplex.server.web.behavior.RunTaskBehavior;
import com.gitplex.server.web.component.link.ViewStateAwareAjaxLink;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;
import com.gitplex.server.web.page.project.blob.search.result.SearchResultPanel;
import com.gitplex.server.web.util.ajaxlistener.ConfirmLeaveListener;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

@SuppressWarnings("serial")
public abstract class QuickSearchPanel extends Panel {

	private static final int MAX_RECENT_OPENED = 30;
	
	private static final int MAX_QUERY_ENTRIES = 15;
	
	private static final String COOKIE_RECENT_OPENED = "quickSearch.recentOpened";
	
	private final IModel<Project> projectModel;
	
	private final IModel<String> revisionModel;
	
	private String searchInput;
	
	private List<QueryHit> symbolHits = new ArrayList<>();
	
	private RunTaskBehavior moreSymbolHitsBehavior;
	
	public QuickSearchPanel(String id, IModel<Project> projectModel, IModel<String> revisionModel) {
		super(id);
		
		this.projectModel = projectModel;
		this.revisionModel = revisionModel;
		
		Project project = projectModel.getObject();
		for (String blobPath: getRecentOpened()) {
			try {
				RevTree revTree = project.getRevCommit(revisionModel.getObject()).getTree();
				TreeWalk treeWalk = TreeWalk.forPath(project.getRepository(), blobPath, revTree);
				if (treeWalk != null && treeWalk.getRawMode(0) != FileMode.TREE.getBits()) {
					symbolHits.add(new FileHit(blobPath, null));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private List<String> getRecentOpened() {
		List<String> recentOpened = new ArrayList<>();
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_RECENT_OPENED);
		if (cookie != null && cookie.getValue() != null) {
			try {
				String decoded = URLDecoder.decode(cookie.getValue(), Charsets.UTF_8.name());
				recentOpened.addAll(Splitter.on("\n").splitToList(decoded));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		} 
		return recentOpened;
	}
	
	private List<QueryHit> querySymbols(String searchInput, int count) {
		SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
		ObjectId commit = projectModel.getObject().getRevCommit(revisionModel.getObject());		
		List<QueryHit> symbolHits = new ArrayList<>();
		try {
			// first try an exact search against primary symbol to make sure the result 
			// always contains exact match if exists
			BlobQuery query = new SymbolQuery.Builder()
					.term(searchInput)
					.primary(true)
					.count(count)
					.build();
			symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
			
			// now do wildcard search but exclude the exact match returned above 
			if (symbolHits.size() < count) {
				query = new SymbolQuery.Builder().term(searchInput+"*")
						.excludeTerm(searchInput)
						.primary(true)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
			}

			// do the same for file names
			if (symbolHits.size() < count) {
				query = new FileQuery.Builder()
						.fileNames(searchInput)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
			}
			
			if (symbolHits.size() < count) {
				query = new FileQuery.Builder().fileNames(searchInput+"*")
						.excludeFileName(searchInput)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
			}
			
			// do the same for secondary symbols
			if (symbolHits.size() < count) {
				query = new SymbolQuery.Builder()
						.term(searchInput)
						.primary(false)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
			}
			
			if (symbolHits.size() < count) {
				query = new SymbolQuery.Builder().term(searchInput+"*")
						.excludeTerm(searchInput)
						.primary(false)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
			}
			
		} catch (TooGeneralQueryException e) {
			symbolHits = new ArrayList<>();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return symbolHits;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		TextField<String> searchField = new TextField<>("input");
		add(searchField);
		newSearchResult(null);
		
		add(new AbstractPostAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel("blob-quick-search-input", AjaxChannel.Type.DROP));
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String key = params.getParameterValue("key").toString();

				if (key.equals("input")) {
					searchInput = params.getParameterValue("param").toString();
					if (StringUtils.isNotBlank(searchInput)) {
						symbolHits = querySymbols(searchInput, MAX_QUERY_ENTRIES);
					} else {
						symbolHits = new ArrayList<>();
					}
					newSearchResult(target);
				} else if (key.equals("return")) {
					int activeHitIndex = params.getParameterValue("param").toInt();
					QueryHit activeHit = getActiveHit(activeHitIndex);
					if (activeHit != null) {
						if (activeHit instanceof MoreSymbolHit) { 
							moreSymbolHitsBehavior.requestRun(target);
						} else {
							storeRecentOpened(activeHit.getBlobPath());
							onSelect(target, activeHit);
						}
					}
				} else {
					throw new IllegalStateException("Unrecognized key: " + key);
				}
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				String script = String.format(
						"gitplex.server.onQuickSearchDomReady('%s', %s);", 
						getMarkupId(), 
						getCallbackFunction(explicit("key"), explicit("param")));
				
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private void storeRecentOpened(String blobPath) {
		List<String> recentOpened = getRecentOpened();
		recentOpened.remove(blobPath);
		recentOpened.add(0, blobPath);
		while (recentOpened.size() > MAX_RECENT_OPENED)
			recentOpened.remove(recentOpened.size()-1);
		String encoded;
		try {
			encoded = URLEncoder.encode(Joiner.on("\n").join(recentOpened), Charsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		Cookie cookie = new Cookie(COOKIE_RECENT_OPENED, encoded);
		cookie.setMaxAge(Integer.MAX_VALUE);
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		response.addCookie(cookie);
	}
	
	private void newSearchResult(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer result = new WebMarkupContainer("result");
		result.setOutputMarkupId(true);
		
		result.add(new ListView<QueryHit>("symbolHits", new AbstractReadOnlyModel<List<QueryHit>>() {

			@Override
			public List<QueryHit> getObject() {
				return symbolHits;
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!symbolHits.isEmpty());
			}

			@Override
			protected void populateItem(ListItem<QueryHit> item) {
				QueryHit hit = item.getModelObject();
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						storeRecentOpened(hit.getBlobPath());
						onSelect(target, hit);
					}
					
				};
				link.add(hit.renderIcon("icon"));
				link.add(hit.render("label"));
				link.add(new Label("scope", hit.getNamespace()).setVisible(hit.getNamespace()!=null));
				item.add(link);

				BlobIdent blobIdent = new BlobIdent(revisionModel.getObject(), hit.getBlobPath(), 
						FileMode.REGULAR_FILE.getBits());
				ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
				state.mark = TextRange.of(hit.getTokenPos());
				PageParameters params = ProjectBlobPage.paramsOf(projectModel.getObject(), state);
				CharSequence url = RequestCycle.get().urlFor(ProjectBlobPage.class, params);
				link.add(AttributeAppender.replace("href", url.toString()));

				if (item.getIndex() == 0)
					item.add(AttributeModifier.append("class", "active"));
			}
			
		});
		result.add(new AjaxLink<Void>("moreSymbolHits") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(moreSymbolHitsBehavior = new RunTaskBehavior() {
					
					@Override
					protected void runTask(AjaxRequestTarget target) {
						List<QueryHit> hits = querySymbols(searchInput, SearchResultPanel.MAX_QUERY_ENTRIES);
						onMoreQueried(target, hits);
					}
					
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(searchInput != null && symbolHits.size() == MAX_QUERY_ENTRIES);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				moreSymbolHitsBehavior.requestRun(target);
			}
			
		});
		
		result.add(new WebMarkupContainer("noMatches") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(StringUtils.isNotBlank(searchInput) && symbolHits.isEmpty());
			}
			
		});

		result.add(new WebMarkupContainer("help") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(StringUtils.isBlank(searchInput) && symbolHits.isEmpty());
			}
			
		});
		
		if (target != null) {
			replace(result);
			target.add(result);
		} else {
			add(result);
		}
	}
	
	private QueryHit getActiveHit(int activeHitIndex) {
		List<QueryHit> hits = new ArrayList<>();
		hits.addAll(symbolHits);
		if (symbolHits.size() == MAX_QUERY_ENTRIES)
			hits.add(new MoreSymbolHit());

		if (hits.isEmpty())
			return null;
		else if (activeHitIndex <0) 
			return hits.get(0);
		else if (activeHitIndex>=hits.size())
			return hits.get(hits.size()-1);
		else
			return hits.get(activeHitIndex);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new QuickSearchResourceReference()));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		revisionModel.detach();
		
		super.onDetach();
	}

	protected abstract void onCancel(AjaxRequestTarget target);
	
	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);
	
	protected abstract void onMoreQueried(AjaxRequestTarget target, List<QueryHit> hits);
	
	private static class MoreSymbolHit extends QueryHit {

		public MoreSymbolHit() {
			super(null, null);
		}

		@Override
		public Component render(String componentId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getNamespace() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Image renderIcon(String componentId) {
			throw new UnsupportedOperationException();
		}
		
	}
	
}
