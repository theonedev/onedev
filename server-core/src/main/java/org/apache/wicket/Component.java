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
package org.apache.wicket;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.IAjaxRegionMarkupIdProvider;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.UnauthorizedActionException;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.core.util.string.ComponentStrings;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.event.IEventSink;
import org.apache.wicket.event.IEventSource;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupCache;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IFormSubmitListener;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.DefaultMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IComponentInheritedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IModelComparator;
import org.apache.wicket.model.IWrapModel;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.settings.DebugSettings;
import org.apache.wicket.settings.ExceptionSettings;
import org.apache.wicket.util.IHierarchical;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.PrependingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitFilter;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.util.visit.Visit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.util.ComponentContext;


/**
 * Component serves as the highest level abstract base class for all components.
 * 
 * <ul>
 * <li><b>Identity </b>- All Components must have a non-null id which is retrieved by calling
 * getId(). The id must be unique within the {@link MarkupContainer} that holds the Component, but
 * does not have to be globally unique or unique within a Page's component hierarchy.</li>
 * <li><b>Hierarchy </b>- A component has a parent which can be retrieved with {@link #getParent()}.
 * If a component is an instance of MarkupContainer, it may have children. In this way it has a
 * place in the hierarchy of components contained on a given page.
 * <p>
 * The path from the Page at the root of the component hierarchy to a given Component is simply the
 * concatenation with colon separators of each id along the way. For example, the path "a:b:c" would
 * refer to the component named "c" inside the MarkupContainer named "b" inside the container named
 * "a". The path to a component can be retrieved by calling {@link #getPath()}. To get a Component
 * path relative to the page that contains it, you can call {@link #getPageRelativePath()}.</li>
 * <li><b>LifeCycle </b>- Components participate in the following lifecycle phases:
 * <ul>
 * <li><b>Construction </b>- A Component is constructed with the Java language new operator.
 * Children may be added during construction if the Component is a MarkupContainer.
 * {@link IComponentInstantiationListener}s are notified of component instantiation.
 * <p>
 * {@link #onInitialize()} is called on the component as soon as the component is part of a page's
 * component tree. At this state the component is able to access its markup.</li>
 * <li><b>Request Handling </b>- An incoming request is processed by a protocol request handler such
 * as {@link WicketFilter}. An associated Application object creates {@link Session},
 * {@link Request} and {@link Response} objects for use by a given Component in updating its model
 * and rendering a response. These objects are stored inside a container called {@link RequestCycle}
 * which is accessible via {@link Component#getRequestCycle()}. The convenience methods
 * {@link Component#getRequest()}, {@link Component#getResponse()} and
 * {@link Component#getSession()} provide easy access to the contents of this container.</li>
 * <li><b>Listener Invocation </b>- If the request references an {@link IRequestListener} on an
 * existing Component (or one of its {@link Behavior}s, see below), that listener is notified,
 * allowing arbitrary user code to handle events such as link clicks or form submits. Although
 * arbitrary listeners are supported in Wicket, the need to implement a new class of listener is
 * unlikely for a web application and even the need to implement a listener interface directly is
 * highly discouraged. Instead, calls to listeners are routed through logic specific to the event,
 * resulting in calls to user code through other overridable methods. See {@link Form} for an
 * example of a component which listens for events via {@link IFormSubmitListener}.</li>
 * <li><b>Rendering </b>- Before a page or part of a page (in case of Ajax updates) is rendered, all
 * containing components are able to prepare for rendering via two hook methods:
 * {@link #onConfigure()} (regardless whether they are visible or not) and {@link #onBeforeRender()}
 * (if visible only) . <br>
 * A markup response is generated by the Component via {@link Component#render()}, which calls
 * subclass implementation code contained in {@link Component#onRender()}. Once this phase begins, a
 * Component becomes immutable. Attempts to alter the Component will result in a
 * WicketRuntimeException.</li>
 * <li><b>Detachment </b>- Each request cycle finishes by detaching all touched components.
 * Subclasses should clean up their state by overriding {@link #onDetach()} or more specifically
 * {@link #detachModels()} if they keep references to models beside the default model.</li>
 * </ul>
 * </li>
 * <li><b>Visibility </b>- If a component is not visible (see {@link #setVisible(boolean)}) it will
 * not render a response (nor will their children).</li>
 * <li><b>Enabling </b>- Component subclasses take into account their enabled state (see
 * {@link #setEnabled(boolean)} when rendering, and in case of a {@link FormComponent} will not not
 * update its model while the request is handled.</li>
 * <li><b>Models </b>- The primary responsibility of a component is to use its model (an object that
 * implements {@link IModel}) to render a response in an appropriate markup language, such as HTML.
 * In addition, {@link FormComponent}s know how to update their models based on request information,
 * see {@link FormComponent#updateModel()}. Since the IModel interface is a wrapper around another
 * object, a convenience method {@link Component#getDefaultModelObject()} is provided to retrieve
 * the object from its IModel wrapper. A further convenience method,
 * {@link Component#getDefaultModelObjectAsString()}, is provided for the very common operation of
 * converting the wrapped object to a String. <br>
 * The component's model can be passed in the constructor or set via
 * {@link Component#setDefaultModel(IModel)}. In neither case a model can be created on demand with
 * {@link #initModel()}.<br>
 * Note that a component can have more models besides its default model.</li>
 * <li><b>Behaviors </b>- You can add multiple {@link Behavior}s to any component if you need to
 * dynamically alter the behavior of components, e.g. manipulate attributes of the markup tag to
 * which a Component is attached. Behaviors take part in the component's lifecycle through various
 * callback methods.</li>
 * <li><b>Locale </b>- The Locale for a Component is available through {@link #getLocale()}, which
 * delegates to its parent's locale, finally consulting the {@link Session}'s locale.</li>
 * <li><b>Style </b>- The Session's style ("skin") is available through
 * {@link org.apache.wicket.Component#getStyle()}. Styles are intended to give a particular look to
 * all components or resources in a session that is independent of its Locale. For example, a style
 * might be a set of resources, including images and markup files, which gives the design look of
 * "ocean" to the user. If the Session's style is set to "ocean" and these resources are given names
 * suffixed with "_ocean", Wicket's resource management logic will prefer these resources to other
 * resources, such as default resources, which are not as good of a match.</li>
 * <li><b>Variation </b>- Whereas styles are Session (user) specific, variations are component
 * specific. E.g. if the Style is "ocean" and {@link #getVariation()} returnss "NorthSea", than the
 * resources are given the names suffixed with "_ocean_NorthSea".</li>
 * <li><b>String Resources </b>- Components can have associated String resources via the
 * Application's Localizer, which is available through the method {@link Component#getLocalizer()}.
 * The convenience methods {@link Component#getString(String key)} and
 * {@link Component#getString(String key, IModel model)} wrap the identical methods on the
 * Application Localizer for easy access in Components.</li>
 * <li><b>Feedback Messages </b>- The {@link Component#debug(Serializable)},
 * {@link Component#info(Serializable)}, {@link Component#warn(Serializable)},
 * {@link Component#error(java.io.Serializable)} and {@link Component#fatal(Serializable)} methods
 * associate feedback messages with a Component. It is generally not necessary to use these methods
 * directly since Wicket validators automatically register feedback messages on Components. Feedback
 * message for a given Component can be retrieved with {@link Component#getFeedbackMessages}.</li>
 * <li><b>Versioning </b>- Pages are the unit of versioning in Wicket, but fine-grained control of
 * which Components should participate in versioning is possible via the
 * {@link Component#setVersioned(boolean)} method. The versioning participation of a given Component
 * can be retrieved with {@link Component#isVersioned()}.</li>
 * <li><b>Page </b>- The Page containing any given Component can be retrieved by calling
 * {@link Component#getPage()}. If the Component is not attached to a Page, an IllegalStateException
 * will be thrown. An equivalent method, {@link Component#findPage()} is available for special
 * circumstances where it might be desirable to get a null reference back instead.</li>
 * <li><b>Application </b>- The {@link #getApplication()} method provides convenient access to the
 * {@link Application} for a Component.</li>
 * <li><b>AJAX support</b>- Components can be re-rendered after the whole Page has been rendered at
 * least once by calling doRender().</li>
 * <li><b>Security </b>- All components are subject to an {@link IAuthorizationStrategy} which
 * controls instantiation, visibility and enabling. See {@link SimplePageAuthorizationStrategy} for
 * a simple implementation.</li>
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Johan Compagner
 * @author Juergen Donnerstag
 * @author Igor Vaynberg (ivaynberg)
 */
