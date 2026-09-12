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

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.json.JSONException;
import org.json.JSONStringer;

import io.onedev.server.annotation.OmitName;
import io.onedev.server.util.ComponentHierarchical;
import io.onedev.server.util.HierarchicalContext;
import io.onedev.server.web.component.select2.json.Json;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;

/**
 * Select2 settings. Refer to the Select2 documentation for what these options
 * mean.
 * 
 * @author igor
 */
public final class Settings implements Serializable {

	private static final long serialVersionUID = 1L;

	private AbstractSelect2Choice<?, ?> select2;
	
	public static class Widths {
		public static final String STYLE = "style";
		public static final String RESOLVE = "resolve";
		public static final String ELEMENT = "element";
		public static final String AUTO = "auto";
	}

	private Integer minimumInputLength;
	private Integer minimumResultsForSearch;
	private Integer maximumSelectionLength;
	private Object placeholder;
	private Boolean allowClear;
	private Boolean multiple;
	private Boolean closeOnSelect;
	private String matcher;
	private String tokenizer;
	private String sorter;
	private String templateSelection;
	private String templateResult;
	private String escapeMarkup;
	private String createTag;
	private String width;
	private String selectionCssClass;
	private String dropdownCssClass;
	private String data;
	private Boolean tags;
	private String[] tokenSeparators;
	private Boolean dropdownAutoWidth;
	private String noResults;
	private String inputTooShort;
	private String maximumSelected;
	private String loadingMore;
	private String searching;
	private AjaxSettings ajax;

	public Settings(AbstractSelect2Choice<?, ?> select2) {
		this.select2 = select2;
	}

	public CharSequence toJson() {
		try {
			JSONStringer writer = new JSONStringer();
			writer.object();
			Json.writeObject(writer, "minimumInputLength", minimumInputLength);
			Json.writeObject(writer, "minimumResultsForSearch", minimumResultsForSearch);
			Json.writeObject(writer, "maximumSelectionLength", maximumSelectionLength);
			Json.writeObject(writer, "placeholder", placeholder);
			Json.writeObject(writer, "allowClear", allowClear);
			Json.writeObject(writer, "multiple", multiple);
			Json.writeObject(writer, "closeOnSelect", closeOnSelect);
			Json.writeFunction(writer, "matcher", matcher);
			Json.writeFunction(writer, "tokenizer", tokenizer);
			Json.writeFunction(writer, "sorter", sorter);
			Json.writeFunction(writer, "templateSelection", templateSelection);
			Json.writeFunction(writer, "templateResult", templateResult);
			Json.writeFunction(writer, "escapeMarkup", escapeMarkup);
			Json.writeFunction(writer, "createTag", createTag);
			Json.writeObject(writer, "width", width);
			Json.writeObject(writer, "selectionCssClass", selectionCssClass);
			Json.writeObject(writer, "dropdownCssClass", dropdownCssClass);
			Json.writeFunction(writer, "data", data);
			Json.writeObject(writer, "tags", tags);
			Json.writeObject(writer, "tokenSeparators", tokenSeparators);
			Json.writeObject(writer, "dropdownAutoWidth", dropdownAutoWidth);
			writer.key("language").object();
			Json.writeFunction(writer, "noResults", noResults);
			Json.writeFunction(writer, "inputTooShort", inputTooShort);
			Json.writeFunction(writer, "maximumSelected", maximumSelected);
			Json.writeFunction(writer, "loadingMore", loadingMore);
			Json.writeFunction(writer, "searching", searching);
			writer.endObject();
			if (ajax != null) {
				writer.key("ajax");
				ajax.toJson(writer);
			}
			writer.endObject();
			return writer.toString();
		} catch (JSONException e) {
			throw new RuntimeException("Could not convert Select2 settings object to Json", e);
		}
	}

	public Integer getMinimumInputLength() {
		return minimumInputLength;
	}

	public void setMinimumInputLength(Integer minimumInputLength) {
		this.minimumInputLength = minimumInputLength;
	}

	public Integer getMinimumResultsForSearch() {
		return minimumResultsForSearch;
	}

	public void setMinimumResultsForSearch(Integer minimumResultsForSearch) {
		this.minimumResultsForSearch = minimumResultsForSearch;
	}

	public Integer getMaximumSelectionLength() {
		return maximumSelectionLength;
	}

	public void setMaximumSelectionLength(Integer maximumSelectionLength) {
		this.maximumSelectionLength = maximumSelectionLength;
	}

	public Object getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(Object placeholder) {
		this.placeholder = placeholder;
	}

