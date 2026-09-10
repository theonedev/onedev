onedev.server.diffSymbolContext = {
	lookup: function(ranges, line) {
		let low = 0;
		let high = ranges.length;
		while (low < high) {
			const middle = (low + high) >>> 1;
			if (ranges[middle][0] <= line)
				low = middle + 1;
			else
				high = middle;
		}
		const range = ranges[low - 1];
		return range && line < range[1] ? range[2] : "";
	},
	label: function(side, text, translations) {
		const $label = $("<div class='symbol-context-label'>").addClass(side).toggleClass("empty", !text);
		if (side !== "common")
			$("<span class='symbol-context-side'>").text(side === "old" ? "-" : "+")
				.attr("title", translations[side + "-context"]).appendTo($label);
		$("<span class='symbol-context-name'>").text(text).attr("title", text).appendTo($label);
		return $label;
	},
	init: function($container, ranges, translations) {
		if (!ranges.old.length && !ranges.new.length)
			return;
		const previous = $container.data("symbolContext");
		if (previous)
			previous.resizeObserver.disconnect();
		$container.children(".diff-symbol-sticky").remove();
		const context = {ranges, translations};
		$container.data("symbolContext", context);
		const $sticky = $("<div class='diff-symbol-sticky empty'>").prependTo($container);
		for (const side of ["old", "new"])
			$sticky.append(this.label(side, "", translations));
		this.refresh($container);
		context.resizeObserver = new ResizeObserver(() => {
			if (!$container[0].isConnected)
				context.resizeObserver.disconnect();
			else
				this.update($container);
		});
		context.resizeObserver.observe($container[0]);
		if (!this.installed) {
			this.installed = true;
			let pending = false;
			const schedule = () => {
				if (!pending) {
					pending = true;
					requestAnimationFrame(() => {
						pending = false;
						$(".blob-text-diff").each((index, element) => this.update($(element)));
					});
				}
			};
			// One listener for all diffs, including nested scrolling and Ajax replacements.
			document.addEventListener("scroll", schedule, true);
			window.addEventListener("resize", schedule);
			$(document).on("resized", schedule);
		}
	},
	refresh: function($container) {
		const context = $container.data("symbolContext");
		if (!context)
			return;
		const $table = $container.children("table.text-diff");
		$table.find("tr.diff-symbol-hunk").remove();
		const $rows = $table.find("tr.code");
		const split = $rows.first().children("td.content").length === 2;
		context.split = split;
		$container.children(".diff-symbol-sticky").toggleClass("split", split);
		context.cells = {};
		context.hunkHeaders = new WeakMap();
		for (const side of ["old", "new"]) {
			// Split equal cells carry both line numbers; keep only the corresponding column.
			context.cells[side] = $rows.children("td.content[data-" + side + "]")
				.filter((index, cell) => !split || !$(cell).hasClass(side === "old" ? "right" : "left")).toArray();
		}
		let hunk = [];
		const appendHunk = () => {
			if (!hunk.length)
				return;
			const labels = {};
			for (const side of ["old", "new"]) {
				const cells = $(hunk).children("td.content[data-" + side + "]")
					.filter((index, cell) => !split || !$(cell).hasClass(side === "old" ? "right" : "left"));
				// Describe the code immediately below the header on each side, including
				// after expansion reveals lines outside the changed symbol's scope.
				const anchor = cells.first();
				labels[side] = anchor.length ? this.lookup(context.ranges[side], Number(anchor.attr("data-" + side))) : "";
			}
			if (labels.old || labels.new) {
				const $header = $("<tr class='diff-symbol-hunk'>");
				const columns = $(hunk[0]).children().length;
				if (labels.old === labels.new) {
					$("<td>").attr("colspan", columns).append(this.label("common", labels.old, context.translations)).appendTo($header);
				} else if (split) {
					for (const side of ["old", "new"])
						$("<td>").attr("colspan", columns / 2).append(this.label(side, labels[side], context.translations)).appendTo($header);
				} else {
					const $cell = $("<td>").attr("colspan", columns).appendTo($header);
					for (const side of ["old", "new"])
						$cell.append(this.label(side, labels[side], context.translations));
				}
				$header.insertBefore(hunk[0]);
				for (const row of hunk)
					context.hunkHeaders.set(row, $header[0]);
			}
			hunk = [];
		};
		$table.find("tbody>tr").each((index, row) => {
			if ($(row).hasClass("expander"))
				appendHunk();
			else if ($(row).hasClass("code"))
				hunk.push(row);
		});
		appendHunk();
		this.update($container);
	},
	update: function($container) {
		const context = $container.data("symbolContext");
		if (!context || !$container.is(":visible"))
			return;
		const bounds = $container[0].getBoundingClientRect();
		if (bounds.bottom < 0 || bounds.top > window.innerHeight)
			return;
		const $sticky = $container.children(".diff-symbol-sticky");
		const $head = $container.closest(".blob-diff").children(".head");
		const stickyTop = $head.length ? (parseFloat($head.css("top")) || 0) + $head.outerHeight() : 0;
		$sticky.css("top", stickyTop);
		const scrollParent = $container.scrollParent()[0];
		// Use a stable scroll edge, independent of whether the sticky context is visible.
		const top = Math.max(bounds.top, $head.length ? $head[0].getBoundingClientRect().bottom : 0,
			scrollParent && scrollParent.nodeType === 1 ? scrollParent.getBoundingClientRect().top : 0);
		const bottom = scrollParent && scrollParent.nodeType === 1
			? Math.min(window.innerHeight, scrollParent.getBoundingClientRect().bottom) : window.innerHeight;
		const labels = {};
		let headerVisible = false;
		for (const side of ["old", "new"]) {
			const cells = context.cells[side];
			let low = 0;
			let high = cells.length;
			while (low < high) {
				const middle = (low + high) >>> 1;
				if (cells[middle].getBoundingClientRect().bottom <= top)
					low = middle + 1;
				else
					high = middle;
			}
			const cell = cells[low];
			labels[side] = cell && cell.getBoundingClientRect().top < bottom
				? this.lookup(context.ranges[side], Number(cell.getAttribute("data-" + side))) : "";
			if (labels[side]) {
				const header = context.hunkHeaders.get(cell.parentElement);
				if (header && header.getBoundingClientRect().bottom > top)
					headerVisible = true;
			}
		}
		for (const side of ["old", "new"]) {
			const $label = $sticky.children("." + side).toggleClass("empty", !labels[side]);
			const $name = $label.children(".symbol-context-name");
			if ($name.attr("title") !== labels[side])
				$name.text(labels[side]).attr("title", labels[side]);
		}
		const common = labels.old === labels.new;
		$sticky.toggleClass("common", common);
		let oldWidth = "";
		if (context.split && !common && context.cells.new.length) {
			// Column widths include line numbers, operations and optional blame.
			const row = $(context.cells.new[0]).parent()[0];
			const middle = row.children[row.children.length / 2];
			oldWidth = middle.getBoundingClientRect().left - bounds.left;
		}
		$sticky.children(".old").css("width", oldWidth);
		$sticky.toggleClass("empty", headerVisible || !labels.old && !labels.new);
		// Overlay the code without moving hunk headers when context appears or disappears.
		$sticky.css("margin-bottom", $sticky.hasClass("empty") ? 0 : -$sticky.outerHeight());
	}
};
