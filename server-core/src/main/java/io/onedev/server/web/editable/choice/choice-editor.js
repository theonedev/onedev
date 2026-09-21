onedev.server.choiceEditor = {
	init: function(editorId, inputId) {
		const $editor = $(document.getElementById(editorId));
		const $input = $(document.getElementById(inputId));
		// Keep the suggestion API on the property editor independent of its input widget.
		$editor.data("suggestionSupport", {
			canSuggest: function() {
				return !$input.prop("disabled") && (!$input.val() || $input.val().length === 0);
			},
			showPlaceholder: function(placeholder) {
				const select2 = $input.data("select2");
				if (!select2)
					return function() {};
				const previous = $input.data("suggestionPlaceholder");
				const saved = {placeholder: previous ? previous.placeholder : select2.selection.placeholder};
				$input.data("suggestionPlaceholder", saved);
				select2.selection.placeholder = {id: "", text: placeholder};
				$input.trigger("change.select2");
				return function() {
					if ($input.data("suggestionPlaceholder") === saved) {
						$input.removeData("suggestionPlaceholder");
						if ($input.data("select2") === select2) {
							select2.selection.placeholder = saved.placeholder;
							$input.trigger("change.select2");
						}
					}
				};
			},
			applySuggestion: function(value) {
				// The choice formatter needs the name metadata as well as text.
				const adapter = $input.data("select2").dataAdapter;
				adapter.addOptions(adapter.option({id: value, text: value, name: value, selected: true}));
				$input.trigger("change");
			}
		});
	}
};
