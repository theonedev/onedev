onedev.server.pdfView = {
	render: async function(containerId, pdfUrl) {
		try {
			const pdfjsLib = await import('/~es6/pdfjs/pdf.mjs');
			pdfjsLib.GlobalWorkerOptions.workerSrc = '/~es6/pdfjs/pdf.worker.mjs';
			
			const container = document.getElementById(containerId);
			if (!container)
				return;
			container.innerHTML = '';
			
			const pdf = await pdfjsLib.getDocument({
				url: pdfUrl,
				cMapUrl: '/~es6/pdfjs/cmaps/',
				cMapPacked: true,
				standardFontDataUrl: '/~es6/pdfjs/standard_fonts/',
				wasmUrl: '/~es6/pdfjs/wasm/'
			}).promise;
			
			for (let i = 1; i <= pdf.numPages; i++) {
				const page = await pdf.getPage(i);
				const outputScale = window.devicePixelRatio || 1;
				const scale = 2.0;
				const viewport = page.getViewport({scale: scale});
				
				const canvas = document.createElement('canvas');
				const ctx = canvas.getContext('2d');
				canvas.width = Math.floor(viewport.width * outputScale);
				canvas.height = Math.floor(viewport.height * outputScale);
				canvas.style.width = viewport.width + 'px';
				canvas.style.display = 'block';
				canvas.style.margin = '0 auto 10px auto';
				canvas.style.maxWidth = '100%';
				canvas.style.height = 'auto';
				
				ctx.scale(outputScale, outputScale);
				
				await page.render({
					canvasContext: ctx,
					viewport: viewport
				}).promise;
				
				container.appendChild(canvas);
				$(window).resize();
			}
		} catch (error) {
			console.error(error);
		}
	}
}
