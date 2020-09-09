package io.onedev.server.web.component.entity.nav;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.protocol.http.WebSession;

import io.onedev.commons.utils.WordUtils;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;

@SuppressWarnings("serial")
public abstract class EntityNavPanel<T extends AbstractEntity> extends Panel {

	private final String entityName;
	
	public EntityNavPanel(String id) {
		super(id);
		
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(EntityNavPanel.class, getClass());
		entityName = WordUtils.uncamel(typeArguments.get(0).getSimpleName()).toLowerCase();
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
		add(new AjaxLink<Void>("next") {

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
				tag.put("title", "Next " + entityName);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				EntityQuery<T> query = parse(getCursor().getQuery(), getCursor().isInProject());
				int count = getCursor().getCount();
				int offset = getCursor().getOffset() - 1;
				List<T> entities = query(query, offset, 1, getCursor().isInProject());
				if (!entities.isEmpty()) {
					if (!query.matches(getEntity()))
						count--;
					Cursor prevCursor = new Cursor(getCursor().getQuery(), count, offset, getCursor().isInProject());
					getCursorSupport().navTo(target, entities.get(0), prevCursor);
				} else {
					WebSession.get().warn("No more " + entityName + "s");
				}
			}
			
		});
		add(new AjaxLink<Void>("prev") {

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
				tag.put("title", "Previous " + entityName);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				EntityQuery<T> query = parse(getCursor().getQuery(), getCursor().isInProject());
				int offset = getCursor().getOffset();
				int count = getCursor().getCount();
				if (query.matches(getEntity())) 
					offset++;
				else
					count--;
				
				List<T> entities = query(query, offset, 1, getCursor().isInProject());
				if (!entities.isEmpty()) {
					Cursor nextCursor = new Cursor(getCursor().getQuery(), count, offset, getCursor().isInProject());
					getCursorSupport().navTo(target, entities.get(0), nextCursor);
				} else {
					WebSession.get().warn("No more " + entityName + "s");
				}
			}
			
		});
		
		add(new Label("current", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getCursor() != null)
					return entityName + " " + (getCursor().getCount()-getCursor().getOffset()) + " of " + getCursor().getCount();				
				else
					return entityName + " 1 of 1";
			}
			
		}));
	}

	protected abstract EntityQuery<T> parse(String queryString, boolean inProject);
	
	protected abstract T getEntity();
	
	@Nullable
	protected abstract CursorSupport<T> getCursorSupport();
	
	protected abstract List<T> query(EntityQuery<T> query, int offset, int count, boolean inProject);
	
}
