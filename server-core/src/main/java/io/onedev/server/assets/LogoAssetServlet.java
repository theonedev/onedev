package io.onedev.server.assets;

import java.io.IOException;

import io.onedev.server.web.util.BrandingUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LogoAssetServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
		var logo = BrandingUtils.getLogo(request.getServletPath().equals("/logo-dark.png"));

		// Never cache current bytes under an outdated version, or a URL without a version.
		if (logo.version().equals(request.getParameter("v"))) {
			response.setHeader("Cache-Control", "max-age=86400,public");
			var etag = "\"" + logo.version() + "\"";
			response.setHeader("ETag", etag);
			var ifNoneMatch = request.getHeader("If-None-Match");
			if (ifNoneMatch != null) {
				for (var candidate : ifNoneMatch.split(",")) {
					candidate = candidate.trim();
					if (candidate.equals(etag) || candidate.equals("W/" + etag) || candidate.equals("*")) {
						response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
						return;
					}
				}
			}
		}
		response.setContentType("image/png");
		response.setContentLength(logo.bytes().length);
		response.getOutputStream().write(logo.bytes());
	}

}