@SuppressWarnings("unused")
public abstract class Component
	implements
		IClusterable,
		IConverterLocator,
		IRequestableComponent,
		IHeaderContributor,
		IHierarchical<Component>,
		IEventSink,
		IEventSource
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(Component.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Action used with IAuthorizationStrategy to determine whether a component is allowed to be
	 * enabled.
	 * <p>
	 * If enabling is authorized, a component may decide by itself (typically using it's enabled
	 * property) whether it is enabled or not. If enabling is not authorized, the given component is
	 * marked disabled, regardless its enabled property.
	 * <p>
	 * When a component is not allowed to be enabled (in effect disabled through the implementation
	 * of this interface), Wicket will try to prevent model updates too. This is not completely fail
	 * safe, as constructs like:
	 * 
	 * <pre>
	 * 
	 * User u = (User)getModelObject();
	 * u.setName(&quot;got you there!&quot;);
	 * 
	 * </pre>
	 * 
	 * can't be prevented. Indeed it can be argued that any model protection is best dealt with in
	 * your model objects to be completely secured. Wicket will catch all normal framework-directed
	 * use though.
	 */
	public static final Action ENABLE = new Action(Action.ENABLE);

	/** Separator for component paths */
	public static final char PATH_SEPARATOR = ':';
	/** Path segment that represents this component's parent */
	public static final String PARENT_PATH = "..";

	/**
	 * Action used with IAuthorizationStrategy to determine whether a component and its children are
	 * allowed to be rendered.
	 * <p>
	 * There are two uses for this method:
	 * <ul>
	 * <li>The 'normal' use is for controlling whether a component is rendered without having any
	 * effect on the rest of the processing. If a strategy lets this method return 'false', then the
	 * target component and its children will not be rendered, in the same fashion as if that
	 * component had visibility property 'false'.</li>
	 * <li>The other use is when a component should block the rendering of the whole page. So
	 * instead of 'hiding' a component, what we generally want to achieve here is that we force the
	 * user to logon/give-credentials for a higher level of authorization. For this functionality,
	 * the strategy implementation should throw a {@link AuthorizationException}, which will then be
	 * handled further by the framework.</li>
	 * </ul>
	 * </p>
	 */
	public static final Action RENDER = new Action(Action.RENDER);

	/** meta data for user specified markup id */
	private static final MetaDataKey<String> MARKUP_ID_KEY = new MetaDataKey<String>()
	{
		private static final long serialVersionUID = 1L;
	};

	/** meta data for user specified markup id */
	private static final MetaDataKey<FeedbackMessages> FEEDBACK_KEY = new MetaDataKey<FeedbackMessages>()
	{
		private static final long serialVersionUID = 1L;
	};


	/** Basic model IModelComparator implementation for normal object models */
	private static final IModelComparator defaultModelComparator = new IModelComparator()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean compare(Component component, Object b)
		{
			final Object a = component.getDefaultModelObject();
			if (a == null && b == null)
			{
				return true;
			}
			if (a == null || b == null)
			{
				return false;
			}
			return a.equals(b);
		}
	};

	/** an unused flag */
	private static final int FLAG_UNUSED0 = 0x20000000;
	private static final int FLAG_UNUSED1 = 0x800000;
	private static final int FLAG_UNUSED2 = 0x1000000;
	private static final int FLAG_UNUSED3 = 0x10000000;

	/** True when a component is being auto-added */
	private static final int FLAG_AUTO = 0x0001;

	/** Flag for escaping HTML in model strings */
	private static final int FLAG_ESCAPE_MODEL_STRINGS = 0x0002;

	/** Boolean whether this component's model is inheritable. */
	static final int FLAG_INHERITABLE_MODEL = 0x0004;

	/** Versioning boolean */
	private static final int FLAG_VERSIONED = 0x0008;

	/** Visibility boolean */
	private static final int FLAG_VISIBLE = 0x0010;

	/** Render tag boolean */
	private static final int FLAG_RENDER_BODY_ONLY = 0x0020;

	/** Ignore attribute modifiers */
	private static final int FLAG_IGNORE_ATTRIBUTE_MODIFIER = 0x0040;

	/** True when a component is enabled for model updates and is reachable. */
	private static final int FLAG_ENABLED = 0x0080;

	/** Reserved subclass-definable flag bit */
	protected static final int FLAG_RESERVED1 = 0x0100;
	/** Reserved subclass-definable flag bit */
	protected static final int FLAG_RESERVED2 = 0x0200;
	/** Reserved subclass-definable flag bit */
	protected static final int FLAG_RESERVED3 = 0x0400;
	/** Reserved subclass-definable flag bit */
	protected static final int FLAG_RESERVED4 = 0x0800;

	/** Boolean whether this component was rendered at least once for tracking changes. */
	private static final int FLAG_HAS_BEEN_RENDERED = 0x1000;

	/**
	 * Internal indicator of whether this component may be rendered given the current context's
	 * authorization. It overrides the visible flag in case this is false. Authorization is done
	 * before trying to render any component (otherwise we would end up with a half rendered page in
	 * the buffer)
	 */
	private static final int FLAG_IS_RENDER_ALLOWED = 0x2000;

	/** Whether or not the component should print out its markup id into the id attribute */
	private static final int FLAG_OUTPUT_MARKUP_ID = 0x4000;

	/**
	 * Output a placeholder tag if the component is not visible. This is useful in ajax mode to go
	 * to visible(false) to visible(true) without the overhead of repainting a visible parent
	 * container
	 */
	private static final int FLAG_PLACEHOLDER = 0x8000;

	/** Reserved subclass-definable flag bit */
	protected static final int FLAG_RESERVED5 = 0x10000;
	/** onInitialize called */
	protected static final int FLAG_INITIALIZED = 0x20000;
	/** Set when a component is removed from the hierarchy */
	private static final int FLAG_REMOVED = 0x40000;
	/** Reserved subclass-definable flag bit */
	protected static final int FLAG_RESERVED8 = 0x80000;

	/**
	 * Flag that determines whether the model is set. This is necessary because of the way we
	 * represent component state ({@link #data}). We can't distinguish between model and behavior
	 * using instanceof, because one object can implement both interfaces. Thus we need this flag -
	 * when the flag is set, first object in {@link #data} is always model.
	 */
	private static final int FLAG_MODEL_SET = 0x100000;

	/** True when a component is being removed from the hierarchy */
	protected static final int FLAG_REMOVING_FROM_HIERARCHY = 0x200000;

	/**
	 * Flag that makes we are in before-render callback phase Set after component.onBeforeRender is
	 * invoked (right before invoking beforeRender on children)
	 */
	protected static final int FLAG_RENDERING = 0x2000000;
	protected static final int FLAG_PREPARED_FOR_RENDER = 0x4000000;
	protected static final int FLAG_AFTER_RENDERING = 0x8000000;

	/**
	 * Flag that restricts visibility of a component when set to true. This is usually used when a
	 * component wants to restrict visibility of another component. Calling
	 * {@link #setVisible(boolean)} on a component does not always have the desired effect because
	 * isVisible() can be overwritten thus this flag offers an alternative that should always work.
	 */
	private static final int FLAG_VISIBILITY_ALLOWED = 0x40000000;

	private static final int FLAG_DETACHING = 0x80000000;
	
	/**
	 * The name of attribute that will hold markup id
	 */
	private static final String MARKUP_ID_ATTR_NAME = "id";

	/**
	 * Meta data key for line precise error logging for the moment of addition. Made package private
	 * for access in {@link MarkupContainer} and {@link Page}
	 */
	static final MetaDataKey<String> ADDED_AT_KEY = new MetaDataKey<String>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * meta data key for line precise error logging for the moment of construction. Made package
	 * private for access in {@link Page}
	 */
	static final MetaDataKey<String> CONSTRUCTED_AT_KEY = new MetaDataKey<String>()
	{
		private static final long serialVersionUID = 1L;
	};

	/** Component flags. See FLAG_* for possible non-exclusive flag values. */
	private int flags = FLAG_VISIBLE | FLAG_ESCAPE_MODEL_STRINGS | FLAG_VERSIONED | FLAG_ENABLED |
		FLAG_IS_RENDER_ALLOWED | FLAG_VISIBILITY_ALLOWED | FLAG_RESERVED5 /* page's stateless hint */;

	private static final short RFLAG_ENABLED_IN_HIERARCHY_VALUE = 0x1;
	private static final short RFLAG_ENABLED_IN_HIERARCHY_SET = 0x2;
	private static final short RFLAG_VISIBLE_IN_HIEARARCHY_VALUE = 0x4;
	private static final short RFLAG_VISIBLE_IN_HIERARCHY_SET = 0x8;
	/** onconfigure has been called */
	private static final short RFLAG_CONFIGURED = 0x10;
	private static final short RFLAG_BEFORE_RENDER_SUPER_CALL_VERIFIED = 0x20;
	private static final short RFLAG_INITIALIZE_SUPER_CALL_VERIFIED = 0x40;
	protected static final short RFLAG_CONTAINER_DEQUEING = 0x80;
	private static final short RFLAG_ON_RE_ADD_SUPER_CALL_VERIFIED = 0x100;

	/**
	 * Flags that only keep their value during the request. Useful for cache markers, etc. At the
	 * end of the request the value of this variable is reset to 0
	 */
	private transient short requestFlags = 0;

	/** Component id. */
	private String id;

	/** Any parent container. */
	private MarkupContainer parent;

	/**
	 * Instead of remembering the whole markupId, we just remember the number for this component so
	 * we can "reconstruct" the markupId on demand. While this could be part of {@link #data},
	 * profiling showed that having it as separate property consumes less memory.
	 */
	int generatedMarkupId = -1;

	/** Must only be used by auto components */
	private transient IMarkupFragment markup;

	/**
	 * Will be re-created instead of persisted when session is replicated. Markup sourcing strategy
	 * are typically stateless (but don't have to).
	 */
	private transient IMarkupSourcingStrategy markupSourcingStrategy;

	/**
	 * The object that holds the component state.
	 * <p>
	 * What's stored here depends on what attributes are set on component. Data can contains
	 * combination of following attributes:
	 * <ul>
	 * <li>Model (indicated by {@link #FLAG_MODEL_SET})
	 * <li>MetaDataEntry (optionally {@link MetaDataEntry}[] if more metadata entries are present) *
	 * <li>{@link Behavior}(s) added to component. The behaviors are not stored in separate array,
	 * they are part of the {@link #data} array (this is in order to save the space of the pointer
	 * to an empty array as most components have no behaviours). 
	 * </ul>
	 * If there is only one attribute set (i.e. model or MetaDataEntry([]) or one behavior), the
	 * #data object points directly to value of that attribute. Otherwise the data is of type
	 * Object[] where the attributes are ordered as specified above.
	 * <p>
	 */
	Object data = null;

	final int data_start()
	{
		return getFlag(FLAG_MODEL_SET) ? 1 : 0;
	}

	final int data_length()
	{
		if (data == null)
		{
			return 0;
		}
		else if (data instanceof Object[] && !(data instanceof MetaDataEntry<?>[]))
		{
			return ((Object[])data).length;
		}
		else
		{
			return 1;
		}
	}

	final Object data_get(int index)
	{
		if (data == null)
		{
			return null;
		}
		else if (data instanceof Object[] && !(data instanceof MetaDataEntry<?>[]))
		{
			Object[] array = (Object[])data;
			return index < array.length ? array[index] : null;
		}
		else if (index == 0)
		{
			return data;
		}
		else
		{
			return null;
		}
	}

	final void data_set(int index, Object object)
	{
		if (index > data_length() - 1)
		{
			throw new IndexOutOfBoundsException("can not set data at " + index +
				" when data_length() is " + data_length());
		}
		else if (index == 0 && !(data instanceof Object[] && !(data instanceof MetaDataEntry<?>[])))
		{
			data = object;
		}
		else
		{
			Object[] array = (Object[])data;
			array[index] = object;
		}
	}

	final void data_add(Object object)
	{
		data_insert(-1, object);
	}

	final void data_insert(int position, Object object)
	{
		int currentLength = data_length();
		if (position == -1)
		{
			position = currentLength;
		}
		if (position > currentLength)
		{
			throw new IndexOutOfBoundsException("can not insert data at " + position +
				" when data_length() is " + currentLength);
		}
		if (currentLength == 0)
		{
			data = object;
		}
		else if (currentLength == 1)
		{
			Object[] array = new Object[2];
			if (position == 0)
			{
				array[0] = object;
				array[1] = data;
			}
			else
			{
				array[0] = data;
				array[1] = object;
			}
			data = array;
		}
		else
		{
			Object[] array = new Object[currentLength + 1];
			Object[] current = (Object[])data;
			int after = currentLength - position;
			if (position > 0)
			{
				System.arraycopy(current, 0, array, 0, position);
			}
			array[position] = object;
			if (after > 0)
			{
				System.arraycopy(current, position, array, position + 1, after);
			}
			data = array;
		}
	}

	final void data_remove(int position)
	{
		int currentLength = data_length();

		if (position > currentLength - 1)
		{
			throw new IndexOutOfBoundsException();
		}
		else if (currentLength == 1)
		{
			data = null;
		}
		else if (currentLength == 2)
		{
			Object[] current = (Object[])data;
			if (position == 0)
			{
				data = current[1];
			}
			else
			{
				data = current[0];
			}
		}
		else
		{
			Object[] current = (Object[])data;
			data = new Object[currentLength - 1];

			if (position > 0)
			{
				System.arraycopy(current, 0, data, 0, position);
			}
			if (position != currentLength - 1)
			{
				final int left = currentLength - position - 1;
				System.arraycopy(current, position + 1, data, position, left);
			}
		}
	}

	/**
	 * Constructor. All components have names. A component's id cannot be null. This is the minimal
	 * constructor of component. It does not register a model.
	 * 
	 * @param id
	 *            The non-null id of this component
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null id.
	 */
	public Component(final String id)
	{
		this(id, null);
	}

	/**
	 * Constructor. All components have names. A component's id cannot be null. This constructor
	 * includes a model.
	 * 
	 * @param id
	 *            The non-null id of this component
	 * @param model
	 *            The component's model
	 * 
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null id.
	 */
	public Component(final String id, final IModel<?> model)
	{
		setId(id);

		init();

		getApplication().getComponentInstantiationListeners().onInstantiation(this);

		final DebugSettings debugSettings = getApplication().getDebugSettings();
		if (debugSettings.isLinePreciseReportingOnNewComponentEnabled() && debugSettings.getComponentUseCheck())
		{
			setMetaData(CONSTRUCTED_AT_KEY,
				ComponentStrings.toString(this, new MarkupException("constructed")));
		}

		if (model != null)
		{
			setModelImpl(wrap(model));
		}
	}

	/**
	 * Let subclasses initialize this instance, before constructors are executed. <br>
	 * This method is intentionally <b>not</b> declared protected, to limit overriding to classes in
	 * this package.
	 */
	void init()
	{
	}

	/**
	 * Get the Markup associated with the Component. If not subclassed, the parent container is
	 * asked to return the markup of this child component.
	 * <p/>
	 * Components like Panel and Border should return the "calling" markup fragment, e.g.
	 * <code>&lt;span wicket:id="myPanel"&gt;body&lt;/span&gt;</code>. You may use
	 * Panel/Border/Enclosure.getMarkup(null) to return the associated markup file. And
	 * Panel/Border/Enclosure.getMarkup(child) will search the child in the appropriate markup
	 * fragment.
	 * 
	 * @see MarkupContainer#getMarkup(Component)
	 * 
	 * @return The markup fragment
	 */
	public IMarkupFragment getMarkup()
	{
		// Markup already determined or preset?
		if (markup != null)
		{
			return markup;
		}

		// No parent, than check associated markup files
		if (parent == null)
		{
			// Must be a MarkupContainer to have associated markup file
			if (this instanceof MarkupContainer)
			{
				MarkupContainer container = (MarkupContainer)this;
				Markup associatedMarkup = container.getAssociatedMarkup();
				if (associatedMarkup != null)
				{
					markup = associatedMarkup;
					return markup;
				}
			}

			// Don't know how to find the markup
			throw new MarkupNotFoundException(
				"Can not determine Markup. Component is not yet connected to a parent. " +
					toString());
		}

		// Ask the parent for find the markup for me
		markup = parent.getMarkup(this);
		return markup;
	}

	/**
	 * @return The 'id' attribute from the associated markup tag
	 */
	public final String getMarkupIdFromMarkup()
	{
		ComponentTag tag = getMarkupTag();
		if (tag != null)
		{
			String id = tag.getAttribute("id");
			if (Strings.isEmpty(id) == false)
			{
				return id.trim();
			}
		}

		return null;
	}

	/**
	 * Set the markup for the component. Note that the component's markup variable is transient and
	 * thus must only be used for one render cycle. E.g. auto-component are using it. You may also
	 * it if you subclassed getMarkup().
	 * 
	 * @param markup
	 */
	public final Component setMarkup(final IMarkupFragment markup)
	{
		this.markup = markup;
		return this;
	}

	/**
	 * Called once per request on components before they are about to be rendered. This method
	 * should be used to configure such things as visibility and enabled flags.
	 * <p>
	 * Overrides must call {@code super.onConfigure()}, usually before any other code
	 * <p>
	 * NOTE: Component hierarchy should not be modified inside this method, instead it should be
	 * done in {@link #onBeforeRender()}
	 * <p>
	 * NOTE: Why this method is preferrable to directly overriding {@link #isVisible()} and
	 * {@link #isEnabled()}? Because those methods are called multiple times even for processing of
	 * a single request. If they contain expensive logic they can slow down the response time of the
	 * entire page. Further, overriding those methods directly on form components may lead to
	 * inconsistent or unexpected state depending on when those methods are called in the form
	 * processing workflow. It is a better practice to push changes to state rather than pull.
	 * <p>
	 * NOTE: If component's visibility or another property depends on another component you may call
	 * {@code other.configure()} followed by {@code other.isVisible()} as mentioned in
	 * {@link #configure()} javadoc.
	 * <p>
	 * NOTE: Why should {@link #onBeforeRender()} not be used for this? Because if a component's
	 * visibility is controlled inside {@link #onBeforeRender()}, once invisible the component will
	 * never become visible again.
	 */
	protected void onConfigure()
	{
	}

	/**
	 * This method is meant to be used as an alternative to initialize components. Usually the
	 * component's constructor is used for this task, but sometimes a component cannot be
	 * initialized in isolation, it may need to access its parent component or its markup in order
	 * to fully initialize. This method is invoked once per component's lifecycle when a path exists
	 * from this component to the {@link Page} thus providing the component with an atomic callback
	 * when the component's environment is built out.
	 * <p>
	 * Overrides must call super#{@link #onInitialize()}. Usually this should be the first thing an
	 * override does, much like a constructor.
	 * </p>
	 * <p>
	 * Parent containers are guaranteed to be initialized before their children
	 * </p>
	 * 
	 * <p>
	 * It is safe to use {@link #getPage()} in this method
	 * </p>
	 * 
	 * <p>
	 * NOTE:The timing of this call is not precise, the contract is that it is called sometime
	 * before {@link Component#onBeforeRender()}.
	 * </p>
	 * 
	 */
	protected void onInitialize()
	{
		setRequestFlag(RFLAG_INITIALIZE_SUPER_CALL_VERIFIED, true);
	}

	/**
	 * Checks if the component has been initialized - {@link #onInitialize()} has been called
	 * 
	 * @return {@code true} if component has been initialized
	 */
	final boolean isInitialized()
	{
		return getFlag(FLAG_INITIALIZED);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE PUBLIC API, DO NOT CALL IT
	 * 
	 * Used to call {@link #onInitialize()}
	 */
	public void internalInitialize()
	{
		fireInitialize();
	}

	/**
	 * Used to call {@link #onInitialize()}
	 */
	final void fireInitialize()
	{
		ComponentContext.push(new ComponentContext(this));
		try {
			if (!getFlag(FLAG_INITIALIZED))
			{
				setFlag(FLAG_INITIALIZED, true);
				setRequestFlag(RFLAG_INITIALIZE_SUPER_CALL_VERIFIED, false);
				onInitialize();
				if (!getRequestFlag(RFLAG_INITIALIZE_SUPER_CALL_VERIFIED))
				{
					throw new IllegalStateException(Component.class.getName() +
						" has not been properly initialized. Something in the hierarchy of " +
						getClass().getName() +
						" has not called super.onInitialize() in the override of onInitialize() method");
				}
				setRequestFlag(RFLAG_INITIALIZE_SUPER_CALL_VERIFIED, false);
	
				getApplication().getComponentInitializationListeners().onInitialize(this);
			}
			else if (getFlag(FLAG_REMOVED))
			{
				setFlag(FLAG_REMOVED, false);
				setRequestFlag(RFLAG_ON_RE_ADD_SUPER_CALL_VERIFIED, false);
				onReAdd();
				if (!getRequestFlag(RFLAG_ON_RE_ADD_SUPER_CALL_VERIFIED))
				{
					throw new IllegalStateException(Component.class.getName() +
							" has not been properly added. Something in the hierarchy of " +
							getClass().getName() +
							" has not called super.onReAdd() in the override of onReAdd() method");
				}
			}
		} finally {
			ComponentContext.pop();
		}
	}

	/**
	 * Called on every component after the page is rendered. It will call onAfterRender for it self
	 * and its children.
	 */
	public final void afterRender()
	{
		try
		{
			setFlag(FLAG_AFTER_RENDERING, true);

			// always detach children because components can be attached
			// independently of their parents
			onAfterRenderChildren();

			onAfterRender();
			getApplication().getComponentOnAfterRenderListeners().onAfterRender(this);
			if (getFlag(FLAG_AFTER_RENDERING))
			{
				throw new IllegalStateException(Component.class.getName() +
					" has not been properly detached. Something in the hierarchy of " +
					getClass().getName() +
					" has not called super.onAfterRender() in the override of onAfterRender() method");
			}
		}
		finally
		{
			// this flag must always be set to false.
			markRendering(false);
		}
	}

	/**
	 * 
	 */
	private void internalBeforeRender()
	{
		configure();

		if ((determineVisibility()) && !getFlag(FLAG_RENDERING) &&
			!getFlag(FLAG_PREPARED_FOR_RENDER))
		{
			setRequestFlag(RFLAG_BEFORE_RENDER_SUPER_CALL_VERIFIED, false);

			getApplication().getComponentPreOnBeforeRenderListeners().onBeforeRender(this);

			onBeforeRender();
			getApplication().getComponentPostOnBeforeRenderListeners().onBeforeRender(this);

			if (!getRequestFlag(RFLAG_BEFORE_RENDER_SUPER_CALL_VERIFIED))
			{
				throw new IllegalStateException(Component.class.getName() +
					" has not been properly rendered. Something in the hierarchy of " +
					getClass().getName() +
					" has not called super.onBeforeRender() in the override of onBeforeRender() method");
			}
		}
	}

	/**
	 * We need to postpone calling beforeRender() on components that implement {@link IFeedback}, to
	 * be sure that all other component's beforeRender() has been already called, so that IFeedbacks
	 * can collect all feedback messages. This is the key under list of postponed {@link IFeedback}
	 * is stored to request cycle metadata. The List is then iterated over in
	 * {@link #prepareForRender()} after calling {@link #beforeRender()}, to initialize postponed
	 * components.
	 */
	private static final MetaDataKey<List<Component>> FEEDBACK_LIST = new MetaDataKey<List<Component>>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Called for every component when the page is getting to be rendered. it will call
	 * {@link #configure()} and {@link #onBeforeRender()} for this component and all the child
	 * components
	 */
	public final void beforeRender()
	{
		ComponentContext.push(new ComponentContext(this));
		try {
			if (this instanceof IFeedback)
			{
				// this component is a feedback. Feedback must be initialized last, so that
				// they can collect messages from other components
				List<Component> feedbacks = getRequestCycle().getMetaData(FEEDBACK_LIST);
				if (feedbacks == null)
				{
					feedbacks = new ArrayList<Component>();
					getRequestCycle().setMetaData(FEEDBACK_LIST, feedbacks);
				}
	
				if (this instanceof MarkupContainer)
				{
					((MarkupContainer)this).visitChildren(IFeedback.class,
						new IVisitor<Component, Void>()
						{
							@Override
							public void component(Component feedback, IVisit<Void> visit)
							{
								feedback.beforeRender();
	
								// don't need to go deeper,
								// as the feedback will visit its children on its own
								visit.dontGoDeeper();
							}
						});
				}
	
				if (!feedbacks.contains(this))
				{
					feedbacks.add(this);
				}
			}
			else
			{
				internalBeforeRender();
			}
		} finally {
			ComponentContext.pop();
		}
	}

	/**
	 * Triggers {@link #onConfigure()} to be invoked on this component if it has not already during
	 * this request.
	 * <p>
	 * This method should be invoked before any calls to {@link #isVisible()} or
	 * {@link #isEnabled()}. Usually this method will be called by the framework before the
	 * component is rendered and so users should not need to call it; however, in cases where
	 * visibility or enabled or other state of one component depends on the state of another this
	 * method should be manually invoked on the other component by the user. EG to link visiliby of
	 * two markup containers the following should be done:
	 * 
	 * <pre>
	 * final WebMarkupContainer source=new WebMarkupContainer("a") {
	 * 	protected void onConfigure() {
	 *    setVisible(Math.rand()>0.5f);
	 *  }
	 * };
	 * 
	 * WebMarkupContainer linked=new WebMarkupContainer("b") {
	 * 	protected void onConfigure() {
	 * 		source.configure(); // make sure source is configured
	 * 		setVisible(source.isVisible());
	 *  }
	 * }
	 * </pre>
	 * 
	 * </p>
	 */
	public final void configure()
	{
		if (!getRequestFlag(RFLAG_CONFIGURED))
		{
			clearEnabledInHierarchyCache();
			clearVisibleInHierarchyCache();
			onConfigure();
			for (Behavior behavior : getBehaviors())
			{
				if (isBehaviorAccepted(behavior))
				{
					behavior.onConfigure(this);
				}
			}

			// check authorization
			setRenderAllowed();

			internalOnAfterConfigure();

			getApplication().getComponentOnConfigureListeners().onConfigure(this);

			setRequestFlag(RFLAG_CONFIGURED, true);
		}
	}

	/**
	 * 
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * Called after the {@link #onConfigure()}, but before {@link #onBeforeRender()}
	 */
	void internalOnAfterConfigure()
	{
	}

	/**
	 * Redirects to any intercept page previously specified by a call to {@link #redirectToInterceptPage(Page)}.
	 * The redirect is done by throwing an exception. If there is no intercept page no exception
	 * will be thrown and the program flow will continue uninterrupted.
	 * 
	 * Example:
	 * 
	 * <pre>
	 * add(new Link(&quot;login&quot;)
	 * {
	 * 	protected void onClick()
	 * 	{
	 * 		if (authenticate())
	 * 		{
	 * 			continueToOriginalDestination();
	 * 			// if we reach this line there was no intercept page, so go to home page
	 * 			setResponsePage(WelcomePage.class);
	 * 		}
	 * 	}
	 * });
	 * 
	 * @see Component#redirectToInterceptPage(Page)
	 */
	public final void continueToOriginalDestination()
	{
		RestartResponseAtInterceptPageException.continueToOriginalDestination();
	}

	/**
	 * Clears any data about previously intercepted page.
	 */
	public final void clearOriginalDestination()
	{
		RestartResponseAtInterceptPageException.clearOriginalDestination();
	}

	/**
	 * Registers a debug feedback message for this component
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void debug(final Serializable message)
	{
		getFeedbackMessages().debug(this, message);
		addStateChange();
	}

	/**
	 * Signals this Component that it is removed from the Component hierarchy.
	 */
	final void internalOnRemove()
	{
		setFlag(FLAG_REMOVING_FROM_HIERARCHY, true);
		onRemove();
		setFlag(FLAG_REMOVED, true);
		if (getFlag(FLAG_REMOVING_FROM_HIERARCHY))
		{
			throw new IllegalStateException(Component.class.getName() +
				" has not been properly removed from hierachy. Something in the hierarchy of " +
				getClass().getName() +
				" has not called super.onRemove() in the override of onRemove() method");
		}
		new Behaviors(this).onRemove(this);
		removeChildren();
	}

	/**
	 * Detaches the component. This is called at the end of the request for all the pages that are
	 * touched in that request.
	 */
	@Override
	public final void detach()
	{
		try
		{
			setFlag(FLAG_DETACHING, true);
			onDetach();
			if (getFlag(FLAG_DETACHING))
			{
				throw new IllegalStateException(Component.class.getName() +
						" has not been properly detached. Something in the hierarchy of " +
						getClass().getName() +
						" has not called super.onDetach() in the override of onDetach() method");
			}

			// always detach models because they can be attached without the
			// component. eg component has a compoundpropertymodel and one of its
			// children component's getmodelobject is called
			detachModels();

			// detach any behaviors
			new Behaviors(this).detach();
		}
		catch (Exception x)
		{
			throw new WicketRuntimeException("An error occurred while detaching component: " + toString(true), x);
		}

		// always detach children because components can be attached
		// independently of their parents
		detachChildren();

		// reset the model to null when the current model is a IWrapModel and
		// the model that created it/wrapped in it is a IComponentInheritedModel
		// The model will be created next time.
		if (getFlag(FLAG_INHERITABLE_MODEL))
		{
			setModelImpl(null);
			setFlag(FLAG_INHERITABLE_MODEL, false);
		}

		clearEnabledInHierarchyCache();
		clearVisibleInHierarchyCache();

		boolean beforeRenderSuperCallVerified = getRequestFlag(RFLAG_BEFORE_RENDER_SUPER_CALL_VERIFIED);
		boolean initializeSuperCallVerified = getRequestFlag(RFLAG_INITIALIZE_SUPER_CALL_VERIFIED);

		requestFlags = 0;

		// preserve the super_call_verified flags if they were set. WICKET-5417
		if (beforeRenderSuperCallVerified)
		{
			setRequestFlag(RFLAG_BEFORE_RENDER_SUPER_CALL_VERIFIED, true);
		}
		if (initializeSuperCallVerified)
		{
			setRequestFlag(RFLAG_INITIALIZE_SUPER_CALL_VERIFIED, true);
		}

		detachFeedback();

		internalDetach();

		// notify any detach listener
		IDetachListener detachListener = getApplication().getFrameworkSettings()
			.getDetachListener();
		if (detachListener != null)
		{
			detachListener.onDetach(this);
		}
	}

	private void detachFeedback()
	{
		FeedbackMessages feedback = getMetaData(FEEDBACK_KEY);
		if (feedback != null)
		{
			feedback.clear(getApplication().getApplicationSettings()
				.getFeedbackMessageCleanupFilter());

			if (feedback.isEmpty())
			{
				setMetaData(FEEDBACK_KEY, null);
			}
			else
			{
				feedback.detach();
			}
		}
	}

	/**
	 * Removes the cached markup at the end of the request. For the next request it will be get
	 * either from the parent's markup or from {@link MarkupCache}.
	 */
	private void internalDetach()
	{
		markup = null;
	}

	/**
	 * Detaches all models
	 */
	public void detachModels()
	{
		// Detach any detachable model from this component
		detachModel();
	}

	/**
	 * Registers an error feedback message for this component
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void error(final Serializable message)
	{
		getFeedbackMessages().error(this, message);
		addStateChange();
	}

	/**
	 * Registers a fatal feedback message for this component
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void fatal(final Serializable message)
	{
		getFeedbackMessages().fatal(this, message);
		addStateChange();
	}

	/**
	 * Finds the first container parent of this component of the given class.
	 * 
	 * @param <Z>
	 *            type of parent
	 * 
	 * 
	 * @param c
	 *            MarkupContainer class to search for
	 * @return First container parent that is an instance of the given class, or null if none can be
	 *         found
	 */
	public final <Z> Z findParent(final Class<Z> c)
	{
		// Start with immediate parent
		MarkupContainer current = parent;

		// Walk up containment hierarchy
		while (current != null)
		{
			// Is current an instance of this class?
			if (c.isInstance(current))
			{
				return c.cast(current);
			}

			// Check parent
			current = current.getParent();
		}

		// Failed to find component
		return null;
	}

	/**
	 * @return The nearest markup container with associated markup
	 */
	public final MarkupContainer findParentWithAssociatedMarkup()
	{
		MarkupContainer container = parent;
		while (container != null)
		{
			if (container.getAssociatedMarkup() != null)
			{
				return container;
			}
			container = container.getParent();
		}

		// This should never happen since Page always has associated markup
		throw new WicketRuntimeException("Unable to find parent with associated markup");
	}

	/**
	 * Gets interface to application that this component is a part of.
	 * 
	 * @return The application associated with the session that this component is in.
	 * @see Application
	 */
	public final Application getApplication()
	{
		return Application.get();
	}

	/**
	 * @return A path of the form [page-class-name]:[page-relative-path]
	 * @see Component#getPageRelativePath()
	 */
	public final String getClassRelativePath()
	{
		return getClass().getName() + PATH_SEPARATOR + getPageRelativePath();
	}

	/**
	 * Get the converter that should be used by this component, delegates to
	 * {@link #createConverter(Class)} and then to the application's
	 * {@link IConverterLocator}.
	 *
	 * @param type
	 *            The type to convert to
	 *
	 * @return The converter that should be used by this component
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <C> IConverter<C> getConverter(Class<C> type) {
		IConverter<?> converter = createConverter(type);
		if (converter != null) {
			return (IConverter<C>) converter;
		}
		return getApplication().getConverterLocator().getConverter(type);
	}

	/**
	 * Factory method for converters to be used by this component,
	 * returns {@code null} by default.
	 *
	 * @param type
	 *            The type to convert to
	 *
	 * @return a converter to be used by this component
	 */
	protected IConverter<?> createConverter(Class<?> type) {
		return null;
	}

	/**
	 * Gets whether model strings should be escaped.
	 * 
	 * @return Returns whether model strings should be escaped
	 */
	public final boolean getEscapeModelStrings()
	{
		return getFlag(FLAG_ESCAPE_MODEL_STRINGS);
	}

	/**
	 * Gets the id of this component.
	 * 
	 * @return The id of this component
	 */
	@Override
	public String getId()
	{
		return id;
	}

	/**
	 * @return Innermost model for this component
	 */
	public final IModel<?> getInnermostModel()
	{
		return getInnermostModel(getDefaultModel());
	}

	/**
	 * Gets the locale for this component. By default, it searches its parents for a locale. If no
	 * parents (it's a recursive search) returns a locale, it gets one from the session.
	 * 
	 * @return The locale to be used for this component
	 * @see Session#getLocale()
	 */
	public Locale getLocale()
	{
		if (parent != null)
		{
			return parent.getLocale();
		}
		return getSession().getLocale();
	}

	/**
	 * Convenience method to provide easy access to the localizer object within any component.
	 * 
	 * @return The localizer object
	 */
	public final Localizer getLocalizer()
	{
		return getApplication().getResourceSettings().getLocalizer();
	}

	/**
	 * Get the first component tag in the associated markup
	 * 
	 * @return first component tag
	 */
	private ComponentTag getMarkupTag()
	{
		IMarkupFragment markup = getMarkup();
		if (markup != null)
		{
			for (int i = 0; i < markup.size(); i++)
			{
				MarkupElement elem = markup.get(i);
				if (elem instanceof ComponentTag)
				{
					return (ComponentTag)elem;
				}
			}
		}
		return null;
	}

	/**
	 * THIS IS WICKET INTERNAL ONLY. DO NOT USE IT.
	 * 
	 * Get a copy of the markup's attributes which are associated with the component.
	 * <p>
	 * Modifications to the map returned don't change the tags attributes. It is just a copy.
	 * <p>
	 * Note: The component must have been added (directly or indirectly) to a container with an
	 * associated markup file (Page, Panel or Border).
	 * 
	 * @return markup attributes
	 */
	public final ValueMap getMarkupAttributes()
	{
		ComponentTag tag = getMarkupTag();
		if (tag != null)
		{
			ValueMap attrs = new ValueMap(tag.getAttributes());
			attrs.makeImmutable();
			return attrs;
		}
		return ValueMap.EMPTY_MAP;
	}

	/**
	 * Get the markupId
	 * 
	 * @return MarkupId
	 */
	public final Object getMarkupIdImpl()
	{
		if (generatedMarkupId != -1)
		{
			return generatedMarkupId;
		}

		String id = getMetaData(MARKUP_ID_KEY);

		// if still no markup id is found, and the component has been attached to a page, try to
		// retrieve the id from the markup file.
		if (id == null && findPage() != null)
		{
			id = getMarkupIdFromMarkup();
		}
		return id;
	}

	/**
	 * Retrieves id by which this component is represented within the markup. This is either the id
	 * attribute set explicitly via a call to {@link #setMarkupId(String)}, id attribute defined in
	 * the markup, or an automatically generated id - in that order.
	 * <p>
	 * If no id is set and <code>createIfDoesNotExist</code> is false, this method will return null.
	 * Otherwise it will generate an id value which by default will be unique in the page. This is
	 * the preferred way as there is no chance of id collision. This will also enable
	 * {@link #setOutputMarkupId(boolean)}.
	 * <p>
	 * 
	 * <p>
	 * Note: This method should only be called after the component or its parent have been added to
	 * the page.
	 * 
	 * @param createIfDoesNotExist
	 *            When there is no existing markup id, determines whether it should be generated or
	 *            whether <code>null</code> should be returned.
	 * 
	 * @return markup id of the component
	 */
	public String getMarkupId(boolean createIfDoesNotExist)
	{
		IMarkupIdGenerator markupIdGenerator = getApplication().getMarkupSettings().getMarkupIdGenerator();
		String markupId = markupIdGenerator.generateMarkupId(this, createIfDoesNotExist);
		return markupId;
	}

	/**
	 * Retrieves id by which this component is represented within the markup. This is either the id
	 * attribute set explicitly via a call to {@link #setMarkupId(String)}, id attribute defined in
	 * the markup, or an automatically generated id - in that order.
	 * <p>
	 * If no explicit id is set this function will generate an id value that will be unique in the
	 * page. This is the preferred way as there is no chance of id collision. This will also enable
	 * {@link #setOutputMarkupId(boolean)}.
	 * <p>
	 * Note: This method should only be called after the component or its parent have been added to
	 * the page.
	 * 
	 * @return markup id of the component
	 */
	public String getMarkupId()
	{
		return getMarkupId(true);
	}

	/**
	 * Gets metadata for this component using the given key.
	 * 
	 * @param <M>
	 *            The type of the metadata.
	 * 
	 * @param key
	 *            The key for the data
	 * @return The metadata or null of no metadata was found for the given key
	 * @see MetaDataKey
	 */
	public final <M extends Serializable> M getMetaData(final MetaDataKey<M> key)
	{
		return key.get(getMetaData());
	}

	/**
	 * Gets the meta data entries for this component as an array of {@link MetaDataEntry} objects.
         *
	 * @return the meta data entries for this component
	 */
	private MetaDataEntry<?>[] getMetaData()
	{
		MetaDataEntry<?>[] metaData = null;

		// index where we should expect the entry
		int index = getFlag(FLAG_MODEL_SET) ? 1 : 0;

		int length = data_length();

		if (index < length)
		{
			Object object = data_get(index);
			if (object instanceof MetaDataEntry<?>[])
			{
				metaData = (MetaDataEntry<?>[])object;
			}
			else if (object instanceof MetaDataEntry)
			{
				metaData = new MetaDataEntry[] { (MetaDataEntry<?>)object };
			}
		}

		return metaData;
	}

	/**
	 * Gets the model. It returns the object that wraps the backing model.
	 * 
	 * @return The model
	 */
	public final IModel<?> getDefaultModel()
	{
		IModel<?> model = getModelImpl();
		// If model is null
		if (model == null)
		{
			// give subclass a chance to lazy-init model
			model = initModel();
			setModelImpl(model);
		}

		return model;
	}

	/**
	 * Gets the backing model object. Unlike getDefaultModel().getObject(), this method returns null
	 * for a null model.
	 * 
	 * @return The backing model object
	 */
	public final Object getDefaultModelObject()
	{
		final IModel<?> model = getDefaultModel();
		if (model != null)
		{
			try
			{
				// Get model value for this component.
				return model.getObject();
			}
			catch (Exception ex)
			{
				// wrap the exception so that it brings info about the component
				WicketRuntimeException rex = new WicketRuntimeException(
					"An error occurred while getting the model object for Component: " +
						this.toString(true), ex);
				throw rex;
			}
		}
		return null;
	}

	/**
	 * Gets a model object as a string. Depending on the "escape model strings" flag of the
	 * component, the string is either HTML escaped or not. "HTML escaped" meaning that only HTML
	 * sensitive chars are escaped but not all none-ascii chars. Proper HTML encoding should be used
	 * instead. In case you really need a fully escaped model string you may call
	 * {@link Strings#escapeMarkup(CharSequence, boolean, boolean)} on the model string returned.
	 * 
	 * @see Strings#escapeMarkup(CharSequence, boolean, boolean)
	 * @see #getEscapeModelStrings()
	 * 
	 * @return Model object for this component as a string
	 */
	public final String getDefaultModelObjectAsString()
	{
		return getDefaultModelObjectAsString(getDefaultModelObject());
	}

	/**
	 * Gets a model object as a string. Depending on the "escape model strings" flag of the
	 * component, the string is either HTML escaped or not. "HTML escaped" meaning that only HTML
	 * sensitive chars are escaped but not all none-ascii chars. Proper HTML encoding should be used
	 * instead. In case you really need a fully escaped model string you may call
	 * {@link Strings#escapeMarkup(CharSequence, boolean, boolean)} on the model string returned.
	 * 
	 * @see Strings#escapeMarkup(CharSequence, boolean, boolean)
	 * @see #getEscapeModelStrings()
	 * 
	 * @param modelObject
	 *            Model object to convert to string
	 * @return The string
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final String getDefaultModelObjectAsString(final Object modelObject)
	{
		if (modelObject != null)
		{
			// Get converter
			final Class<?> objectClass = modelObject.getClass();

			final IConverter converter = getConverter(objectClass);

			// Model string from property
			final String modelString = converter.convertToString(modelObject, getLocale());

			if (modelString != null)
			{
				// If we should escape the markup
				if (getFlag(FLAG_ESCAPE_MODEL_STRINGS))
				{
					// Escape HTML sensitive characters only. Not all none-ascii chars
					return Strings.escapeMarkup(modelString, false, false).toString();
				}
				return modelString;
			}
		}
		return "";
	}

	/**
	 * Gets whether or not component will output id attribute into the markup. id attribute will be
	 * set to the value returned from {@link Component#getMarkupId()}.
	 * 
	 * @return whether or not component will output id attribute into the markup
	 */
	public final boolean getOutputMarkupId()
	{
		return getFlag(FLAG_OUTPUT_MARKUP_ID);
	}

	/**
	 * Gets whether or not an invisible component will render a placeholder tag.
	 * 
	 * @return true if a placeholder tag should be rendered
	 */
	public final boolean getOutputMarkupPlaceholderTag()
	{
		return getFlag(FLAG_PLACEHOLDER);
	}

	/**
	 * Gets the page holding this component.
	 * 
	 * @return The page holding this component
	 * @throws WicketRuntimeException
	 *             Thrown if component is not yet attached to a Page.
	 * @see #findPage()
	 */
	@Override
	public final Page getPage()
	{
		// Search for nearest Page
		final Page page = findPage();

		// If no Page was found
		if (page == null)
		{
			// Give up with a nice exception
			throw new WicketRuntimeException("No Page found for component " + this);
		}

		return page;
	}

	/**
	 * Gets the path to this component relative to its containing page, i.e. without leading page
	 * id.
	 * 
	 * @return The path to this component relative to the page it is in
	 */
	@Override
	public final String getPageRelativePath()
	{
		return Strings.afterFirstPathComponent(getPath(), PATH_SEPARATOR);
	}

	/**
	 * Gets any parent container, or null if there is none.
	 * 
	 * @return Any parent container, or null if there is none
	 */
	@Override
	public final MarkupContainer getParent()
	{
		return parent;
	}

	/**
	 * Gets this component's path.
	 * 
	 * @return Colon separated path to this component in the component hierarchy
	 */
	public final String getPath()
	{
		final PrependingStringBuffer buffer = new PrependingStringBuffer(32);
		for (Component c = this; c != null; c = c.getParent())
		{
			if (buffer.length() > 0)
			{
				buffer.prepend(PATH_SEPARATOR);
			}
			buffer.prepend(c.getId());
		}
		return buffer.toString();
	}

	/**
	 * If false the component's tag will be printed as well as its body (which is default). If true
	 * only the body will be printed, but not the component's tag.
	 * 
	 * @return If true, the component tag will not be printed
	 */
	public final boolean getRenderBodyOnly()
	{
		return getFlag(FLAG_RENDER_BODY_ONLY);
	}

	/**
	 * @return The request for this component's active request cycle
	 */
	public final Request getRequest()
	{
		RequestCycle requestCycle = getRequestCycle();
		if (requestCycle == null)
		{
			// Happens often with WicketTester when one forgets to call
			// createRequestCycle()
			throw new WicketRuntimeException("No RequestCycle is currently set!");
		}
		return requestCycle.getRequest();
	}

	/**
	 * Gets the active request cycle for this component
	 * 
	 * @return The request cycle
	 */
	public final RequestCycle getRequestCycle()
	{
		return RequestCycle.get();
	}

	/**
	 * @return The response for this component's active request cycle
	 */
	public final Response getResponse()
	{
		return getRequestCycle().getResponse();
	}

	/**
	 * Gets the current Session object.
	 * 
	 * @return The Session that this component is in
	 */
	public Session getSession()
	{
		return Session.get();
	}

	/**
	 * @return Size of this Component in bytes. Returns {@code 0} - if the size cannot be calculated for some reason
	 */
	public long getSizeInBytes()
	{
		final MarkupContainer originalParent = parent;
		parent = null;
		long size = 0;
		try
		{
			size = WicketObjects.sizeof(this);
		}
		catch (Exception e)
		{
			log.error("Exception getting size for component " + this, e);
		}
		parent = originalParent;
		return size;
	}

	/**
	 * @param key
	 *            Key of string resource in property file
	 * @return The String
	 * @see Localizer
	 */
	public final String getString(final String key)
	{
		return getString(key, null);
	}

	/**
	 * @param key
	 *            The resource key
	 * @param model
	 *            The model
	 * @return The formatted string
	 * @see Localizer
	 */
	public final String getString(final String key, final IModel<?> model)
	{
		return getLocalizer().getString(key, this, model);
	}

	/**
	 * @param key
	 *            The resource key
	 * @param model
	 *            The model
	 * @param defaultValue
	 *            A default value if the string cannot be found
	 * @return The formatted string
	 * @see Localizer
	 */
	public final String getString(final String key, final IModel<?> model, final String defaultValue)
	{
		return getLocalizer().getString(key, this, model, defaultValue);
	}

	/**
	 * A convenience method to access the Sessions's style.
	 * 
	 * @return The style of this component respectively the style of the Session.
	 * 
	 * @see org.apache.wicket.Session#getStyle()
	 */
	public final String getStyle()
	{
		Session session = getSession();
		if (session == null)
		{
			throw new WicketRuntimeException("Wicket Session object not available");
		}
		return session.getStyle();
	}

	/**
	 * Gets the variation string of this component that will be used to look up markup for this
	 * component. Subclasses can override this method to define by an instance what markup variation
	 * should be picked up. By default it will return null or the value of a parent.
	 * 
	 * @return The variation of this component.
	 */
	public String getVariation()
	{
		if (parent != null)
		{
			return parent.getVariation();
		}
		return null;
	}

	/**
	 * Gets whether this component was rendered at least once.
	 * 
	 * @return true if the component has been rendered before, false if it is merely constructed
	 */
	public final boolean hasBeenRendered()
	{
		return getFlag(FLAG_HAS_BEEN_RENDERED);
	}

	/**
	 * Gets feedback messages for this component. This method will instantiate a
	 * {@link FeedbackMessages} instance and add it to the component metadata, even when called on a
	 * component that has no feedback messages, to avoid the overhead use
	 * {@link #hasFeedbackMessage()}
	 * 
	 * @return feedback messages instance
	 */
	public FeedbackMessages getFeedbackMessages()
	{
		FeedbackMessages messages = getMetaData(FEEDBACK_KEY);
		if (messages == null)
		{
			messages = new FeedbackMessages();
			setMetaData(FEEDBACK_KEY, messages);
		}
		return messages;
	}

	/**
	 * @return True if this component has an error message
	 */
	public final boolean hasErrorMessage()
	{
		FeedbackMessages messages = getMetaData(FEEDBACK_KEY);
		if (messages == null)
		{
			return false;
		}
		return messages.hasMessage(FeedbackMessage.ERROR);
	}

	/**
	 * @return True if this component has some kind of feedback message
	 * 
	 */
	public final boolean hasFeedbackMessage()
	{
		FeedbackMessages messages = getMetaData(FEEDBACK_KEY);
		if (messages == null)
		{
			return false;
		}
		return messages.size() > 0;
	}

	/**
	 * Registers an informational feedback message for this component
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void info(final Serializable message)
	{
		getFeedbackMessages().info(this, message);
		addStateChange();
	}

	/**
	 * Registers an success feedback message for this component
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void success(final Serializable message)
	{
		getFeedbackMessages().success(this, message);
		addStateChange();
	}

	/**
	 * Authorizes an action for a component.
	 * 
	 * @param action
	 *            The action to authorize
	 * @return True if the action is allowed
	 * @throws AuthorizationException
	 *             Can be thrown by implementation if action is unauthorized
	 */
	public final boolean isActionAuthorized(Action action)
	{
		IAuthorizationStrategy authorizationStrategy = getSession().getAuthorizationStrategy();
		if (authorizationStrategy != null)
		{
			return authorizationStrategy.isActionAuthorized(this, action);
		}
		return true;
	}

	/**
	 * @return true if this component is authorized to be enabled, false otherwise
	 */
	public final boolean isEnableAllowed()
	{
		return isActionAuthorized(ENABLE);
	}

	/**
	 * Gets whether this component is enabled. Specific components may decide to implement special
	 * behavior that uses this property, like web form components that add a disabled='disabled'
	 * attribute when enabled is false.
	 * 
	 * @return Whether this component is enabled.
	 */
	public boolean isEnabled()
	{
		return getFlag(FLAG_ENABLED);
	}

	/**
	 * Checks the security strategy if the {@link Component#RENDER} action is allowed on this
	 * component
	 * 
	 * @return ture if {@link Component#RENDER} action is allowed, false otherwise
	 */
	public final boolean isRenderAllowed()
	{
		return getFlag(FLAG_IS_RENDER_ALLOWED);
	}

	/**
	 * Returns if the component is stateless or not. It checks the stateless hint if that is false
	 * it returns directly false. If that is still true it checks all its behaviors if they can be
	 * stateless.
	 * 
	 * @return whether the component is stateless.
	 */
	public final boolean isStateless()
	{
		if (
		// the component is either invisible or disabled
		(isVisibleInHierarchy() && isEnabledInHierarchy()) == false &&

		// and it can't call listener interfaces
			canCallListenerInterface(null) == false)
		{
			// then pretend the component is stateless
			return true;
		}

		if (!getStatelessHint())
		{
			return false;
		}

		for (Behavior behavior : getBehaviors())
		{
			if (!behavior.getStatelessHint(this))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @return {@code true} if this component should notify its holding page about changes in its
	 *         state. If a {@link Page} is not versioned then it wont track changes in its
	 *         components and will use the same {@link Page#getPageId()} during its lifetime
	 */
	public boolean isVersioned()
	{
		// Is the component itself versioned?
		if (!getFlag(FLAG_VERSIONED))
		{
			return false;
		}
		else
		{
			// If there's a parent and this component is versioned
			if (parent != null)
			{
				// Check if the parent is unversioned. If any parent
				// (recursively) is unversioned, then this component is too
				if (!parent.isVersioned())
				{
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Gets whether this component and any children are visible.
	 * <p>
	 * WARNING: this method can be called multiple times during a request. If you override this
	 * method, it is a good idea to keep it cheap in terms of processing. Alternatively, you can
	 * call {@link #setVisible(boolean)}.
	 * <p>
	 * 
	 * @return True if component and any children are visible
	 */
	public boolean isVisible()
	{
		return getFlag(FLAG_VISIBLE);
	}

	/**
	 * Checks if the component itself and all its parents are visible.
	 * 
	 * @return true if the component and all its parents are visible.
	 */
	public final boolean isVisibleInHierarchy()
	{
		Component parent = getParent();
		if (parent != null && !parent.isVisibleInHierarchy())
		{
			return false;
		}
		else
		{
			return determineVisibility();
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * Sets the RENDERING flag and removes the PREPARED_FOR_RENDER flag on component and it's
	 * children.
	 * 
	 * @param setRenderingFlag
	 *            if this is false only the PREPARED_FOR_RENDER flag is removed from component, the
	 *            RENDERING flag is not set.
	 * 
	 * @see #internalPrepareForRender(boolean)
	 */
	public final void markRendering(boolean setRenderingFlag)
	{
		internalMarkRendering(setRenderingFlag);
	}

	/**
	 * Called to indicate that the model content for this component has been changed
	 */
	public final void modelChanged()
	{
		// Call user code
		internalOnModelChanged();
		onModelChanged();
	}

	/**
	 * Called to indicate that the model content for this component is about to change
	 */
	public final void modelChanging()
	{
		checkHierarchyChange(this);

		// Call user code
		onModelChanging();

		// Tell the page that our model changed
		final Page page = findPage();
		if (page != null)
		{
			page.componentModelChanging(this);
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * <p>
	 * Prepares the component and it's children for rendering. On whole page render this method must
	 * be called on the page. On AJAX request, this method must be called on the updated component.
	 * 
	 * @param setRenderingFlag
	 *            Whether to set the rendering flag. This must be true if the page is about to be
	 *            rendered. However, there are usecases to call this method without an immediate
	 *            render (e.g. on stateless listener request target to build the component
	 *            hierarchy), in that case setRenderingFlag should be false.
	 */
	public void internalPrepareForRender(boolean setRenderingFlag)
	{
		beforeRender();

		if (setRenderingFlag)
		{
			// only process feedback panel when we are about to be rendered.
			// setRenderingFlag is false in case prepareForRender is called only to build component
			// hierarchy (i.e. in BookmarkableListenerInterfaceRequestHandler).
			// prepareForRender(true) is always called before the actual rendering is done so
			// that's where feedback panels gather the messages

			List<Component> feedbacks = getRequestCycle().getMetaData(FEEDBACK_LIST);
			if (feedbacks != null)
			{
				// iterate over a copy because a IFeedback may add more IFeedback children
// (WICKET-4687)
				Component[] feedbacksCopy = feedbacks.toArray(new Component[feedbacks.size()]);
				for (Component feedback : feedbacksCopy)
				{
					// render it only if it is still in the page hierarchy (WICKET-4895)
					if (feedback.findPage() != null)
					{
						feedback.internalBeforeRender();
					}
				}
			}
			getRequestCycle().setMetaData(FEEDBACK_LIST, null);
		}

		markRendering(setRenderingFlag);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * Prepares the component and it's children for rendering. On whole page render this method must
	 * be called on the page. On AJAX request, this method must be called on updated component.
	 */
	public final void prepareForRender()
	{
		internalPrepareForRender(true);
	}

	/**
	 * Redirects browser to an intermediate page such as a sign-in page. The current request's URL
	 * is saved for future use by method {@link #continueToOriginalDestination()}; only use this method when
	 * you plan to continue to the current URL at some later time; otherwise just set a new response page.
	 * 
	 * @param page
	 *            The sign in page
	 *
	 * @see #setResponsePage(Class)
	 * @see #setResponsePage(IRequestablePage)
	 * @see #setResponsePage(Class, PageParameters)
	 * @see Component#continueToOriginalDestination()
	 */
	public final void redirectToInterceptPage(final Page page)
	{
		throw new RestartResponseAtInterceptPageException(page);
	}

	/**
	 * Removes this component from its parent. It's important to remember that a component that is
	 * removed cannot be referenced from the markup still.
	 * <p>
	 * You must not use this method in your callback to any of the
	 * {@link MarkupContainer#visitChildren(IVisitor)} methods. See <a
	 * href="https://issues.apache.org/jira/browse/WICKET-3229">WICKET-3329</a>.
	 */
	public final void remove()
	{
		if (parent == null)
		{
			throw new IllegalStateException("Cannot remove " + this + " from null parent!");
		}
		parent.remove(this);
	}


	/**
	 * Render the Component.
	 */
	public final void render()
	{
		ComponentContext.push(new ComponentContext(this));
		try {
			RuntimeException exception = null;
	
			try
			{
				// Invoke prepareForRender only if this is the root component to be rendered
				MarkupContainer parent = getParent();
				if ((parent == null) || (parent.getFlag(FLAG_RENDERING) == false) || isAuto())
				{
					internalPrepareForRender(true);
				}
	
				// Do the render
				internalRender();
			}
			catch (final RuntimeException ex)
			{
				// Remember it as the originating exception
				exception = ex;
			}
			finally
			{
				try
				{
					// Cleanup
					afterRender();
				}
				catch (RuntimeException ex2)
				{
					// Only remember it if not already another exception happened
					if (exception == null)
					{
						exception = ex2;
					}
				}
			}
	
			// Re-throw if needed
			if (exception != null)
			{
				throw exception;
			}
		} finally {
			ComponentContext.pop();
		}
	}

	/**
	 * Performs a render of this component as part of a Page level render process.
	 */
	private void internalRender()
	{
		// Make sure there is a markup available for the Component
		IMarkupFragment markup = getMarkup();
		if (markup == null)
		{
			throw new MarkupNotFoundException("Markup not found for Component: " + toString());
		}

		// MarkupStream is an Iterator for the markup
		MarkupStream markupStream = new MarkupStream(markup);

		// Flag: we started the render process
		markRendering(true);

		MarkupElement elem = markup.get(0);
		if (elem instanceof ComponentTag)
		{
			// Guarantee that the markupStream is set and determineVisibility not yet tested
			// See WICKET-2049
			((ComponentTag)elem).onBeforeRender(this, markupStream);
		}

		// Determine if component is visible using it's authorization status
		// and the isVisible property.
		if (determineVisibility())
		{
			setFlag(FLAG_HAS_BEEN_RENDERED, true);

			// Rendering is beginning
			if (log.isDebugEnabled())
			{
				log.debug("Begin render {}", this);
			}

			try
			{
				notifyBehaviorsComponentBeforeRender();
				onRender();
				notifyBehaviorsComponentRendered();

				// Component has been rendered
				rendered();
			}
			catch (RuntimeException ex)
			{
				onException(ex);
			}

			if (log.isDebugEnabled())
			{
				log.debug("End render {}", this);
			}
		}
		// elem is null when rendering a page
		else if ((elem != null) && (elem instanceof ComponentTag))
		{
			if (getFlag(FLAG_PLACEHOLDER))
			{
				renderPlaceholderTag((ComponentTag)elem, getResponse());
			}
		}
	}

	/**
	 * Called when a runtime exception is caught during the render process
	 * 
	 * @param ex
	 *            The exception caught.
	 */
	private void onException(final RuntimeException ex)
	{
		// Call each behaviors onException() to allow the
		// behavior to clean up
		for (Behavior behavior : getBehaviors())
		{
			if (isBehaviorAccepted(behavior))
			{
				try
				{
					behavior.onException(this, ex);
				}
				catch (Exception ex2)
				{
					log.error("Error while cleaning up after exception", ex2);
				}
			}
		}

		// Re-throw the exception
		throw ex;
	}

	/**
	 * Renders a placeholder tag for the component when it is invisible and
	 * {@link #setOutputMarkupPlaceholderTag(boolean)} has been called with <code>true</code>.
	 * 
	 * @param tag
	 *            component tag
	 * @param response
	 *            response
	 */
	protected void renderPlaceholderTag(final ComponentTag tag, final Response response)
	{
		String ns = Strings.isEmpty(tag.getNamespace()) ? null : tag.getNamespace() + ':';

		response.write("<");
		if (ns != null)
		{
			response.write(ns);
		}
		response.write(tag.getName());
		response.write(" id=\"");
		response.write(getAjaxRegionMarkupId());
		response.write("\" style=\"display:none\"></");
		if (ns != null)
		{
			response.write(ns);
		}
		response.write(tag.getName());
		response.write(">");
	}


	/**
	 * Returns the id of the markup region that will be updated via ajax. This can be different to
	 * the markup id of the component if a {@link IAjaxRegionMarkupIdProvider} behavior has been
	 * added.
	 * 
	 * @return the markup id of the region to be updated via ajax.
	 */
	public final String getAjaxRegionMarkupId()
	{
		String markupId = null;
		for (Behavior behavior : getBehaviors())
		{
			if (behavior instanceof IAjaxRegionMarkupIdProvider && behavior.isEnabled(this))
			{
				markupId = ((IAjaxRegionMarkupIdProvider)behavior).getAjaxRegionMarkupId(this);
				break;
			}
		}
		if (markupId == null)
		{
			if (this instanceof IAjaxRegionMarkupIdProvider)
			{
				markupId = ((IAjaxRegionMarkupIdProvider)this).getAjaxRegionMarkupId(this);
			}
		}
		if (markupId == null)
		{
			markupId = getMarkupId();
		}
		return markupId;
	}


	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * <p>
	 * Renders the component at the current position in the given markup stream. The method
	 * onComponentTag() is called to allow the component to mutate the start tag. The method
	 * onComponentTagBody() is then called to permit the component to render its body.
	 */
	public final void internalRenderComponent()
	{
		final IMarkupFragment markup = getMarkup();
		if (markup == null)
		{
			throw new MarkupException("Markup not found. Component: " + toString());
		}

		final MarkupStream markupStream = new MarkupStream(markup);

		// Get mutable copy of next tag
		final ComponentTag openTag = markupStream.getTag();
		final ComponentTag tag = openTag.mutable();

		// call application-wide tag listeners
		getApplication().getOnComponentTagListeners().onComponentTag(this, tag);

		// Call any tag handler
		onComponentTag(tag);

		// If we're an openclose tag
		if (!tag.isOpenClose() && !tag.isOpen())
		{
			// We were something other than <tag> or <tag/>
			markupStream.throwMarkupException("Method renderComponent called on bad markup element: " +
				tag);
		}

		if (tag.isOpenClose() && openTag.isOpen())
		{
			markupStream.throwMarkupException("You can not modify a open tag to open-close: " + tag);
		}

		try
		{
			// Render open tag
			boolean renderBodyOnly = getRenderBodyOnly();
			if (renderBodyOnly)
			{
				ExceptionSettings.NotRenderableErrorStrategy notRenderableErrorStrategy = ExceptionSettings.NotRenderableErrorStrategy.LOG_WARNING;
				if (Application.exists())
				{
					notRenderableErrorStrategy = getApplication().getExceptionSettings().getNotRenderableErrorStrategy();
				}

				if (getFlag(FLAG_OUTPUT_MARKUP_ID))
				{
					String message = String.format("Markup id set on a component that renders its body only. " +
					                               "Markup id: %s, component id: %s.", getMarkupId(), getId());
					if (notRenderableErrorStrategy == ExceptionSettings.NotRenderableErrorStrategy.THROW_EXCEPTION)
					{
						throw new IllegalStateException(message);
					}
					log.warn(message);
				}
				if (getFlag(FLAG_PLACEHOLDER))
				{
					String message = String.format("Placeholder tag set on a component that renders its body only. " +
					                               "Component id: %s.", getId());
					if (notRenderableErrorStrategy == ExceptionSettings.NotRenderableErrorStrategy.THROW_EXCEPTION)
					{
						throw new IllegalStateException(message);
					}
					log.warn(message);
				}
			}
			else
			{
				renderComponentTag(tag);
			}
			markupStream.next();

			// Render the body only if open-body-close. Do not render if open-close.
			if (tag.isOpen())
			{
				// Render the body. The default strategy will simply call the component's
				// onComponentTagBody() implementation.
				getMarkupSourcingStrategy().onComponentTagBody(this, markupStream, tag);

				// Render close tag
				if (openTag.isOpen())
				{
					renderClosingComponentTag(markupStream, tag, renderBodyOnly);
				}
				else if (renderBodyOnly == false)
				{
					if (needToRenderTag(openTag))
					{
						// Close the manually opened tag. And since the user might have changed the
						// tag name ...
						getResponse().write(tag.syntheticCloseTagString());
					}
				}
			}
		}
		catch (WicketRuntimeException wre)
		{
			throw wre;
		}
		catch (RuntimeException re)
		{
			throw new WicketRuntimeException("Exception in rendering component: " + this, re);
		}
	}

	/**
	 * 
	 * @param openTag
	 * @return true, if the tag shall be rendered
	 */
	private boolean needToRenderTag(final ComponentTag openTag)
	{
		// If a open-close tag has been modified to be open-body-close then a
		// synthetic close tag must be rendered.
		boolean renderTag = (openTag != null && !(openTag instanceof WicketTag));
		if (renderTag == false)
		{
			renderTag = !getApplication().getMarkupSettings().getStripWicketTags();
		}
		return renderTag;
	}

	/**
	 * Called to indicate that a component has been rendered. This method should only very rarely be
	 * called at all. Some components may render its children without calling render() on them.
	 * These components need to call rendered() to indicate that its child components were actually
	 * rendered, the framework would think they had never been rendered, and in development mode
	 * this would result in a runtime exception.
	 */
	public final void rendered()
	{
		Page page = findPage();
		if (page != null)
		{
			// Tell the page that the component rendered
			page.componentRendered(this);
		}
		else
		{
			log.error("Component is not connected to a Page. Cannot register the component as being rendered. Component: " +
				toString());
		}
	}

	/**
	 * Get the markup sourcing strategy for the component. If null,
	 * {@link #newMarkupSourcingStrategy()} will be called.
	 * 
	 * @return Markup sourcing strategy
	 */
	protected final IMarkupSourcingStrategy getMarkupSourcingStrategy()
	{
		if (markupSourcingStrategy == null)
		{
			markupSourcingStrategy = newMarkupSourcingStrategy();

			// If not strategy by provided, than we use a default one.
			if (markupSourcingStrategy == null)
			{
				markupSourcingStrategy = DefaultMarkupSourcingStrategy.get();
			}
		}
		return markupSourcingStrategy;
	}

	/**
	 * If {@link #getMarkupSourcingStrategy()} returns null, this method will be called. By default
	 * it returns null, which means that a default markup strategy will be attached to the
	 * component.
	 * <p>
	 * Please note that markup source strategies are not persisted. Instead they get re-created by
	 * calling this method again. That's ok since markup sourcing strategies usually do not maintain
	 * a state.
	 * 
	 * @return Markup sourcing strategy
	 */
	protected IMarkupSourcingStrategy newMarkupSourcingStrategy()
	{
		return null;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * Print to the web response what ever the component wants to contribute to the head section.
	 * Make sure that all attached behaviors are asked as well.
	 * <p>
	 * NOT intended for overriding by framework clients. Rather, use
	 * {@link Component#renderHead(org.apache.wicket.markup.head.IHeaderResponse)}
	 * </p>
	 * 
	 * @param container
	 *            The HtmlHeaderContainer
	 */
	public void internalRenderHead(final HtmlHeaderContainer container)
	{
		ComponentContext.push(new ComponentContext(this));
		try {
			if (isVisibleInHierarchy() && isRenderAllowed())
			{
				if (log.isDebugEnabled())
				{
					log.debug("internalRenderHead: {}", toString(false));
				}
	
				IHeaderResponse response = container.getHeaderResponse();
	
				// Allow component to contribute
				if (response.wasRendered(this) == false)
				{
					StringResponse markupHeaderResponse = new StringResponse();
					Response oldResponse = getResponse();
					RequestCycle.get().setResponse(markupHeaderResponse);
					try
					{
						// Make sure the markup source strategy contributes to the header first
						// to be backward compatible. WICKET-3761
						getMarkupSourcingStrategy().renderHead(this, container);
						CharSequence headerContribution = markupHeaderResponse.getBuffer();
						if (Strings.isEmpty(headerContribution) == false)
						{
							response.render(StringHeaderItem.forString(headerContribution));
						}
					}
					finally
					{
						RequestCycle.get().setResponse(oldResponse);
					}
					// Then let the component itself to contribute to the header
					renderHead(this, response);
	
					response.markRendered(this);
				}
	
				// Then ask all behaviors
				for (Behavior behavior : getBehaviors())
				{
					if (isBehaviorAccepted(behavior))
					{
						if (response.wasRendered(behavior) == false)
						{
							behavior.renderHead(this, response);
							List<IClusterable> pair = Arrays.asList(this, behavior);
							response.markRendered(pair);
						}
					}
				}
			}
		} finally {
			ComponentContext.pop();
		}
	}

	/**
	 * Replaces this component with another. The replacing component must have the same component id
	 * as this component. This method serves as a shortcut to
	 * 
	 * <code>this.getParent().replace(replacement)</code>
	 * 
	 * and provides a better context for errors.
	 * <p>
	 * Usage: <code>component = component.replaceWith(replacement);</code>
	 * </p>
	 * 
	 * @since 1.2.1
	 * 
	 * @param replacement
	 *            component to replace this one
	 * @return the component which replaced this one
	 */
	public Component replaceWith(Component replacement)
	{
		Args.notNull(replacement, "replacement");

		if (!getId().equals(replacement.getId()))
		{
			throw new IllegalArgumentException(
				"Replacement component must have the same id as the component it will replace. Replacement id [[" +
					replacement.getId() + "]], replaced id [[" + getId() + "]].");
		}
		if (parent == null)
		{
			throw new IllegalStateException(
				"This method can only be called on a component that has already been added to its parent.");
		}
		parent.replace(replacement);
		return replacement;
	}

	/**
	 * @param component
	 *            The component to compare with
	 * @return True if the given component's model is the same as this component's model.
	 */
	public final boolean sameInnermostModel(final Component component)
	{
		return sameInnermostModel(component.getDefaultModel());
	}

	/**
	 * @param model
	 *            The model to compare with
	 * @return True if the given component's model is the same as this component's model.
	 */
	public final boolean sameInnermostModel(final IModel<?> model)
	{
		// Get the two models
		IModel<?> thisModel = getDefaultModel();

		// If both models are non-null they could be the same
		if (thisModel != null && model != null)
		{
			return getInnermostModel(thisModel) == getInnermostModel(model);
		}

		return false;
	}

	/**
	 * Sets whether this component is enabled. Specific components may decide to implement special
	 * behavior that uses this property, like web form components that add a disabled='disabled'
	 * attribute when enabled is false. If it is not enabled, it will not be allowed to call any
	 * listener method on it (e.g. Link.onClick) and the model object will be protected (for the
	 * common use cases, not for programmer's misuse)
	 * 
	 * @param enabled
	 *            whether this component is enabled
	 * @return This
	 */
	public final Component setEnabled(final boolean enabled)
	{
		// Is new enabled state a change?
		if (enabled != getFlag(FLAG_ENABLED))
		{
			// Tell the page that this component's enabled was changed
			if (isVersioned())
			{
				final Page page = findPage();
				if (page != null)
				{
					addStateChange();
				}
			}

			// Change visibility
			setFlag(FLAG_ENABLED, enabled);
			onEnabledStateChanged();
		}
		return this;
	}

	void clearEnabledInHierarchyCache()
	{
		setRequestFlag(RFLAG_ENABLED_IN_HIERARCHY_SET, false);
	}

	void onEnabledStateChanged()
	{
		clearEnabledInHierarchyCache();
	}

	/**
	 * Sets whether model strings should be escaped.
	 * 
	 * @param escapeMarkup
	 *            True is model strings should be escaped
	 * @return This
	 */
	public final Component setEscapeModelStrings(final boolean escapeMarkup)
	{
		setFlag(FLAG_ESCAPE_MODEL_STRINGS, escapeMarkup);
		return this;
	}

	/**
	 * Set markup ID, which must be String or Integer
	 * 
	 * @param markupId
	 */
	public final void setMarkupIdImpl(Object markupId)
	{
		if (markupId != null && !(markupId instanceof String) && !(markupId instanceof Integer))
		{
			throw new IllegalArgumentException("markupId must be String or Integer");
		}

		setOutputMarkupId(true);
		if (markupId instanceof Integer)
		{
			generatedMarkupId = (Integer)markupId;
			setMetaData(MARKUP_ID_KEY, null);
			return;
		}

		generatedMarkupId = -1;
		setMetaData(MARKUP_ID_KEY, (String)markupId);

	}

	/**
	 * Copy markupId
	 * 
	 * @param comp
	 */
	final void setMarkupId(Component comp)
	{
		Args.notNull(comp, "comp");

		generatedMarkupId = comp.generatedMarkupId;
		setMetaData(MARKUP_ID_KEY, comp.getMetaData(MARKUP_ID_KEY));
		if (comp.getOutputMarkupId())
		{
			setOutputMarkupId(true);
		}
	}

	/**
	 * Sets this component's markup id to a user defined value. It is up to the user to ensure this
	 * value is unique.
	 * <p>
	 * The recommended way is to let wicket generate the value automatically, this method is here to
	 * serve as an override for that value in cases where a specific id must be used.
	 * <p>
	 * If null is passed in the user defined value is cleared and markup id value will fall back on
	 * automatically generated value
	 * 
	 * @see #getMarkupId()
	 * 
	 * @param markupId
	 *            markup id value or null to clear any previous user defined value
	 * @return this for chaining
	 */
	public Component setMarkupId(String markupId)
	{
		Args.notEmpty(markupId, "markupId");

		setMarkupIdImpl(markupId);
		return this;
	}

	/**
	 * Sets the metadata for this component using the given key. If the metadata object is not of
	 * the correct type for the metadata key, an IllegalArgumentException will be thrown. For
	 * information on creating MetaDataKeys, see {@link MetaDataKey}.
	 * 
	 * @param <M>
	 *            The type of the metadata
	 * 
	 * @param key
	 *            The singleton key for the metadata
	 * @param object
	 *            The metadata object
	 * @throws IllegalArgumentException
	 * @see MetaDataKey
	 */
	public final <M extends Serializable> Component setMetaData(final MetaDataKey<M> key, final M object)
	{
		MetaDataEntry<?>[] old = getMetaData();

		Object metaData = null;
		MetaDataEntry<?>[] metaDataArray = key.set(getMetaData(), object);
		if (metaDataArray != null && metaDataArray.length > 0)
		{
			metaData = (metaDataArray.length > 1) ? metaDataArray : metaDataArray[0];
		}

		int index = getFlag(FLAG_MODEL_SET) ? 1 : 0;

		if (old == null && metaData != null)
		{
			data_insert(index, metaData);
		}
		else if (old != null && metaData != null)
		{
			data_set(index, metaData);
		}
		else if (old != null && metaData == null)
		{
			data_remove(index);
		}
		return this;
	}

	/**
	 * Sets the given model.
	 * <p>
	 * WARNING: DO NOT OVERRIDE THIS METHOD UNLESS YOU HAVE A VERY GOOD REASON FOR IT. OVERRIDING
	 * THIS MIGHT OPEN UP SECURITY LEAKS AND BREAK BACK-BUTTON SUPPORT.
	 * </p>
	 * 
	 * @param model
	 *            The model
	 * @return This
	 */
	public Component setDefaultModel(final IModel<?> model)
	{
		IModel<?> prevModel = getModelImpl();

		IModel<?> wrappedModel = prevModel;
		if (prevModel instanceof IWrapModel)
		{
			wrappedModel = ((IWrapModel<?>)prevModel).getWrappedModel();
		}

		// Change model
		if (wrappedModel != model)
		{
			// Detach the old/current model
			if (prevModel != null)
			{
				prevModel.detach();
			}

			modelChanging();
			setModelImpl(wrap(model));
			modelChanged();

			// WICKET-3413 reset 'inherited model' when model is explicitely set
			setFlag(FLAG_INHERITABLE_MODEL, false);
		}

		return this;
	}

	/**
	 * @return model
	 */
	IModel<?> getModelImpl()
	{
		if (getFlag(FLAG_MODEL_SET))
		{
			return (IModel<?>)data_get(0);
		}
		return null;
	}

	/**
	 * 
	 * @param model
	 */
	void setModelImpl(IModel<?> model)
	{
		if (getFlag(FLAG_MODEL_SET))
		{
			if (model != null)
			{
				data_set(0, model);
			}
			else
			{
				data_remove(0);
				setFlag(FLAG_MODEL_SET, false);
			}
		}
		else
		{
			if (model != null)
			{
				data_insert(0, model);
				setFlag(FLAG_MODEL_SET, true);
			}
		}
	}

	/**
	 * Sets the backing model object. Unlike <code>getDefaultModel().setObject(object)</code>, this
	 * method checks authorisation and model comparator, and invokes <code>modelChanging</code> and
	 * <code>modelChanged</code> if the value really changes.
	 * 
	 * @param object
	 *            The object to set
	 * @return This
	 * @throws IllegalStateException If the component has neither its own model nor any of its
	 * parents uses {@link IComponentInheritedModel}
	 */
	@SuppressWarnings("unchecked")
	public final Component setDefaultModelObject(final Object object)
	{
		final IModel<Object> model = (IModel<Object>)getDefaultModel();

		// Check whether anything can be set at all
		if (model == null)
		{
			throw new IllegalStateException(
				"Attempt to set a model object on a component without a model! " +
				"Either pass an IModel to the constructor or use #setDefaultModel(new SomeModel(object)). " +
				"Component: " + getPageRelativePath());
		}

		// Check authorization
		if (!isActionAuthorized(ENABLE))
		{
			throw new UnauthorizedActionException(this, ENABLE);
		}

		// Check whether this will result in an actual change
		if (!getModelComparator().compare(this, object))
		{
			modelChanging();
			model.setObject(object);
			modelChanged();
		}

		return this;
	}

	/**
	 * Sets whether or not component will output id attribute into the markup. id attribute will be
	 * set to the value returned from {@link Component#getMarkupId()}.
	 * 
	 * @param output
	 *            True if the component will output the id attribute into markup. Please note that
	 *            the default behavior is to use the same id as the component. This means that your
	 *            component must begin with [a-zA-Z] in order to generate a valid markup id
	 *            according to: http://www.w3.org/TR/html401/types.html#type-name
	 * 
	 * @return this for chaining
	 */
	public final Component setOutputMarkupId(final boolean output)
	{
		setFlag(FLAG_OUTPUT_MARKUP_ID, output);
		return this;
	}

	/**
	 * Render a placeholder tag when the component is not visible. The tag is of form:
	 * &lt;componenttag style="display:none;" id="markupid"/&gt;. This method will also call
	 * <code>setOutputMarkupId(true)</code>.
	 * 
	 * This is useful, for example, in ajax situations where the component starts out invisible and
	 * then becomes visible through an ajax update. With a placeholder tag already in the markup you
	 * do not need to repaint this component's parent, instead you can repaint the component
	 * directly.
	 * 
	 * When this method is called with parameter <code>false</code> the outputmarkupid flag is not
	 * reverted to false.
	 * 
	 * @param outputTag
	 * @return this for chaining
	 */
	public final Component setOutputMarkupPlaceholderTag(final boolean outputTag)
	{
		if (outputTag != getFlag(FLAG_PLACEHOLDER))
		{
			if (outputTag)
			{
				setOutputMarkupId(true);
				setFlag(FLAG_PLACEHOLDER, true);
			}
			else
			{
				setFlag(FLAG_PLACEHOLDER, false);
				// I think it's better to not setOutputMarkupId to false...
				// user can do it if she want
			}
		}
		return this;
	}

	/**
	 * If false the component's tag will be printed as well as its body (which is default). If true
	 * only the body will be printed, but not the component's tag.
	 * 
	 * @param renderTag
	 *            If true, the component tag will not be printed
	 * @return This
	 */
	public final Component setRenderBodyOnly(final boolean renderTag)
	{
		setFlag(FLAG_RENDER_BODY_ONLY, renderTag);
		return this;
	}

	/**
	 * Sets the page that will respond to this request
	 * 
	 * @param <C>
	 * 
	 * @param cls
	 *            The response page class
	 * @see RequestCycle#setResponsePage(Class)
	 */
	public final <C extends IRequestablePage> void setResponsePage(final Class<C> cls)
	{
		getRequestCycle().setResponsePage(cls, (PageParameters)null);
	}

	/**
	 * Sets the page class and its parameters that will respond to this request
	 * 
	 * @param <C>
	 * 
	 * @param cls
	 *            The response page class
	 * @param parameters
	 *            The parameters for this bookmarkable page.
	 * @see RequestCycle#setResponsePage(Class, PageParameters)
	 */
	public final <C extends IRequestablePage> void setResponsePage(final Class<C> cls,
		PageParameters parameters)
	{
		getRequestCycle().setResponsePage(cls, parameters);
	}

	/**
	 * Sets the page that will respond to this request
	 * 
	 * @param page
	 *            The response page
	 * 
	 * @see RequestCycle#setResponsePage(org.apache.wicket.request.component.IRequestablePage)
	 */
	public final void setResponsePage(final IRequestablePage page)
	{
		getRequestCycle().setResponsePage(page);
	}

	/**
	 * @param versioned
	 *            True to turn on versioning for this component, false to turn it off for this
	 *            component and any children.
	 * @return This
	 */
	public Component setVersioned(boolean versioned)
	{
		setFlag(FLAG_VERSIONED, versioned);
		return this;
	}

	/**
	 * Sets whether this component and any children are visible.
	 * 
	 * @param visible
	 *            True if this component and any children should be visible
	 * @return This
	 */
	public final Component setVisible(final boolean visible)
	{
		// Is new visibility state a change?
		if (visible != getFlag(FLAG_VISIBLE))
		{
			// record component's visibility change
			addStateChange();

			// Change visibility
			setFlag(FLAG_VISIBLE, visible);
			onVisibleStateChanged();
		}
		return this;
	}

	void clearVisibleInHierarchyCache()
	{
		setRequestFlag(RFLAG_VISIBLE_IN_HIERARCHY_SET, false);
	}

	void onVisibleStateChanged()
	{
		clearVisibleInHierarchyCache();
	}


	/**
	 * Gets the string representation of this component.
	 * 
	 * @return The path to this component
	 */
	@Override
	public String toString()
	{
		return toString(false);
	}

	/**
	 * @param detailed
	 *            True if a detailed string is desired
	 * @return The string
	 */
	public String toString(final boolean detailed)
	{
		try
		{
			final StringBuilder buffer = new StringBuilder();
			buffer.append("[Component id = ").append(getId());

			if (detailed)
			{
				final Page page = findPage();
				if (page == null)
				{
					buffer.append(", page = <No Page>, path = ")
						.append(getPath())
						.append('.')
						.append(Classes.simpleName(getClass()));
				}
				else
				{
					buffer.append(", page = ")
						.append(Classes.name(getPage().getPageClass()))
						.append(", path = ")
						.append(getPageRelativePath())
						.append(", type = ")
						.append(Classes.name(getClass()))
						.append(", isVisible = ")
						.append((determineVisibility()))
						.append(", isVersioned = ")
						.append(isVersioned());
				}

				if (markup != null)
				{
					buffer.append(", markup = ").append(new MarkupStream(getMarkup()).toString());
				}
			}

			buffer.append(']');

			return buffer.toString();
		}
		catch (Exception e)
		{
			log.warn("Error while building toString()", e);
			return String.format(
				"[Component id = %s <attributes are not available because exception %s was thrown during toString()>]",
				getId(), e.getClass().getName());
		}
	}

	/**
	 * Returns a bookmarkable URL that references a given page class using a given set of page
	 * parameters. Since the URL which is returned contains all information necessary to instantiate
	 * and render the page, it can be stored in a user's browser as a stable bookmark.
	 * 
	 * @param <C>
	 * 
	 * @see RequestCycle#urlFor(Class, org.apache.wicket.request.mapper.parameter.PageParameters)
	 * 
	 * @param pageClass
	 *            Class of page
	 * @param parameters
	 *            Parameters to page
	 * @return Bookmarkable URL to page
	 */
	public final <C extends Page> CharSequence urlFor(final Class<C> pageClass,
		final PageParameters parameters)
	{
		return getRequestCycle().urlFor(pageClass, parameters);
	}

	/**
	 * Gets a URL for the listener interface on a behavior (e.g. IBehaviorListener on
	 * AjaxPagingNavigationBehavior).
	 * 
	 * @param behaviour
	 *            The behavior that the URL should point to
	 * @param listener
	 *            The listener interface that the URL should call
	 * @param parameters
	 *            The parameters that should be rendered into the urls
	 * @return The URL
	 */
	public final CharSequence urlFor(final Behavior behaviour,
		final RequestListenerInterface listener, final PageParameters parameters)
	{
		int id = getBehaviorId(behaviour);
		IRequestHandler handler = createRequestHandler(listener, parameters, id);
		return getRequestCycle().urlFor(handler);
	}

	/**
	 * Create a suitable request handler depending whether the page is stateless or bookmarkable.
	 */
	private IRequestHandler createRequestHandler(RequestListenerInterface listener,
		PageParameters parameters, Integer id)
	{
		Page page = getPage();

		PageAndComponentProvider provider = new PageAndComponentProvider(page, this, parameters);

		if (page.isPageStateless()
			|| (page.isBookmarkable() && page.wasCreatedBookmarkable()))
		{
			return new BookmarkableListenerInterfaceRequestHandler(provider, listener, id);
		}
		else
		{
			return new ListenerInterfaceRequestHandler(provider, listener, id);
		}
	}

	/**
	 * Returns a URL that references the given request target.
	 * 
	 * @see RequestCycle#urlFor(IRequestHandler)
	 * 
	 * @param requestHandler
	 *            the request target to reference
	 * 
	 * @return a URL that references the given request target
	 */
	public final CharSequence urlFor(final IRequestHandler requestHandler)
	{
		return getRequestCycle().urlFor(requestHandler);
	}

	/**
	 * Gets a URL for the listener interface (e.g. ILinkListener).
	 * 
	 * @see RequestCycle#urlFor(IRequestHandler)
	 * 
	 * @param listener
	 *            The listener interface that the URL should call
	 * @param parameters
	 *            The parameters that should be rendered into the urls
	 * @return The URL
	 */
	public final CharSequence urlFor(final RequestListenerInterface listener,
		final PageParameters parameters)
	{
		IRequestHandler handler = createRequestHandler(listener, parameters, null);
		return getRequestCycle().urlFor(handler);
	}

	/**
	 * Returns a URL that references a shared resource through the provided resource reference.
	 * 
	 * @see RequestCycle#urlFor(IRequestHandler)
	 * 
	 * @param resourceReference
	 *            The resource reference
	 * @param parameters
	 *            parameters or {@code null} if none
	 * @return The url for the shared resource
	 */
	public final CharSequence urlFor(final ResourceReference resourceReference,
		PageParameters parameters)
	{
		return getRequestCycle().urlFor(resourceReference, parameters);
	}

	/**
	 * Traverses all parent components of the given class in this parentClass, calling the visitor's
	 * visit method at each one.
	 * 
	 * @param <R>
	 *            the type of the result object
	 * @param parentClass
	 *            Class
	 * @param visitor
	 *            The visitor to call at each parent of the given type
	 * @return First non-null value returned by visitor callback
	 */
	public final <R, C extends MarkupContainer> R visitParents(final Class<C> parentClass,
		final IVisitor<C, R> visitor)
	{
		return visitParents(parentClass, visitor, IVisitFilter.ANY);
	}

	/**
	 * Traverses all parent components of the given class in this parentClass, calling the visitor's
	 * visit method at each one.
	 * 
	 * @param <R>
	 *            the type of the result object
	 * @param parentClass
	 *            the class of the parent component
	 * @param visitor
	 *            The visitor to call at each parent of the given type
	 * @param filter
	 *            a filter that adds an additional logic to the condition whether a parent container
	 *            matches
	 * @return First non-null value returned by visitor callback
	 */
	@SuppressWarnings("unchecked")
	public final <R, C extends MarkupContainer> R visitParents(final Class<C> parentClass,
		final IVisitor<C, R> visitor, IVisitFilter filter)
	{
		Args.notNull(filter, "filter");

		// Start here
		MarkupContainer current = getParent();

		Visit<R> visit = new Visit<R>();

		// Walk up containment hierarchy
		while (current != null)
		{
			// Is current an instance of this class?
			if (parentClass.isInstance(current) && filter.visitObject(current))
			{
				visitor.component((C)current, visit);
				if (visit.isStopped())
				{
					return visit.getResult();
				}
			}

			// Check parent
			current = current.getParent();
		}
		return null;
	}

	/**
	 * Registers a warning feedback message for this component.
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void warn(final Serializable message)
	{
		getFeedbackMessages().warn(this, message);
		addStateChange();
	}

	/**
	 * {@link Behavior#beforeRender(Component)} Notify all behaviors that are assigned to this
	 * component that the component is about to be rendered.
	 */
	private void notifyBehaviorsComponentBeforeRender()
	{
		for (Behavior behavior : getBehaviors())
		{
			if (isBehaviorAccepted(behavior))
			{
				behavior.beforeRender(this);
			}
		}
	}

	/**
	 * {@link Behavior#afterRender(Component)} Notify all behaviors that are assigned to this
	 * component that the component has rendered.
	 */
	private void notifyBehaviorsComponentRendered()
	{
		// notify the behaviors that component has been rendered
		for (Behavior behavior : getBehaviors())
		{
			if (isBehaviorAccepted(behavior))
			{
				behavior.afterRender(this);
			}
		}
	}

	protected final void addStateChange()
	{
		checkHierarchyChange(this);
		final Page page = findPage();
		if (page != null)
		{
			page.componentStateChanging(this);
		}
	}

	/**
	 * Checks whether the given type has the expected name.
	 * 
	 * @param tag
	 *            The tag to check
	 * @param name
	 *            The expected tag name
	 * @throws MarkupException
	 *             Thrown if the tag is not of the right name
	 */
	protected final void checkComponentTag(final ComponentTag tag, final String name)
	{
		if (!tag.getName().equalsIgnoreCase(name))
		{
			String msg = String.format("Component [%s] (path = [%s]) must be "
				+ "applied to a tag of type [%s], not: %s", getId(), getPath(), name,
				tag.toUserDebugString());

			findMarkupStream().throwMarkupException(msg);
		}
	}

	/**
	 * Checks that a given tag has a required attribute value.
	 * 
	 * @param tag
	 *            The tag
	 * @param key
	 *            The attribute key
	 * @param values
	 *            The required value for the attribute key
	 * @throws MarkupException
	 *             Thrown if the tag does not have the required attribute value
	 */
	protected final void checkComponentTagAttribute(final ComponentTag tag, final String key,
		final String... values)
	{
		if (key != null)
		{
			final String tagAttributeValue = tag.getAttributes().getString(key);

			boolean found = false;
			if (tagAttributeValue != null)
			{
				for (String value : values)
				{
					if (value.equalsIgnoreCase(tagAttributeValue))
					{
						found = true;
						break;
					}
				}
			}

			if (found == false)
			{
				String msg = String.format("Component [%s] (path = [%s]) must be applied to a tag "
						+ "with [%s] attribute matching any of %s, not [%s]", getId(), getPath(), key,
						Arrays.toString(values), tagAttributeValue);

				findMarkupStream().throwMarkupException(msg);
			}
		}
	}

	/**
	 * Checks whether the hierarchy may be changed at all, and throws an exception if this is not
	 * the case.
	 * 
	 * @param component
	 *            the component which is about to be added or removed
	 */
	protected void checkHierarchyChange(final Component component)
	{
		// Throw exception if modification is attempted during rendering
		if (getFlag(FLAG_RENDERING) && !component.isAuto())
		{
			throw new WicketRuntimeException(
				"Cannot modify component hierarchy after render phase has started (page version cant change then anymore)");
		}
	}

	/**
	 * Detaches the model for this component if it is detachable.
	 */
	protected void detachModel()
	{
		IModel<?> model = getModelImpl();
		if (model != null)
		{
			model.detach();
		}
		// also detach the wrapped model of a component assigned wrap (not
		// inherited)
		if (model instanceof IWrapModel && !getFlag(FLAG_INHERITABLE_MODEL))
		{
			((IWrapModel<?>)model).getWrappedModel().detach();
		}
	}

	/**
	 * Suffixes an exception message with useful information about this. component.
	 * 
	 * @param message
	 *            The message
	 * @return The modified message
	 */
	protected final String exceptionMessage(final String message)
	{
		return message + ":\n" + toString();
	}

	/**
	 * Finds the markup stream for this component.
	 * 
	 * @return The markup stream for this component. Since a Component cannot have a markup stream,
	 *         we ask this component's parent to search for it.
	 */
	protected final MarkupStream findMarkupStream()
	{
		return new MarkupStream(getMarkup());
	}

	/**
	 * If this Component is a Page, returns self. Otherwise, searches for the nearest Page parent in
	 * the component hierarchy. If no Page parent can be found, {@code null} is returned.
	 * 
	 * @return The Page or {@code null} if none can be found
	 */
	protected final Page findPage()
	{
		// Search for page
		return (Page)(this instanceof Page ? this : findParent(Page.class));
	}

	/**
	 * Gets the subset of the currently coupled {@link Behavior}s that are of the provided type as
	 * an unmodifiable list. Returns an empty list if there are no behaviors coupled to this
	 * component.
	 * 
	 * @param type
	 *            The type or null for all
	 * @return The subset of the currently coupled behaviors that are of the provided type as an
	 *         unmodifiable list
	 * @param <M>
	 *            A class derived from Behavior
	 */
	public <M extends Behavior> List<M> getBehaviors(Class<M> type)
	{
		return new Behaviors(this).getBehaviors(type);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * @param flag
	 *            The flag to test
	 * @return True if the flag is set
	 */
	protected final boolean getFlag(final int flag)
	{
		return (flags & flag) != 0;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * @param flag
	 *            The flag to test
	 * @return True if the flag is set
	 */
	protected final boolean getRequestFlag(final short flag)
	{
		return (requestFlags & flag) != 0;
	}

	/**
	 * Finds the innermost IModel object for an IModel that might contain nested IModel(s).
	 * 
	 * @param model
	 *            The model
	 * @return The innermost (most nested) model
	 */
	protected final IModel<?> getInnermostModel(final IModel<?> model)
	{
		IModel<?> nested = model;
		while (nested != null && nested instanceof IWrapModel)
		{
			final IModel<?> next = ((IWrapModel<?>)nested).getWrappedModel();
			if (nested == next)
			{
				throw new WicketRuntimeException("Model for " + nested + " is self-referential");
			}
			nested = next;
		}
		return nested;
	}

	/**
	 * Gets the component's current model comparator. Implementations can be used for testing the
	 * current value of the components model data with the new value that is given.
	 * 
	 * @return the value defaultModelComparator
	 */
	public IModelComparator getModelComparator()
	{
		return defaultModelComparator;
	}

	/**
	 * Returns whether the component can be stateless. Also the component behaviors must be
	 * stateless, otherwise the component will be treat as stateful. In order for page to be
	 * stateless (and not to be stored in session), all components (and component behaviors) must be
	 * stateless.
	 * 
	 * @return whether the component can be stateless
	 */
	protected boolean getStatelessHint()
	{
		return true;
	}

	/**
	 * Called when a null model is about to be retrieved in order to allow a subclass to provide an
	 * initial model.
	 * <p>
	 * By default this implementation looks components in the parent chain owning a
	 * {@link IComponentInheritedModel} to provide a model for this component via
	 * {@link IComponentInheritedModel#wrapOnInheritance(Component)}.
	 * <p>
	 * For example a {@link FormComponent} has the opportunity to instantiate a model on the fly
	 * using its {@code id} and the containing {@link Form}'s model, if the form holds a
	 * {@link CompoundPropertyModel}.
	 * 
	 * @return The model
	 */
	protected IModel<?> initModel()
	{
		IModel<?> foundModel = null;
		// Search parents for IComponentInheritedModel (i.e. CompoundPropertyModel)
		for (Component current = getParent(); current != null; current = current.getParent())
		{
			// Get model
			// Don't call the getModel() that could initialize many in between
			// completely useless models.
			// IModel model = current.getDefaultModel();
			IModel<?> model = current.getModelImpl();

			if (model instanceof IWrapModel && !(model instanceof IComponentInheritedModel))
			{
				model = ((IWrapModel<?>)model).getWrappedModel();
			}

			if (model instanceof IComponentInheritedModel)
			{
				// return the shared inherited
				foundModel = ((IComponentInheritedModel<?>)model).wrapOnInheritance(this);
				setFlag(FLAG_INHERITABLE_MODEL, true);
				break;
			}
		}

		// No model for this component!
		return foundModel;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR OVERRIDE.
	 * 
	 * <p>
	 * Called anytime a model is changed via setModel or setModelObject.
	 * </p>
	 */
	protected void internalOnModelChanged()
	{
	}

	/**
	 * Components are allowed to reject behavior modifiers.
	 * 
	 * @param behavior
	 * @return False, if the component should not apply this behavior
	 */
	protected boolean isBehaviorAccepted(final Behavior behavior)
	{
		// Ignore AttributeModifiers when FLAG_IGNORE_ATTRIBUTE_MODIFIER is set
		if ((behavior instanceof AttributeModifier) &&
			(getFlag(FLAG_IGNORE_ATTRIBUTE_MODIFIER) != false))
		{
			return false;
		}

		return behavior.isEnabled(this);
	}

	/**
	 * If true, all attribute modifiers will be ignored
	 * 
	 * @return True, if attribute modifiers are to be ignored
	 */
	protected final boolean isIgnoreAttributeModifier()
	{
		return getFlag(FLAG_IGNORE_ATTRIBUTE_MODIFIER);
	}

	/**
	 * Called just after a component is rendered.
	 */
	protected void onAfterRender()
	{
		setFlag(FLAG_AFTER_RENDERING, false);
	}

	/**
	 * Called just before a component is rendered only if the component is visible.
	 * <p>
	 * <strong>NOTE</strong>: If you override this, you *must* call super.onBeforeRender() within
	 * your implementation.
	 * 
	 * Because this method is responsible for cascading {@link #onBeforeRender()} call to its
	 * children it is strongly recommended that super call is made at the end of the override.
	 * </p>
	 *
	 * Changes to the component tree can be made only <strong>before</strong> calling
	 * super.onBeforeRender().
	 *
	 * @see org.apache.wicket.MarkupContainer#addOrReplace(Component...) 
	 */
	protected void onBeforeRender()
	{
		setFlag(FLAG_PREPARED_FOR_RENDER, true);
		onBeforeRenderChildren();
		setRequestFlag(RFLAG_BEFORE_RENDER_SUPER_CALL_VERIFIED, true);
	}

	/**
	 * Processes the component tag.
	 * 
	 * Overrides of this method most likely should call the super implementation.
	 * 
	 * @param tag
	 *            Tag to modify
	 */
	@SuppressWarnings("deprecation")
	protected void onComponentTag(final ComponentTag tag)
	{
		// We can't try to get the ID from markup. This could be different than
		// id returned from getMarkupId() prior first rendering the component
		// (due to transparent resolvers and borders which break the 1:1
		// component <-> markup relation)
		if (getFlag(FLAG_OUTPUT_MARKUP_ID))
		{
			tag.putInternal(MARKUP_ID_ATTR_NAME, getMarkupId());
		}

		DebugSettings debugSettings = getApplication().getDebugSettings();
		String componentPathAttributeName = debugSettings.getComponentPathAttributeName();
		if (Strings.isEmpty(componentPathAttributeName) && debugSettings.isOutputComponentPath())
		{
			// fallback to the old 'wicketpath'
			componentPathAttributeName = "wicketpath";
		}
		if (Strings.isEmpty(componentPathAttributeName) == false)
		{
			String path = getPageRelativePath();
			path = path.replace("_", "__");
			path = path.replace(':', '_');
			tag.put(componentPathAttributeName, path);
		}

		// The markup sourcing strategy may also want to work on the tag
		getMarkupSourcingStrategy().onComponentTag(this, tag);
	}

	/**
	 * Processes the body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 */
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
	}

	/**
	 * Called to allow a component to detach resources after use.
	 * 
	 * Overrides of this method MUST call the super implementation, the most logical place to do
	 * this is the last line of the override method.
	 */
	protected void onDetach()
	{
		setFlag(FLAG_DETACHING, false);
	}

	/**
	 * Called to notify the component it is being removed from the component hierarchy
	 * 
	 * Overrides of this method MUST call the super implementation, the most logical place to do
	 * this is the last line of the override method.
	 */
	protected void onRemove()
	{
		setFlag(FLAG_REMOVING_FROM_HIERARCHY, false);
	}

	/**
	 * Called anytime a model is changed after the change has occurred
	 */
	protected void onModelChanged()
	{
	}

	/**
	 * Called anytime a model is changed, but before the change actually occurs
	 */
	protected void onModelChanging()
	{
	}

	/**
	 * Implementation that renders this component.
	 */
	protected abstract void onRender();

	/**
	 * Writes a simple tag out to the response stream. Any components that might be referenced by
	 * the tag are ignored. Also undertakes any tag attribute modifications if they have been added
	 * to the component.
	 * 
	 * @param tag
	 *            The tag to write
	 */
	protected final void renderComponentTag(ComponentTag tag)
	{
		if (needToRenderTag(tag))
		{
			// apply behaviors that are attached to the component tag.
			if (tag.hasBehaviors())
			{
				Iterator<? extends Behavior> tagBehaviors = tag.getBehaviors();
				while (tagBehaviors.hasNext())
				{
					final Behavior behavior = tagBehaviors.next();
					if (behavior.isEnabled(this))
					{
						behavior.onComponentTag(this, tag);
					}
					behavior.detach(this);
				}
			}

			// Apply behavior modifiers
			List<? extends Behavior> behaviors = getBehaviors();
			if ((behaviors != null) && !behaviors.isEmpty() && !tag.isClose() &&
				(isIgnoreAttributeModifier() == false))
			{
				tag = tag.mutable();
				for (Behavior behavior : behaviors)
				{
					// Components may reject some behavior components
					if (isBehaviorAccepted(behavior))
					{
						behavior.onComponentTag(this, tag);
					}
				}
			}

			if ((tag instanceof WicketTag) && !tag.isClose() &&
				!getFlag(FLAG_IGNORE_ATTRIBUTE_MODIFIER))
			{
				ExceptionSettings.NotRenderableErrorStrategy notRenderableErrorStrategy = ExceptionSettings.NotRenderableErrorStrategy.LOG_WARNING;
				if (Application.exists())
				{
					notRenderableErrorStrategy = getApplication().getExceptionSettings().getNotRenderableErrorStrategy();
				}

				String tagName = tag.getNamespace() + ":" + tag.getName();
				String componentId = getId();
				if (getFlag(FLAG_OUTPUT_MARKUP_ID))
				{
					String message = String.format("Markup id set on a component that is usually not rendered into markup. " +
					                               "Markup id: %s, component id: %s, component tag: %s.",
					                               getMarkupId(), componentId, tagName);
					if (notRenderableErrorStrategy == ExceptionSettings.NotRenderableErrorStrategy.THROW_EXCEPTION)
					{
						throw new IllegalStateException(message);
					}
					log.warn(message);
				}
				if (getFlag(FLAG_PLACEHOLDER))
				{
					String message = String.format(
							"Placeholder tag set on a component that is usually not rendered into markup. " +
							"Component id: %s, component tag: %s.", componentId, tagName);
					if (notRenderableErrorStrategy == ExceptionSettings.NotRenderableErrorStrategy.THROW_EXCEPTION)
					{
						throw new IllegalStateException(message);
					}
					log.warn(message);
				}
			}

			// Write the tag
			tag.writeOutput(getResponse(), !needToRenderTag(null),
				getMarkup().getMarkupResourceStream().getWicketNamespace());
		}
	}

	/**
	 * Replaces the body with the given one.
	 * 
	 * @param markupStream
	 *            The markup stream to replace the tag body in
	 * @param tag
	 *            The tag
	 * @param body
	 *            The new markup
	 */
	protected final void replaceComponentTagBody(final MarkupStream markupStream,
		final ComponentTag tag, final CharSequence body)
	{
		// The tag might have been changed from open-close to open. Hence
		// we'll need what was in the markup itself
		ComponentTag markupOpenTag = null;

		// If tag has a body
		if (tag.isOpen())
		{
			// Get what tag was in the markup; not what the user it might
			// have changed it to.
			markupOpenTag = markupStream.getPreviousTag();

			// If it was an open tag in the markup as well, than ...
			if (markupOpenTag.isOpen())
			{
				// skip any raw markup in the body
				markupStream.skipRawMarkup();
			}
		}

		if (body != null)
		{
			// Write the new body
			getResponse().write(body);
		}

		// If we had an open tag (and not an openclose tag) and we found a
		// close tag, we're good
		if (tag.isOpen())
		{
			// If it was an open tag in the markup, than there must be
			// a close tag as well.
			if ((markupOpenTag != null) && markupOpenTag.isOpen() && !markupStream.atCloseTag())
			{
				// There must be a component in this discarded body
				markupStream.throwMarkupException("Expected close tag for '" + markupOpenTag +
					"' Possible attempt to embed component(s) '" + markupStream.get() +
					"' in the body of this component which discards its body");
			}
		}
	}

	/**
	 * @param auto
	 *            True to put component into auto-add mode
	 */
	protected final Component setAuto(final boolean auto)
	{
		setFlag(FLAG_AUTO, auto);
		return this;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * @param flag
	 *            The flag to set
	 * @param set
	 *            True to turn the flag on, false to turn it off
	 */
	protected final Component setFlag(final int flag, final boolean set)
	{
		if (set)
		{
			flags |= flag;
		}
		else
		{
			flags &= ~flag;
		}
		return this;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * @param flag
	 *            The flag to set
	 * @param set
	 *            True to turn the flag on, false to turn it off
	 */
	protected final Component setRequestFlag(final short flag, final boolean set)
	{
		if (set)
		{
			requestFlags |= flag;
		}
		else
		{
			requestFlags &= ~flag;
		}
		return this;
	}

	/**
	 * If true, all attribute modifiers will be ignored
	 * 
	 * @param ignore
	 *            If true, all attribute modifiers will be ignored
	 * @return This
	 */
	protected final Component setIgnoreAttributeModifier(final boolean ignore)
	{
		setFlag(FLAG_IGNORE_ATTRIBUTE_MODIFIER, ignore);
		return this;
	}

	/**
	 * @param <V>
	 *            The model type
	 * @param model
	 *            The model to wrap if need be
	 * @return The wrapped model
	 */
	protected final <V> IModel<V> wrap(final IModel<V> model)
	{
		if (model instanceof IComponentAssignedModel)
		{
			return ((IComponentAssignedModel<V>)model).wrapOnAssignment(this);
		}
		return model;
	}

	/**
	 * Detaches any child components
	 */
	void detachChildren()
	{
	}

	/**
	 * Signals this components removal from hierarchy to all its children.
	 */
	void removeChildren()
	{
	}

	/**
	 * Gets the component at the given path.
	 * 
	 * @param path
	 *            Path to component
	 * @return The component at the path
	 */
	@Override
	public Component get(final String path)
	{
		// Path to this component is an empty path
		if (path.length() == 0)
		{
			return this;
		}
		throw new IllegalArgumentException(
			exceptionMessage("Component is not a container and so does not contain the path " +
				path));
	}

	/**
	 * @param setRenderingFlag
	 *            rendering flag
	 */
	void internalMarkRendering(boolean setRenderingFlag)
	{
		// WICKET-5460 no longer prepared for render
		setFlag(FLAG_PREPARED_FOR_RENDER, false);

		setFlag(FLAG_RENDERING, setRenderingFlag);
	}

	/**
	 * @return True if this component or any of its parents is in auto-add mode
	 */
	public final boolean isAuto()
	{
		// Search up hierarchy for FLAG_AUTO
		for (Component current = this; current != null; current = current.getParent())
		{
			if (current.getFlag(FLAG_AUTO))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return <code>true</code> if component has been prepared for render
	 */
	boolean isPreparedForRender()
	{
		return getFlag(FLAG_PREPARED_FOR_RENDER);
	}

	/**
	 * 
	 */
	protected void onAfterRenderChildren()
	{
	}

	/**
	 * This method is here for {@link MarkupContainer}. It is broken out of
	 * {@link #onBeforeRender()} so we can guarantee that it executes as the last in
	 * onBeforeRender() chain no matter where user places the <code>super.onBeforeRender()</code>
	 * call.
	 */
	void onBeforeRenderChildren()
	{
	}

	/**
	 * Renders the close tag at the current position in the markup stream.
	 * 
	 * @param markupStream
	 *            the markup stream
	 * @param openTag
	 *            the tag to render
	 * @param renderBodyOnly
	 *            if true, the tag will not be written to the output
	 */
	final void renderClosingComponentTag(final MarkupStream markupStream,
		final ComponentTag openTag, final boolean renderBodyOnly)
	{
		// Tag should be open tag and not openclose tag
		if (openTag.isOpen())
		{
			// If we found a close tag and it closes the open tag, we're good
			if (markupStream.atCloseTag() && markupStream.getTag().closes(openTag))
			{
				// Render the close tag
				if ((renderBodyOnly == false) && needToRenderTag(openTag))
				{
					getResponse().write(openTag.syntheticCloseTagString());
				}
			}
			else if (openTag.requiresCloseTag())
			{
				// Missing close tag. Some tags, e.g. <p> are handled like <p/> by most browsers and
				// thus will not throw an exception.
				markupStream.throwMarkupException("Expected close tag for " + openTag);
			}
		}
	}

	/**
	 * Sets the id of this component.
	 * 
	 * @param id
	 *            The non-null id of this component
	 */
	final Component setId(final String id)
	{
		if (!(this instanceof Page))
		{
			if (Strings.isEmpty(id))
			{
				throw new WicketRuntimeException("Null or empty component ID's are not allowed.");
			}
		}

		if ((id != null) && (id.indexOf(':') != -1 || id.indexOf('~') != -1))
		{
			throw new WicketRuntimeException("The component ID must not contain ':' or '~' chars.");
		}

		this.id = id;
		return this;
	}

	/**
	 * THIS IS A WICKET INTERNAL API. DO NOT USE IT.
	 * 
	 * Sets the parent of a component. Typically what you really want is parent.add(child).
	 * <p/>
	 * Note that calling setParent() and not parent.add() will connect the child to the parent, but
	 * the parent will not know the child. This might not be a problem in some cases, but e.g.
	 * child.onDetach() will not be invoked (since the parent doesn't know it is his child).
	 * 
	 * @param parent
	 *            The parent container
	 */
	public final void setParent(final MarkupContainer parent)
	{
		if (this.parent != null && log.isDebugEnabled())
		{
			log.debug("Replacing parent " + this.parent + " with " + parent);
		}
		this.parent = parent;
	}

	/**
	 * Sets the render allowed flag.
	 * 
	 * @param renderAllowed
	 */
	final void setRenderAllowed(boolean renderAllowed)
	{
		setFlag(FLAG_IS_RENDER_ALLOWED, renderAllowed);
	}

	/**
	 * Sets the render allowed flag.
	 * 
	 * Visit all this page's children (overridden in MarkupContainer) to check rendering
	 * authorization, as appropriate. We set any result; positive or negative as a temporary boolean
	 * in the components, and when a authorization exception is thrown it will block the rendering
	 * of this page
	 */
	void setRenderAllowed()
	{
		setRenderAllowed(isActionAuthorized(RENDER));
	}

	/**
	 * Sets whether or not this component is allowed to be visible. This method is meant to be used
	 * by components to control visibility of other components. A call to
	 * {@link #setVisible(boolean)} will not always have a desired effect because that component may
	 * have {@link #isVisible()} overridden. Both {@link #setVisibilityAllowed(boolean)} and
	 * {@link #isVisibilityAllowed()} are <code>final</code> so their contract is enforced always.
	 * 
	 * @param allowed
	 * @return <code>this</code> for chaining
	 */
	public final Component setVisibilityAllowed(boolean allowed)
	{
		setFlag(FLAG_VISIBILITY_ALLOWED, allowed);
		return this;
	}

	/**
	 * Gets whether or not visibility is allowed on this component. See
	 * {@link #setVisibilityAllowed(boolean)} for details.
	 * 
	 * @return true if this component is allowed to be visible, false otherwise.
	 */
	public final boolean isVisibilityAllowed()
	{
		return getFlag(FLAG_VISIBILITY_ALLOWED);
	}

	/**
	 * Determines whether or not a component should be visible, taking into account all the factors:
	 * {@link #isVisible()}, {@link #isVisibilityAllowed()}, {@link #isRenderAllowed()}
	 * 
	 * @return <code>true</code> if the component should be visible, <code>false</code> otherwise
	 */
	public final boolean determineVisibility()
	{
		return isVisible() && isRenderAllowed() && isVisibilityAllowed();
	}


	/**
	 * Calculates enabled state of the component taking its hierarchy into account. A component is
	 * enabled iff it is itself enabled ({@link #isEnabled()} and {@link #isEnableAllowed()} both
	 * return <code>true</code>), and all of its parents are enabled.
	 * 
	 * @return <code>true</code> if this component is enabled</code>
	 */
	public boolean isEnabledInHierarchy()
	{
		if (getRequestFlag(RFLAG_ENABLED_IN_HIERARCHY_SET))
		{
			return getRequestFlag(RFLAG_ENABLED_IN_HIERARCHY_VALUE);
		}

		final boolean state;
		Component parent = getParent();
		if (parent != null && !parent.isEnabledInHierarchy())
		{
			state = false;
		}
		else
		{
			state = isEnabled() && isEnableAllowed();
		}

		setRequestFlag(RFLAG_ENABLED_IN_HIERARCHY_SET, true);
		setRequestFlag(RFLAG_ENABLED_IN_HIERARCHY_VALUE, state);
		return state;
	}
	
	/**
	 * Says if the component is rendering or not checking the corresponding flag.
	 * 
	 * @return true if this component is rendering, false otherwise.
	 */
	public final boolean isRendering()
	{
		return getFlag(FLAG_RENDERING);
	}

	/**
	 * Checks whether or not a listener method can be invoked on this component. Usually components
	 * deny these invocations if they are either invisible or disabled in hierarchy. Components can
	 * examine which listener interface is being invoked by examining the declaring class of the
	 * passed in {@literal method} parameter.
	 * <p>
	 * WARNING: be careful when overriding this method because it may open security holes - such as
	 * allowing a user to click on a link that should be disabled.
	 * </p>
	 * <p>
	 * Example usecase for overriding: Suppose you are building an component that displays images.
	 * The component generates a callback to itself using {@link IRequestListener} interface and
	 * uses this callback to stream image data. If such a component is placed inside a disabled
	 * webmarkupcontainer we still want to allow the invocation of the request listener callback
	 * method so that image data can be streamed. Such a component would override this method and
	 * return {@literal true} if the listener method belongs to {@link IRequestListener}.
	 * </p>
	 * 
	 * @param method
	 *            listener method about to be invoked on this component. Could be {@code null} - in
	 *            this case it means <em>any</em> method.
	 * 
	 * @return {@literal true} iff the listener method can be invoked on this component
	 */
	public boolean canCallListenerInterface(Method method)
	{
		return isEnabledInHierarchy() && isVisibleInHierarchy();
	}

	/**
	 * CAUTION: this method is not meant to be overridden like it was in wicket 1.4 when
	 * implementing {@link IHeaderContributor}. overload
	 * {@link Component#renderHead(org.apache.wicket.markup.head.IHeaderResponse)} instead to
	 * contribute to the response header.
	 * 
	 * @param component
	 * @param response
	 */
	public final void renderHead(Component component, IHeaderResponse response)
	{
		if (component != this)
		{
			throw new IllegalStateException(
				"This method is only meant to be invoked on the component where the parameter component==this");
		}
		renderHead(response);
	}

	/**
	 * Render to the web response whatever the component wants to contribute to the head section.
	 * 
	 * @param response
	 *            Response object
	 */
	@Override
	public void renderHead(IHeaderResponse response)
	{
		// noop
	}

	/** {@inheritDoc} */
	@Override
	public void onEvent(IEvent<?> event)
	{
	}

	/** {@inheritDoc} */
	@Override
	public final <T> void send(IEventSink sink, Broadcast type, T payload)
	{
		new ComponentEventSender(this, getApplication().getFrameworkSettings()).send(sink, type,
			payload);
	}

	/**
	 * Removes behavior from component
	 * 
	 * @param behaviors
	 *            behaviors to remove
	 * 
	 * @return this (to allow method call chaining)
	 */
	public Component remove(final Behavior... behaviors)
	{
		Behaviors helper = new Behaviors(this);
		for (Behavior behavior : behaviors)
		{
			helper.remove(behavior);
		}
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public final Behavior getBehaviorById(int id)
	{
		return new Behaviors(this).getBehaviorById(id);
	}

	/** {@inheritDoc} */
	@Override
	public final int getBehaviorId(Behavior behavior)
	{
		return new Behaviors(this).getBehaviorId(behavior);
	}

	/**
	 * Adds a behavior modifier to the component.
	 * 
	 * @param behaviors
	 *            The behavior modifier(s) to be added
	 * @return this (to allow method call chaining)
	 */
	public Component add(final Behavior... behaviors)
	{
		new Behaviors(this).add(behaviors);
		return this;
	}

	/**
	 * Gets the currently coupled {@link Behavior}s as a unmodifiable list. Returns an empty list
	 * rather than null if there are no behaviors coupled to this component.
	 * 
	 * @return The currently coupled behaviors as a unmodifiable list
	 */
	public final List<? extends Behavior> getBehaviors()
	{
		return getBehaviors(null);
	}

	@Override
	public boolean canCallListenerInterfaceAfterExpiry()
	{
        	return getApplication().getPageSettings()
        		.getCallListenerInterfaceAfterExpiry() || isStateless();
	}
	/**
	 * This method is called whenever a component is re-added to the page's component tree, if it
	 * had been removed at some earlier time, i.e., if it is already initialized
	 * (see {@link org.apache.wicket.Component#isInitialized()}).
	 *
	 * This is similar to onInitialize, but only comes after the component has been removed and
	 * then added again:
	 *
	 * <ul>
	 * <li>onInitialize is only called the very first time a component is added</li>
	 * <li>onReAdd is not called the first time, but every time it is re-added after having been
	 * removed</li>
	 * </ul>
	 *
	 * You can think of it as the opposite of onRemove. A component that was once removed will
	 * not be re-initialized but only re-added.
	 *
	 * Subclasses that override this must call super.onReAdd().
	 */
	protected void onReAdd()
	{
		setRequestFlag(RFLAG_ON_RE_ADD_SUPER_CALL_VERIFIED, true);
	}
}
