onedev.server.notebookView = {
	render: async function(containerId, notebookUrl) {
		const container = document.getElementById(containerId);
		if (!container)
			return;

		try {
			if (typeof nb !== 'undefined' && typeof marked !== 'undefined') {
				nb.markdown = function(text) {
					return marked.parse(text);
				};
			}

			const response = await fetch(notebookUrl);
			if (!response.ok)
				throw new Error('Failed to load notebook: ' + response.status);
			const json = await response.json();

			const notebook = nb.parse(json);
			const rendered = notebook.render();

			container.innerHTML = '';
			container.appendChild(rendered);

			onedev.server.notebookView.rewriteRelativeUrls(container, notebookUrl);

			$(window).resize();
		} catch (error) {
			console.error(error);
			container.textContent = 'Failed to render notebook: ' + error.message;
		}
	},

	rewriteRelativeUrls: function(container, notebookUrl) {
		let base;
		try {
			base = new URL(notebookUrl, window.location.href);
		} catch (e) {
			return;
		}

		const isExternal = function(url) {
			return /^[a-z][a-z0-9+\-.]*:/i.test(url) || url.startsWith('//') || url.startsWith('#');
		};

		const rewrite = function(el, attr) {
			const value = el.getAttribute(attr);
			if (!value || isExternal(value))
				return;
			try {
				el.setAttribute(attr, new URL(value, base).toString());
			} catch (e) {
				console.warn('Failed to rewrite ' + attr, value, e);
			}
		};

		container.querySelectorAll('img[src],video[src], audio[src], source[src]').forEach(el => rewrite(el, 'src'));
	}
};
