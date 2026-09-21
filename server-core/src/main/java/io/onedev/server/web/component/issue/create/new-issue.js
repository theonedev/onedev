onedev.server.newIssue = {
	init: function(editorId, callback) {
		const $editor = $(document.getElementById(editorId));
		const previous = $editor.data("jev");
		if (previous)
			clearTimeout(previous.timer);
		const state = previous || {touched: new Set(), requested: new Map(), applying: false, submitting: false};
		state.revision = Date.now();
		$editor.data("jev", state);
		state.suggest = function() {
			clearTimeout(state.timer);
			if (!document.documentElement.contains($editor[0]) || state.submitting)
				return;
			const title = $editor.find(".new-issue > .form-group input[type=text]").first().val() || "";
			const description = $editor.find(".description textarea").val() || "";
			const $fields = $editor.find(".jev-suggest").filter(function() {
				const support = $(this).data("suggestionSupport");
				return $(this).is(":visible") && support && support.canSuggest()
					&& !state.touched.has(this.id) && state.requested.get(this.id) !== this;
			});
			if (title.trim() && $fields.length) {
				// Replaced Wicket editors are eligible again, even if their markup id is reused.
				$fields.each(function() { state.requested.set(this.id, this); });
				const fields = $fields.map(function() { return this.id; }).get();
				callback(title, description, JSON.stringify(fields), state.revision);
			}
		};
		$editor.off(".jev").on("input.jev", ".new-issue > .form-group input[type=text], .description textarea", function() {
			state.revision++;
			state.submitting = false;
			state.requested.clear();
			clearTimeout(state.timer);
			state.timer = setTimeout(state.suggest, 750);
		}).on("change.jev", ".property-editor", function() {
			if (!state.applying) {
				state.revision++;
				state.touched.add(this.id);
				state.submitting = false;
				state.requested.clear();
				// Wait for the field update response to render dependent fields.
				clearTimeout(state.timer);
			}
		});
		if (state.form) {
			state.form.removeEventListener("submit", state.onSubmit, true);
			state.form.removeEventListener("click", state.onSubmit, true);
		}
		state.form = $editor.closest("form")[0];
		state.onSubmit = function(event) {
			if (event.type === "submit" || $(event.target).closest(":submit").length) {
				state.submitting = true;
				state.revision++;
				clearTimeout(state.timer);
			}
		};
		// Capture submission before Wicket's Ajax buttons can stop event propagation.
		state.form.addEventListener("submit", state.onSubmit, true);
		state.form.addEventListener("click", state.onSubmit, true);
		// Rendering the form again after validation must not request suggestions.
		// Requests start with user input/change events and continue after field updates.
	},
	fieldsUpdated: function(editorId) {
		const state = $(document.getElementById(editorId)).data("jev");
		if (state && !state.submitting)
			state.suggest();
	},
	beginSuggestions: function(editorId, attrs, placeholder) {
		const $editor = $(document.getElementById(editorId));
		const state = $editor.data("jev");
		const params = Object.fromEntries(attrs.ep.map(it => [it.name, it.value]));
		attrs.jevPlaceholders = [];
		if (!state || state.submitting || state.revision !== params.revision)
			return;
		JSON.parse(params.fields).forEach(function(id) {
			const $field = $(document.getElementById(id));
			const support = $field.data("suggestionSupport");
			if ($field.closest($editor).length && !state.touched.has(id)
					&& support && support.canSuggest()) {
				attrs.jevPlaceholders.push(support.showPlaceholder(placeholder));
			}
		});
	},
	endSuggestions: function(attrs) {
		(attrs.jevPlaceholders || []).forEach(restore => restore());
	},
	applySuggestions: function(editorId, revision, suggestions) {
		const $editor = $(document.getElementById(editorId));
		const state = $editor.data("jev");
		if (!state || state.submitting || state.revision !== revision)
			return;
		state.applying = true;
		try {
			Object.entries(suggestions).forEach(function([id, value]) {
				const $field = $(document.getElementById(id));
				const support = $field.data("suggestionSupport");
				if ($field.closest($editor).length && $field.is(":visible")
						&& !state.touched.has(id) && support && support.canSuggest()) {
					support.applySuggestion(value);
				}
			});
		} finally {
			state.applying = false;
		}
	}
};
