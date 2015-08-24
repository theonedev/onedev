package com.pmease.gitplex.web.component.diff.revision.option;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.LineProcessor;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.ajaxlistener.IndicateLoadingListener;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.behavior.menu.CheckItem;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.diff.revision.DiffMode;
import com.pmease.gitplex.web.component.diff.revision.LineProcessOption;
import com.pmease.gitplex.web.component.pathselector.PathSelector;

@SuppressWarnings("serial")
public abstract class DiffOptionPanel extends Panel {

	private static final String COOKIE_DIFF_MODE = "gitplex.diff.mode";
	
	private final IModel<Repository> repoModel;
	
	private final IModel<String> newRevModel;
	
	private LineProcessor lineProcessor = LineProcessOption.IGNORE_NOTHING;
	
	private DiffMode diffMode;
	
	public DiffOptionPanel(String id, IModel<Repository> repoModel, IModel<String> newRevModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.newRevModel = newRevModel;
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_DIFF_MODE);
		if (cookie == null)
			diffMode = DiffMode.UNIFIED;
		else
			diffMode = DiffMode.valueOf(cookie.getValue());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		MenuPanel lineProcessorMenu = new MenuPanel("lineProcessorMenu") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				
				for (final LineProcessOption option: LineProcessOption.values()) {
					menuItems.add(new CheckItem() {

						@Override
						protected String getLabel() {
							return option.getName();
						}

						@Override
						protected boolean isChecked() {
							return lineProcessor == option;
						}

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
							if (getDirtyContainer() != null)
								attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(getDirtyContainer()));
						}

						@Override
						protected void onClick(AjaxRequestTarget target) {
							lineProcessor = option;
							onLineProcessorChange(target);
						}
						
					});
				}

				return menuItems;
			}	
			
		};
		add(lineProcessorMenu);
		add(new WebMarkupContainer("lineProcessor").add(new MenuBehavior(lineProcessorMenu)));
		
		for (final DiffMode each: DiffMode.values()) {
			add(new AjaxLink<Void>(each.name().toLowerCase()) {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
					if (getDirtyContainer() != null)
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(getDirtyContainer()));
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					diffMode = each;
					WebResponse response = (WebResponse) RequestCycle.get().getResponse();
					Cookie cookie = new Cookie(COOKIE_DIFF_MODE, diffMode.name());
					cookie.setMaxAge(Integer.MAX_VALUE);
					response.addCookie(cookie);
					target.add(DiffOptionPanel.this);
					
					target.focusComponent(null);
					onDiffModeChange(target);
				}
				
			}.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					return each==diffMode?" active":"";
				}
				
			})));
		}
		
		DropdownPanel filterDropdown = new DropdownPanel("filterDropdown", true) {

			@Override
			protected Component newContent(String id) {
				return new PathSelector(id, repoModel, newRevModel.getObject(), FileMode.TYPE_TREE, 
						FileMode.TYPE_FILE, FileMode.TYPE_GITLINK, FileMode.TYPE_SYMLINK) {
					
					@Override
					protected void onSelect(AjaxRequestTarget target, BlobIdent blobIdent) {
						hide(target);
						target.add(DiffOptionPanel.this);
						onSelectPath(target, blobIdent.path);
					}

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
						if (getDirtyContainer() != null)
							attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(getDirtyContainer()));
					}
					
				};
			}
			
		};
		add(filterDropdown);
		add(new WebMarkupContainer("filter").add(new DropdownBehavior(filterDropdown)));
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		newRevModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(DiffOptionPanel.class, "diff-option.css")));
	}

	protected abstract void onSelectPath(AjaxRequestTarget target, String path);

	protected abstract void onLineProcessorChange(AjaxRequestTarget target);
	
	protected abstract void onDiffModeChange(AjaxRequestTarget target);
	
	public LineProcessor getLineProcessor() {
		return lineProcessor;
	}
	
	public DiffMode getDiffMode() {
		return diffMode;
	}
	
	protected Component getDirtyContainer() {
		return null;
	};
	
}
