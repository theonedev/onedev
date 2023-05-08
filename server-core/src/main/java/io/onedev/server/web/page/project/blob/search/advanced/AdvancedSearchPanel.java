package io.onedev.server.web.page.project.blob.search.advanced;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.insidecommit.CodeSearchManager;
import io.onedev.server.search.code.insidecommit.query.SymbolQuery;
import io.onedev.server.search.code.query.FileQueryOption;
import io.onedev.server.search.code.query.QueryOption;
import io.onedev.server.search.code.query.SymbolQueryOption;
import io.onedev.server.search.code.query.TextQueryOption;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.RunTaskBehavior;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.ObjectId;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class AdvancedSearchPanel extends Panel {

	private static final MetaDataKey<Class<? extends QueryOption>> ACTIVE_TAB =
			new MetaDataKey<>() {};
	
	private static final MetaDataKey<HashMap<Class<?>, QueryOption>> QUERY_OPTIONS =
			new MetaDataKey<>() {};

	private static final MetaDataKey<Boolean> INSIDE_CURRENT_DIR =
			new MetaDataKey<>() {};
	
	private final IModel<Project> projectModel;
	
	private final IModel<String> revisionModel;
	
	private Form<?> form;

	private FormComponentPanel<? extends QueryOption> optionEditor;	
	
	private QueryOption option = new TextQueryOption();
	
	private boolean insideCurrentDir;
	
	public AdvancedSearchPanel(String id, IModel<Project> projectModel, IModel<String> revisionModel) {
		super(id);
		
		this.projectModel = projectModel;
		this.revisionModel = revisionModel;

		Class<? extends QueryOption> activeTab = WebSession.get().getMetaData(ACTIVE_TAB);
		if (activeTab != null) {
			try {
				option = activeTab.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
		
		Map<Class<?>, QueryOption> savedOptions = getSavedOptions();
		if (savedOptions.containsKey(option.getClass()))
			option = savedOptions.get(option.getClass());
		
		Boolean insideCurrentDir = WebSession.get().getMetaData(INSIDE_CURRENT_DIR);
		if (insideCurrentDir != null)
			this.insideCurrentDir = insideCurrentDir; 
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		add(form);
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		List<Tab> tabs = new ArrayList<Tab>();
		tabs.add(new AjaxActionTab(Model.of("Text occurrences")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				option = new TextQueryOption();
				onSelectTab(target);
			}
			
		}.setSelected(option instanceof TextQueryOption));
		
		tabs.add(new AjaxActionTab(Model.of("File names")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				option = new FileQueryOption();
				onSelectTab(target);
			}
			
		}.setSelected(option instanceof FileQueryOption));
		
		tabs.add(new AjaxActionTab(Model.of("Symbol names")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				option = new SymbolQueryOption();
				onSelectTab(target);
			}
			
		}.setSelected(option instanceof SymbolQueryOption));
		
		form.add(new Tabbable("tabs", tabs));
		form.add(optionEditor = option.newOptionEditor("option"));
		optionEditor.setOutputMarkupId(true);
		form.add(new CheckBox("insideCurrentDir", new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return insideCurrentDir;
			}

			@Override
			public void setObject(Boolean object) {
				insideCurrentDir = object;
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getCurrentBlob() != null && getCurrentBlob().path != null);
			}

		});
		
		form.add(new AjaxButton("search") {

			private RunTaskBehavior runTaskBehavior;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(runTaskBehavior = new RunTaskBehavior() {
					
					@Override
					protected void runTask(AjaxRequestTarget target) {
						List<QueryHit> hits;
						var project = projectModel.getObject();
						var revision = revisionModel.getObject();
						var count = getMaxQueryEntries();
						if (revision != null) {
							try {
								var searchManager = OneDev.getInstance(CodeSearchManager.class);
								if (option instanceof SymbolQueryOption) {
									SymbolQuery.Builder builder = (SymbolQuery.Builder) option.newInsideCommitQueryBuilder();
									var query = builder.primary(true)
											.directory(getDirectory(insideCurrentDir))
											.count(count)
											.build();
									ObjectId commit = project.getRevCommit(revision, true);
									hits = searchManager.search(project, commit, query);

									if (hits.size() < count) {
										query = builder.primary(false)
												.directory(getDirectory(insideCurrentDir))
												.count(count)
												.build();
										hits.addAll(searchManager.search(project, commit, query));
									}
								} else {
									var query = option.newInsideCommitQueryBuilder()
											.directory(getDirectory(insideCurrentDir))
											.count(count)
											.build();
									ObjectId commit = project.getRevCommit(revision, true);
									hits = searchManager.search(project, commit, query);
								}
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						} else {
							hits = new ArrayList<>();
						}
						
						HashMap<Class<?>, QueryOption> savedOptions = getSavedOptions();
						savedOptions.put(option.getClass(), option);
						WebSession.get().setMetaData(QUERY_OPTIONS, savedOptions);
						
						onSearchComplete(target, hits);
					}
					
				});
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				option = optionEditor.getModelObject();
				runTaskBehavior.requestRun(target);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
	}
	
	private void onSelectTab(AjaxRequestTarget target) {
		WebSession.get().setMetaData(ACTIVE_TAB, option.getClass());
		optionEditor = option.newOptionEditor("option");
		optionEditor.setOutputMarkupId(true);
		form.replace(optionEditor);
		target.add(optionEditor);
	}
	
	private HashMap<Class<?>, QueryOption> getSavedOptions() {
		HashMap<Class<?>, QueryOption> savedOptions = WebSession.get().getMetaData(QUERY_OPTIONS);
		if (savedOptions == null)
			savedOptions = new HashMap<>();
		return savedOptions;
	}
	
	@Override
	protected void onDetach() {
		projectModel.detach();
		revisionModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AdvancedSearchResourceReference()));
	}

	protected abstract void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	@Nullable
	protected abstract BlobIdent getCurrentBlob();
	
	private static int getMaxQueryEntries() {
		return OneDev.getInstance(SettingManager.class).getPerformanceSetting().getMaxCodeSearchEntries();
	}
	
	protected String getDirectory(boolean insideDir) {
		BlobIdent blobIdent = getCurrentBlob();
		if (blobIdent == null || blobIdent.path == null || !blobIdent.path.contains("/") || !insideDir) 
			return null;
		else if (blobIdent.isTree()) 
			return blobIdent.path;
		else 
			return StringUtils.substringBeforeLast(blobIdent.path, "/");
	}
	
}