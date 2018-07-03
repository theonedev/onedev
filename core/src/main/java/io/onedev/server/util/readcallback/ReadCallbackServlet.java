package io.onedev.server.util.readcallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.Charsets;
import org.apache.http.HttpHeaders;
import org.h2.util.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.web.img.Img;
import io.onedev.utils.ClassUtils;

@Singleton
public class ReadCallbackServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static final String PATH = "/read-callback";

	private final ObjectMapper mapper;
	
	@Inject
	public ReadCallbackServlet(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	private void doNotCache(HttpServletResponse response) {
		response.setHeader("Expires", "Fri, 01 Jan 1980 00:00:00 GMT");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
	}

	@Sessional
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String json = URLDecoder.decode(request.getQueryString(), Charsets.UTF_8.name()); 
		ReadCallback callback = mapper.readValue(json, ReadCallback.class);
		callback.onRead();
		
		response.addHeader(HttpHeaders.CONTENT_TYPE, "image/png");
		doNotCache(response);
		
		try (	InputStream is = ClassUtils.getResourceAsStream(Img.class, "1x1.png");
				OutputStream os = response.getOutputStream();) {
			IOUtils.copy(is, os);
			os.flush();
		}
	}
	
	public static String getUrl(ReadCallback callback) {
		try {
			String json = OneDev.getInstance(ObjectMapper.class).writeValueAsString(callback);
			String queryString = URLEncoder.encode(json, Charsets.UTF_8.name());
			return OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl() + PATH + "?" + queryString;
		} catch (UnsupportedEncodingException | JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
