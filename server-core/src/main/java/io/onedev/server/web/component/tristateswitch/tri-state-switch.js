onedev.server.triStateSwitch = {
	onDomReady: function(id) {
		const input = document.getElementById(id);
		if (!input) return;
		let container = input.parentElement;
		// An Ajax update of just the input preserves its decorative wrapper.
		if (!container.classList.contains("tri-state-switch")) {
			container = document.createElement("span");
			container.className = "tri-state-switch";
			input.before(container);
			container.append(input);
			const labels = document.createElement("span");
			labels.className = "tri-state-switch-labels";
			labels.setAttribute("aria-hidden", "true");
			for (let i = 0; i < 3; i++) labels.append(document.createElement("span"));
			container.append(labels);
		}
		const labels = container.querySelector(".tri-state-switch-labels").children;
		labels[0].textContent = input.dataset.off;
		labels[1].textContent = "−";
		labels[2].textContent = input.dataset.on;
		const update = function() {
			container.dataset.state = input.value;
			container.classList.toggle("disabled", input.matches(":disabled"));
			input.setAttribute("aria-valuetext",
				[input.dataset.off, input.dataset.unspecified, input.dataset.on][input.value]);
		};
		$(input).off(".triStateSwitch").on("input.triStateSwitch change.triStateSwitch", update)
			.on("keydown.triStateSwitch", function(event) {
				if (event.key === " " && !input.matches(":disabled")) {
					event.preventDefault();
					input.value = (Number(input.value) + 1) % 3;
					input.dispatchEvent(new Event("input", {bubbles: true}));
					input.dispatchEvent(new Event("change", {bubbles: true}));
				}
			});
		if (input.form) {
			$(input.form).off("reset.triStateSwitch").on("reset.triStateSwitch", function() {
				const form = this;
				setTimeout(function() {
					$(form).find(".tri-state-switch-input").trigger("input.triStateSwitch");
				}, 0);
			});
		}
		update();
	}
};
