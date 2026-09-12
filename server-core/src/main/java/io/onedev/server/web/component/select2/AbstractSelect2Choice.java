/*
 * Copyright 2012 Igor Vaynberg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.onedev.server.web.component.select2;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.apache.wicket.IResourceListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.string.Strings;
import org.json.JSONException;
import org.json.JSONWriter;

import io.onedev.server.web.component.select2.json.JsonBuilder;
import io.onedev.server.web.editable.InplacePropertyEditPanel;

/**
 * Base class for Select2 components
 * 
 * @author igor
 * 
 * @param <T>
 *            type of choice object
 * @param <M>
 *            type of model object
 */
abstract class AbstractSelect2Choice<T, M> extends FormComponent<M> implements IResourceListener {

	private static final long serialVersionUID = 1L;

	private final Settings settings = new Settings(this);

	private ChoiceProvider<T> provider;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 */
	public AbstractSelect2Choice(String id) {
		this(id, null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            component model
	 */
	public AbstractSelect2Choice(String id, IModel<M> model) {
		this(id, model, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            component id
	 * @param provider
	 *            choice provider
	 */
	public AbstractSelect2Choice(String id, ChoiceProvider<T> provider) {
		this(id, null, provider);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            component model
	 * @param provider
	 *            choice provider
	 */
	public AbstractSelect2Choice(String id, IModel<M> model, ChoiceProvider<T> provider) {
		super(id, model);
		this.provider = provider;

		setOutputMarkupId(true);
	}

	/**
	 * @return Select2 settings for this component
	 */
	public final Settings getSettings() {
		return settings;
	}

	/**
	 * Sets the choice provider
	 * 
	 * @param provider
	 */
	public final void setProvider(ChoiceProvider<T> provider) {
		this.provider = provider;
	}

	/**
	 * @return choice provider
	 */
	public final ChoiceProvider<T> getProvider() {
		if (provider == null) {
			throw new IllegalStateException(
					"Select2 choice component: " + getId() + " does not have a ChoiceProvider set");
		}
		return provider;
	}

	/**
	 * Gets the markup id that is safe to use in jQuery by escaping dots in the
	 * default {@link #getMarkup()}
	 * 
	 * @return markup id
	 */
	protected String getJquerySafeMarkupId() {
		return getMarkupId().replace(".", "\\\\.");
	}

	/**
	 * Escapes single quotes in localized strings to be used as JavaScript
	 * strings enclosed in single quotes
	 *
	 * @param key
	 *            resource key for localized message
	 * @return localized string with escaped single quotes
	 */
	protected String getEscapedJsString(String key) {
		String value = getString(key);

		return Strings.replaceAll(value, "'", "\\'").toString();
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		checkComponentTag(tag, "select");
		if (Boolean.TRUE.equals(settings.getMultiple()))
			tag.put("multiple", "multiple");
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		replaceComponentTagBody(markupStream, openTag, "");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new Select2ResourceReference()));
		JsonBuilder selection = new JsonBuilder();
		selection.array();
		for (T choice : getSelections()) {
			selection.object();
			getProvider().toJson(choice, selection);
			selection.endObject();
		}
		selection.endArray();
		String script = JQuery.execute("onedev.server.select2.init($('#%s'), %s, %s);",
				getJquerySafeMarkupId(), settings.toJson(), selection.toJson());
		if (findParent(InplacePropertyEditPanel.class) != null) {
			response.render(OnDomReadyHeaderItem.forScript(script));
		} else {
			// Wait for modal dialogs to be visible before sizing the search field.
			response.render(OnLoadHeaderItem.forScript(script));
		}
	}

	/**
	 * Returns the submitted selection after validation, or the current model value.
	 */
	protected abstract Collection<T> getSelections();

	@Override
	protected void onInitialize() {
		super.onInitialize();

		// configure the ajax callbacks

		AjaxSettings ajax = settings.getAjax(true);

		ajax.setData(String.format(
				"function(params) { return { select2_term: params.term || '', select2_page: params.page || 1, '%s':true, '%s':[window.location.protocol, '//', window.location.host, window.location.pathname].join('')}; }",
				WebRequest.PARAM_AJAX, WebRequest.PARAM_AJAX_BASE_URL));

		ajax.setProcessResults("function(data) { return data; }");

		// configure the localized strings/renderers
		getSettings().setNoResults("function() { return '" + getEscapedJsString("noMatches") + "';}");
		getSettings().setInputTooShort("function(args) { return args.minimum - args.input.length == 1 ? '"
				+ getEscapedJsString("inputTooShortSingular") + "' : '" + getEscapedJsString("inputTooShortPlural")
				+ "'.replace('{number}', args.minimum - args.input.length); }");
		getSettings().setMaximumSelected(
				"function(args) { return args.maximum == 1 ? '" + getEscapedJsString("selectionTooBigSingular") + "' : '"
						+ getEscapedJsString("selectionTooBigPlural") + "'.replace('{limit}', args.maximum); }");
		getSettings().setLoadingMore("function() { return '" + getEscapedJsString("loadMore") + "';}");
		getSettings().setSearching("function() { return '" + getEscapedJsString("searching") + "';}");
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();

		getSettings().getAjax().setUrl(urlFor(IResourceListener.INTERFACE, null));
	}

	@Override
	public void onResourceRequested() {
		// this is the callback that retrieves matching choices used to populate
		// the dropdown

		Request request = getRequestCycle().getRequest();
		IRequestParameters params = request.getRequestParameters();

		// retrieve choices matching the search term

		String term = params.getParameterValue("select2_term").toOptionalString();

		int page = params.getParameterValue("select2_page").toInt(1);
		
		// select2 uses 1-based paging, but in wicket world we are used to
		// 0-based
		page -= 1;

		Response<T> response = new Response<T>();
		provider.query(term, page, response);

		// jsonize and write out the choices to the response

		WebResponse webResponse = (WebResponse) getRequestCycle().getResponse();
		webResponse.setContentType("application/json");

		OutputStreamWriter out = new OutputStreamWriter(webResponse.getOutputStream(), getRequest().getCharset());
		JSONWriter json = new JSONWriter(out);

		try {
			json.object();
			json.key("results").array();
			for (T item : response) {
				json.object();
				provider.toJson(item, json);
				json.endObject();
			}
			json.endArray();
			json.key("pagination").object().key("more").value(response.getHasMore()).endObject();
			json.endObject();
		} catch (JSONException e) {
			throw new RuntimeException("Could not write Json response", e);
		}
		
		try {
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException("Could not write Json to servlet response", e);
		}
	}

	@Override
	protected void onDetach() {
		provider.detach();
		super.onDetach();
	}

}
