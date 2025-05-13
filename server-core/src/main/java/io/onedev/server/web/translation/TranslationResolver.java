package io.onedev.server.web.translation;

import static io.onedev.server.web.translation.Translation._T;
import static java.lang.Character.isWhitespace;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.markup.resolver.IComponentResolver;

import com.google.common.base.Preconditions;

public class TranslationResolver implements IComponentResolver {

	private static final long serialVersionUID = 1L;

	private static final String TAG_NAME = "t";
		
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
					var html = markup.toString(true);
					var index1 = html.indexOf('>');
					var index2 = html.lastIndexOf('<');
					Preconditions.checkState(index1 < index2);
					var innerHtml = html.substring(index1+1, index2);
					
					var normalizedInnerHtml = innerHtml.trim().replaceAll("\\s+", " ");
					if (normalizedInnerHtml.length() != 0) {
						var index11 = 0;
						while (index11 < innerHtml.length() && isWhitespace(innerHtml.charAt(index11))) {
							index11++;
						}
						var index22 = innerHtml.length() - 1;
						while (index22 >= 0 && isWhitespace(innerHtml.charAt(index22))) {
							index22--;
						}
						var prefix = html.substring(0, index1 + 1 + index11);
						var suffix = html.substring(index1 + 2 + index22, html.length());
						return Markup.of(prefix + _T(normalizedInnerHtml) + suffix);
					} else {
						return markup;
					}
				}
				
			};
			container.autoAdd(component, markupStream);			
			return component;
		} else {
			return null;
		}
	}
} 