	public Boolean getAllowClear() {
		return allowClear;
	}

	public void setAllowClear(Boolean allowClear) {
		this.allowClear = allowClear;
	}

	public Boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public Boolean getCloseOnSelect() {
		return closeOnSelect;
	}

	public void setCloseOnSelect(Boolean closeOnSelect) {
		this.closeOnSelect = closeOnSelect;
	}

	public String getMatcher() {
		return matcher;
	}

	public void setMatcher(String matcher) {
		this.matcher = matcher;
	}

	public String getTokenizer() {
		return tokenizer;
	}

	public void setTokenizer(String tokenizer) {
		this.tokenizer = tokenizer;
	}

	public String getSorter() {
		return sorter;
	}

	public void setSorter(String sorter) {
		this.sorter = sorter;
	}

	public String getTemplateSelection() {
		return templateSelection;
	}

	public void setTemplateSelection(String templateSelection) {
		this.templateSelection = templateSelection;
	}

	public String getTemplateResult() {
		return templateResult;
	}

	public void setTemplateResult(String templateResult) {
		this.templateResult = templateResult;
	}

	public String getEscapeMarkup() {
		return escapeMarkup;
	}

	public void setEscapeMarkup(String escapeMarkup) {
		this.escapeMarkup = escapeMarkup;
	}

	public String getCreateTag() {
		return createTag;
	}

	public void setCreateTag(String createTag) {
		this.createTag = createTag;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getSelectionCssClass() {
		return selectionCssClass;
	}

	public void setSelectionCssClass(String selectionCssClass) {
		this.selectionCssClass = selectionCssClass;
	}

	public String getDropdownCssClass() {
		return dropdownCssClass;
	}

	public void setDropdownCssClass(String dropdownCssClass) {
		this.dropdownCssClass = dropdownCssClass;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Boolean getTags() {
		return tags;
	}

	public void setTags(Boolean tags) {
		this.tags = tags;
	}

	public String[] getTokenSeparators() {
		return tokenSeparators;
	}

	public void setTokenSeparators(String[] tokenSeparators) {
		this.tokenSeparators = tokenSeparators;
	}

	public Boolean getDropdownAutoWidth() {
		return dropdownAutoWidth;
	}

	public void setDropdownAutoWidth(Boolean dropdownAutoWidth) {
		this.dropdownAutoWidth = dropdownAutoWidth;
	}

	public String getNoResults() {
		return noResults;
	}

	public void setNoResults(String noResults) {
		this.noResults = noResults;
	}

	public String getInputTooShort() {
		return inputTooShort;
	}

	public void setInputTooShort(String inputTooShort) {
		this.inputTooShort = inputTooShort;
	}

	public String getMaximumSelected() {
		return maximumSelected;
	}

	public void setMaximumSelected(String maximumSelected) {
		this.maximumSelected = maximumSelected;
	}

	public String getLoadingMore() {
		return loadingMore;
	}

	public void setLoadingMore(String loadingMore) {
		this.loadingMore = loadingMore;
	}

	public String getSearching() {
		return searching;
	}

	public void setSearching(String searching) {
		this.searching = searching;
	}

	public AjaxSettings getAjax() {
		return getAjax(false);
	}

	public AjaxSettings getAjax(boolean createIfNotSet) {
		if (createIfNotSet && ajax == null)
			ajax = new AjaxSettings();
		return ajax;
	}

	public void setAjax(AjaxSettings ajax) {
		this.ajax = ajax;
	}

	public void configurePlaceholder(PropertyDescriptor propertyDescriptor) {
		HierarchicalContext.push(new HierarchicalContext(new ComponentHierarchical(select2)));
		try {
			Method propertyGetter = propertyDescriptor.getPropertyGetter();
			String placeholder = EditableUtils.getPlaceholder(propertyGetter);
			if (placeholder != null) {
				setPlaceholder(placeholder);
			} else if (propertyDescriptor.isPropertyRequired()) {
				if (propertyDescriptor.getPropertyGetter().getAnnotation(OmitName.class) != null)
					setPlaceholder(_T("Choose") + " " + _T(propertyDescriptor.getDisplayName()).toLowerCase() + "...");
				else
					setPlaceholder(_T("Choose..."));
			} else if (propertyDescriptor.getPropertyGetter().getAnnotation(OmitName.class) != null) {
				setPlaceholder(_T(propertyDescriptor.getDisplayName()));
			}
		} finally {
			HierarchicalContext.pop();
		}
	}
	
}
