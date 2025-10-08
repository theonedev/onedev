package io.onedev.server.web.component.entity.nav;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.protocol.http.WebSession;

import io.onedev.commons.utils.WordUtils;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;

public abstract class EntityNavPanel<T extends AbstractEntity> extends Panel {

	private final String entityName;
	
	public EntityNavPanel(String id, @Nullable String entityName) {
		super(id);
		
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(EntityNavPanel.class, getClass());
		if (entityName != null)
			this.entityName = entityName;
		else
			this.entityName = WordUtils.uncamel(typeArguments.get(0).getSimpleName()).toLowerCase();
	}

	public EntityNavPanel(String id) {
		this(id, null);
	}
	
	private Cursor getCursor() {
		if (getCursorSupport() != null)
			return getCursorSupport().getCursor();
		else
			return null;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AjaxLink<Void>("prev") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(getCursor() != null && getCursor().getOffset() > 0);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (getCursor() == null || getCursor().getOffset() <= 0)
					tag.put("disabled", "disabled");
				tag.put("title", MessageFormat.format(_T("Previous {0}"), _T(entityName)));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				ProjectScope projectScope = getCursor().getProjectScope();
				EntityQuery<T> query = parse(getCursor().getQuery(), 
						projectScope!=null?projectScope.getProject():null);
				int count = getCursor().getCount();
				int offset = getCursor().getOffset() - 1;
				List<T> entities = query(query, offset, 1, projectScope);
				if (!entities.isEmpty()) {
					if (!query.matches(getEntity()))
						count--;
					Cursor prevCursor = new Cursor(getCursor().getQuery(), count, offset, projectScope);
					getCursorSupport().navTo(target, entities.get(0), prevCursor);
				} else {
					WebSession.get().warn(_T("No more " + entityName + "s"));
				}
			}
			
		});
		add(new AjaxLink<Void>("next") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(getCursor() != null && getCursor().getOffset()<getCursor().getCount()-1);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (getCursor() == null || getCursor().getOffset() >= getCursor().getCount()-1)
					tag.put("disabled", "disabled");
				tag.put("title", MessageFormat.format(_T("Next {0}"), _T(entityName)));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				ProjectScope projectScope = getCursor().getProjectScope();
				EntityQuery<T> query = parse(getCursor().getQuery(), projectScope!=null?projectScope.getProject():null);
				int offset = getCursor().getOffset();
				int count = getCursor().getCount();
				if (query.matches(getEntity())) 
					offset++;
				else
					count--;
				
				List<T> entities = query(query, offset, 1, projectScope);
				if (!entities.isEmpty()) {
					Cursor nextCursor = new Cursor(getCursor().getQuery(), count, offset, projectScope);
					getCursorSupport().navTo(target, entities.get(0), nextCursor);
				} else {
					WebSession.get().warn(_T("No more " + entityName + "s"));
				}
			}
			
		});
		
		add(new Label("current", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getCursor() != null)
					return _T(entityName) + " " + (getCursor().getOffset() + 1) + "/" + getCursor().getCount();				
				else
					return _T(entityName) + " 1/1";
			}
			
		}));
	}

	protected abstract EntityQuery<T> parse(String queryString, @Nullable Project project);
	
	protected abstract T getEntity();
	
	@Nullable
	protected abstract CursorSupport<T> getCursorSupport();
	
	protected abstract List<T> query(EntityQuery<T> query, int offset, int count, @Nullable ProjectScope projectScope);
	
}
