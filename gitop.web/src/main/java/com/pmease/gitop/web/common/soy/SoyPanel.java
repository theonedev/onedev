package com.pmease.gitop.web.common.soy;

import java.util.Map;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

import com.google.common.collect.Maps;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.web.common.soy.api.SoyTemplateRenderer;
import com.pmease.gitop.web.common.soy.api.TemplateKey;

public class SoyPanel extends Panel implements IMarkupResourceStreamProvider,
		IMarkupCacheKeyProvider {

	private static final long serialVersionUID = 1L;

	private final TemplateKey templateKey;

	public SoyPanel(String id, TemplateKey key) {
		this(id, key, null);
	}

	public SoyPanel(String id, TemplateKey key, IModel<Map<String, Object>> data) {
		super(id, data);
		this.templateKey = key;
	}

	@Override
	public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass) {
//		Stopwatch watch = new Stopwatch().start();
		StringBuffer sb = new StringBuffer();
		sb.append("<wicket:panel>");
		sb.append(evaluateSoyTemplate());
		sb.append("</wicket:panel>");
//		System.out.println("Duration " + watch.elapsedMillis());
		return new StringResourceStream(sb.toString());
	}

	@SuppressWarnings("unchecked")
	protected Map<String, ?> getData() {
		if (getDefaultModel() == null) {
			return Maps.newHashMap();
		}

		return (Map<String, ?>) getDefaultModelObject();
	}

	protected Map<String, ?> getIjData() {
		return Maps.newHashMap();
	}

	protected String evaluateSoyTemplate() {
		SoyTemplateRenderer renderer = Gitop.getInstance(SoyTemplateRenderer.class);
		StringBuffer sb = new StringBuffer();
		renderer.render(templateKey, sb, getData(), getIjData());
		return sb.toString();
	}

	@Override
	public String getCacheKey(MarkupContainer container, Class<?> containerClass) {
		return null;
	}
}