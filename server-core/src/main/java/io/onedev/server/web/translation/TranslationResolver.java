package io.onedev.server.web.translation;

import static io.onedev.server.web.translation.Translation._T;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;

import com.google.common.base.Preconditions;

public class TranslationResolver implements IComponentResolver {

	private static final long serialVersionUID = 1L;

	private static final String TAG_NAME = "t";

	private static final MetaDataKey<Map<String, IMarkupFragment>> MARKUP_CACHE = 
			new MetaDataKey<Map<String, IMarkupFragment>>() {};
		
	static {
	    WicketTagIdentifier.registerWellKnownTagName(TAG_NAME);
	}
	
	@Override
	public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag) {
		if (tag instanceof WicketTag && TAG_NAME.equalsIgnoreCase(tag.getName())) {
			String id = "_" + TAG_NAME + "_" + container.getPage().getAutoIndex();
			var component = new TransparentWebMarkupContainer(id) {

				@Override
				protected void onComponentTag(final ComponentTag tag) {
					super.onComponentTag(tag);
				}
		
				@Override
				public IMarkupFragment getMarkup() {
					var markup = super.getMarkup();
					var markupCache = RequestCycle.get().getMetaData(MARKUP_CACHE);
					if (markupCache == null) {
						markupCache = new HashMap<>();
						RequestCycle.get().setMetaData(MARKUP_CACHE, markupCache);
					}
					return markupCache.computeIfAbsent(markup.toString(true), (k) -> {
						var index1 = k.indexOf('>');
						var index2 = k.lastIndexOf('<');
						Preconditions.checkState(index1 < index2);
						var innerHtml = k.substring(index1+1, index2);
						
						var normalizedInnerHtml = innerHtml.trim().replaceAll("\\s+", " ");
						if (normalizedInnerHtml.length() != 0) {
							var index11 = 0;
							while (index11 < innerHtml.length() && Character.isWhitespace(innerHtml.charAt(index11))) {
								index11++;
							}
							var index22 = innerHtml.length() - 1;
							while (index22 >= 0 && Character.isWhitespace(innerHtml.charAt(index22))) {
								index22--;
							}
							var prefix = k.substring(0, index1 + 1 + index11);
							var suffix = k.substring(index1 + 2 + index22, k.length());
							try {
								var stringResourceStream = new StringResourceStream(prefix + _T(normalizedInnerHtml) + suffix);
								stringResourceStream.setCharset(StandardCharsets.UTF_8);
								MarkupParser markupParser = new MarkupParser(new MarkupResourceStream(stringResourceStream));
								markupParser.setWicketNamespace("wicket");
								return markupParser.parse();
							} catch (IOException | ResourceStreamNotFoundException e) {
								throw new RuntimeException(e);
							}
						} else {
							return markup;
						}
					});
				}
				
			};
			container.autoAdd(component, markupStream);			
			return component;
		} else {
			return null;
		}
	}
} 