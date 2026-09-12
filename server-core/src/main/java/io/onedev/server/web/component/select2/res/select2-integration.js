onedev.server.select2 = {
	init: function($select, settings, selection) {
		var integration = this;
		integration.destroy($select);
		$select.empty();
		if (!settings.multiple)
			$select.append(new Option("", ""));

		// Custom renderers expect domain objects. Select2 also passes loading
		// messages and placeholders, which only have a text property.
		["templateResult", "templateSelection"].forEach(function(name) {
			var template = settings[name];
			if (template) {
				settings[name] = function(data, container) {
					if (data.loading || data.id == null || data.id === "")
						return document.createTextNode(data.text || "");
					return template(data, container);
				};
			}
		});
		if (!settings.width)
			settings.width = "style";
		var $modal = $select.closest(".modal");
		if ($modal.length) {
			// Use a borderless positioned parent that moves with the modal content.
			// The modal-content border in dark mode would offset the dropdown by 1px.
			// Attaching to the scrolling modal misplaces the search field on open,
			// and focusing it can jump the modal back to the top.
			settings.dropdownParent = $select.closest(".modal-dialog");
		}
		$select.select2(settings);
		var instance = $select.data("select2");
		instance.$container.find("textarea").addClass("no-autosize");
		// Preserve layout/validation classes previously copied by Select2 3.x.
		instance.$container.addClass(($select.attr("class") || "")
			.split(/\s+/).filter(function(name) {
				return name !== "select2-hidden-accessible" && name !== "select2";
			}).join(" "));

		// Use the data adapter so custom metadata survives later selection updates.
		selection.forEach(function(data) {
			data.selected = true;
			instance.dataAdapter.addOptions(instance.dataAdapter.option(data));
		});
		$select.trigger("change.select2");
		if (settings.multiple) {
			// Removing a choice must not also toggle the selection's dropdown.
			instance.$selection.on("click.onedev", ".select2-selection__choice__remove", function(event) {
				event.stopPropagation();
			});
			// Native selects submit in option order. Move each newly selected option
			// to the end before Select2 emits change, preserving selection order.
			$select.on("select2:selecting.onedev", function(event) {
				var data = event.params.args.data;
				$select.children("option").filter(function() {
					return this.value === String(data.id);
				}).appendTo($select);
			});
			integration.initDragSort($select);
		}
		$select.on("select2:open.onedev", function() {
			if ($modal.length) {
				// Replace Select2's ancestor scroll lock. Its close handler removes
				// this same namespaced listener when the dropdown is dismissed.
				var Utils = $.fn.select2.amd.require("select2/utils");
				var scrollEvent = "scroll.select2." + instance.id;
				instance.$container.parents().filter(Utils.hasScroll)
					.off(scrollEvent).on(scrollEvent, function() {
						$select.select2("close");
					});
			}
			// Dropdowns are attached outside the bean editor's validation wrapper.
			instance.dropdown.$dropdown.toggleClass("is-invalid",
				$select.closest(".is-invalid").length !== 0 || instance.$selection.hasClass("is-invalid"));
			// Focus explicitly: jQuery 3.6+ does not focus a hidden search field.
			var $search = instance.dropdown.$search || instance.selection.$search;
			if ($search && $search.length)
				$search[0].focus();
		}).on("select2:closing.onedev", function(event) {
			// Escape closes the dropdown before the containing dialog sees it.
			var originalEvent = event.params.args.originalEvent;
			if (originalEvent && originalEvent.which === 27)
				originalEvent.stopPropagation();
		});
	},

	initDragSort: function($select) {
		var instance = $select.data("select2");
		var Utils = $.fn.select2.amd.require("select2/utils");
		var $sortable = instance.$selection.find(".select2-selection__rendered").sortable({
			containment: instance.$selection[0],
			tolerance: "pointer",
			items: ".select2-selection__choice",
			cancel: ".select2-selection__choice__remove",
			disabled: $select.prop("disabled"),
			start: function() {
				$select.select2("close");
			},
			update: function() {
				$(this).children(".select2-selection__choice").each(function() {
					var data = Utils.GetData(this, "data");
					$select.append(data.element);
				});
				$select.trigger("change");
			}
		});
		instance.on("enable", function() {
			$sortable.sortable("enable");
		});
		instance.on("disable", function() {
			$sortable.sortable("disable");
		});
	},

	destroy: function($select) {
		var instance = $select.data("select2");
		if (instance) {
			// Select2 destroys the dropdown without detaching its positioning
			// listeners when Wicket replaces an open control.
			instance.dropdown._detachPositioningHandler(instance);
			var $sortable = instance.$selection.find(".ui-sortable");
			if ($sortable.length)
				$sortable.sortable("destroy");
			$select.off(".onedev");
			$select.select2("destroy");
		}
	},

	destroyWithin: function($container) {
		$container.find("select.select2-hidden-accessible")
			.addBack("select.select2-hidden-accessible").each(function() {
				onedev.server.select2.destroy($(this));
			});
	}
};

// Clean up controls when either they or a containing panel is replaced by Wicket.
$(document).on("beforeElementReplace", function(event, componentId) {
	onedev.server.select2.destroyWithin($("#" + componentId));
});
$(document).on("close", ".floating", function(event) {
	if (event.target === this)
		onedev.server.select2.destroyWithin($(this));
}).on("hide.bs.modal", ".modal", function(event) {
	if (event.target === this)
		onedev.server.select2.destroyWithin($(this));
});
