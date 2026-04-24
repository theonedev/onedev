onedev.server.notebookView = {
	render: async function(containerId, notebookUrl, directoryUrl) {
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

			onedev.server.notebookView.rewriteRelativeUrls(container, directoryUrl);

			$(window).resize();
		} catch (error) {
			console.error(error);
			container.textContent = 'Failed to render notebook: ' + error.message;
		}
	},

	rewriteRelativeUrls: function(container, directoryUrl) {
		if (!directoryUrl)
			return;

		const baseHref = directoryUrl.endsWith('/') ? directoryUrl : directoryUrl + '/';
		let base;
		try {
			base = new URL(baseHref, window.location.href);
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
				const resolved = new URL(value, base);
				resolved.searchParams.set('raw', 'true');
				el.setAttribute(attr, resolved.toString());
			} catch (e) {
				console.warn('Failed to rewrite ' + attr, value, e);
			}
		};

		container.querySelectorAll('img[src]').forEach(el => rewrite(el, 'src'));
		container.querySelectorAll('video[src], audio[src], source[src]').forEach(el => rewrite(el, 'src'));
	}
};
