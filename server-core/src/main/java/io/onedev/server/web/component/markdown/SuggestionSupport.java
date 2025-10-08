package io.onedev.server.web.component.markdown;

import java.io.Serializable;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.commons.utils.LinearRange;

public interface SuggestionSupport extends Serializable {

	String getFileName();
	
	Selection getSelection();
	
	boolean isOutdated();
	
	@Nullable
	ApplySupport getApplySupport();
	
	interface ApplySupport extends Serializable {
		
		void applySuggestion(AjaxRequestTarget target, List<String> suggestion);

		@Nullable
		BatchApplySupport getBatchSupport();
	}
	
	interface BatchApplySupport extends Serializable {

		@Nullable
		List<String> getInBatch();
		
		void addToBatch(AjaxRequestTarget target, List<String> suggestion);

		void removeFromBatch(AjaxRequestTarget target);
		
	}
	
	class Selection {
		
		private final List<String> content;
		
		private final LinearRange range;
		
		public Selection(List<String> content, LinearRange range) {
			this.content = content;
			this.range = range;
		}

		public List<String> getContent() {
			return content;
		}

		public LinearRange getRange() {
			return range;
		}
		
	}
	
}
