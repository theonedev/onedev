package io.onedev.server.web.component.markdown;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.commons.utils.LinearRange;

public interface SuggestionSupport {

	SuggestFor getSuggestFor();
	
	boolean isAuthorized();
	
	boolean isOutdated();
	
	void applySuggestion(AjaxRequestTarget target, List<String> suggestion, 
			CommentResolveCallback resolveCallback);
	
	@Nullable
	BatchApplySupport getBatchApplySupport();
	
	interface BatchApplySupport {
		
		boolean isInBatch();
		
		void applySuggestion(AjaxRequestTarget target, List<String> suggestion, 
				CommentResolveCallback resolveCallback);
		
	}
	
	interface CommentResolveCallback extends Serializable {
		
		void resolveComment(@Nullable String note);
		
	}
	
	class SuggestFor {
		
		private final String content;
		
		private final LinearRange range;
		
		public SuggestFor(String content, LinearRange range) {
			this.content = content;
			this.range = range;
		}

		public String getContent() {
			return content;
		}

		public LinearRange getRange() {
			return range;
		}
		
	}
}
