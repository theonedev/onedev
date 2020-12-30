package io.onedev.server.web.component.svg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.CompoundRequestMapper;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.mapper.BaseResourceMapper;
import io.onedev.server.web.resource.SvgSpriteResourceReference;

@SuppressWarnings("serial")
public class SpriteImage extends WebComponent {

	private static final Map<String, Class<?>> spriteScopes = new ConcurrentHashMap<>();
	
	public SpriteImage(String id, IModel<String> model) {
		super(id, model);
	}
	
	public SpriteImage(String id, @Nullable String href) {
		super(id, Model.of(href));
	}
	
	public SpriteImage(String id) {
		this(id, (String)null);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEscapeModelStrings(false);
	}
	
	private void listResourceMappers(IRequestMapper rootMapper, List<BaseResourceMapper> resourceMappers) {
		if (rootMapper instanceof CompoundRequestMapper) {
			for (IRequestMapper mapper: (CompoundRequestMapper)rootMapper)
				listResourceMappers(mapper, resourceMappers);
		} else if (rootMapper instanceof BaseResourceMapper) {
			resourceMappers.add((BaseResourceMapper) rootMapper);
		}
	}
	
	public static String getVersionedHref(@Nullable Class<?> scope, @Nullable String name) {
		if (scope == null)
			scope = IconScope.class;
		String baseUrl = RequestCycle.get().urlFor(new SvgSpriteResourceReference(scope), new PageParameters()).toString();
		if (name != null)
			return baseUrl + "#" + name;
		else
			return baseUrl;
	}

	public static String getVersionedHref(@Nullable String name) {
		return getVersionedHref(null, name);
	}
	
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
		String href = getDefaultModelObjectAsString();
		Class<?> scope;
		String symbol;
		int index = href.indexOf('#');
		if (index != -1) {
			symbol = StringUtils.strip(href.substring(index+1), "/");
			String mountPath = StringUtils.strip(href.substring(0, index), "/").trim();
			if (mountPath.length() != 0) {
				scope = spriteScopes.get(mountPath);
				if (scope == null) {
					List<BaseResourceMapper> resourceMappers = new ArrayList<>();
					listResourceMappers(getApplication().getRootRequestMapper(), resourceMappers);
					for (BaseResourceMapper mapper: resourceMappers) {
						BaseResourceMapper baseMapper = (BaseResourceMapper) mapper;
						if (StringUtils.strip(baseMapper.getPath(), "/").equalsIgnoreCase(mountPath)) {
							if (baseMapper.getResourceReference() instanceof SvgSpriteResourceReference) {
								scope = ((SvgSpriteResourceReference) baseMapper.getResourceReference()).getScope();
							} else {
								throw new ExplicitException("Path '" + mountPath 
										+ "' should be mounted to a svg sprite resource reference");
							}
						}
					}
					
					if (scope == null)
						throw new ExplicitException("Unable to find svg sprite resource mounted at: " + mountPath);
					
					spriteScopes.put(mountPath, scope);
				}
			} else {
				scope = IconScope.class;
			}
		} else {
			scope = IconScope.class;
			symbol = StringUtils.strip(href, "/");
		}
		
		String spriteUrl = urlFor(new SvgSpriteResourceReference(scope), new PageParameters()).toString();
		replaceComponentTagBody(markupStream, openTag, 
				"<use xlink:href='" + spriteUrl + "#" + symbol + "'></use>");
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);

		if (tag.isOpenClose())
			tag.setType(TagType.OPEN);
	}
	
}
