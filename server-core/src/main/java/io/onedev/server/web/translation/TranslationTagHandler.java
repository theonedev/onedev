/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.onedev.server.web.translation;

import static io.onedev.server.web.translation.Translation._T;

import java.text.ParseException;
import java.util.HashMap;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.resolver.IComponentResolver;


/**
 * This is a markup inline filter and a component resolver. It identifies wicket:message attributes
 * and adds an attribute modifier to the component tag that can localize
 * wicket:message="attr-name:i18n-key,attr-name-2:i18n-key-2,..." expressions, replacing values of
 * attributes specified by attr-name with a localizer lookup with key i18n-key. If an attribute
 * being localized has a set value that value will be used as the default value for the localization
 * lookup. This handler also resolves and localizes raw markup with wicket:message attribute.
 * 
 * @author Juergen Donnerstag
 * @author Igor Vaynberg
 */
public final class TranslationTagHandler extends AbstractMarkupFilter
	implements
		IComponentResolver
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * The id automatically assigned to tags with wicket:message attribute but without id
	 */
	public final static String WICKET_MESSAGE_CONTAINER_ID = "_t_attr_";

	/**
	 * Constructor for the IComponentResolver role.
	 */
	public TranslationTagHandler()
	{
		this(null);
	}

	/**
	 * Constructor for the IMarkupFilter role.
	 */
	public TranslationTagHandler(final MarkupResourceStream markupResourceStream)
	{
		super(markupResourceStream);
	}

	@Override
	protected final MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		if (tag.isClose())
		{
			return tag;
		}

        boolean hasTranslationAttribute = false;
        for (var attribute : tag.getAttributes().keySet()) {
            if (attribute.startsWith("t:")) {
                hasTranslationAttribute = true;
                break;
            }
        }
		if (hasTranslationAttribute) {
			// check if this tag is raw markup
			if (tag.getId() == null)
			{
				// if this is a raw tag we need to set the id to something so
				// that wicket will not merge this as raw markup and instead
				// pass it on to a resolver
				tag.setId(getWicketMessageIdPrefix(null) + getRequestUniqueId());
				tag.setAutoComponentTag(true);
				tag.setModified(true);
			}
			tag.addBehavior(new AttributeLocalizer());
		}

		return tag;
	}

	public static class AttributeLocalizer extends Behavior
	{
		private static final long serialVersionUID = 1L;
		
		@Override
		public void onComponentTag(final Component component, final ComponentTag tag) {
            var translatedAttributes = new HashMap<String, String>();
            for (var entry : tag.getAttributes().entrySet()) {
                if (entry.getKey().startsWith("t:")) {
                    var key = entry.getKey().substring(2);
                    var value = entry.getValue();
                    translatedAttributes.put(key, _T(value.toString()));
                }   
            }
            for (var entry : translatedAttributes.entrySet()) {
                tag.remove("t:" + entry.getKey());
                tag.put(entry.getKey(), entry.getValue());
            }
		}
	}

	@Override
	public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		// localize any raw markup that has wicket:message attrs
		if ((tag != null) && (tag.getId().startsWith(getWicketMessageIdPrefix(markupStream))))
		{
			Component wc;
			String id = tag.getId();

			if (tag.isOpenClose())
			{
				wc = new WebComponent(id);
			}
			else
			{
				wc = new TransparentWebMarkupContainer(id);
			}

			return wc;
		}
		return null;
	}
	
	private String getWicketMessageIdPrefix(final MarkupStream markupStream)
	{
		return getWicketNamespace(markupStream) + WICKET_MESSAGE_CONTAINER_ID;
	}
}
