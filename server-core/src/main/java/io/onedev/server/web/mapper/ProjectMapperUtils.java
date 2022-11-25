package io.onedev.server.web.mapper;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.core.request.mapper.MapperUtils;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;

public class ProjectMapperUtils {

	public static final String PARAM_PROJECT = "project";
	
	@Nullable
	public static Url normalize(Url url) {
		Url normalizedUrl = new Url(url);
		List<String> segments = normalizedUrl.getSegments();
		Collection<String> reservedNames = OneDev.getInstance(ProjectManager.class).getReservedNames();
		int nonProjectSegmentIndex = segments.size();
		for (int i = 0; i < segments.size(); i++) {
			String segment = segments.get(i);
			if (segment.startsWith("~") || reservedNames.contains(segment)) {
				nonProjectSegmentIndex = i;
				break;
			}
		}
		
		if (nonProjectSegmentIndex != 0) {
			String projectPath = Joiner.on("/").join(segments.subList(0, nonProjectSegmentIndex));
			for (int i = 0; i < nonProjectSegmentIndex; i++)
				segments.remove(0);
			segments.add(0, projectPath);
			return normalizedUrl;
		} else {
			return null;
		}
	}
	
	public static boolean setPlaceholders(PageParameters parameters, Url url, String[] mountSegments, 
			PlaceholderProvider placeholderProvider) {
		boolean mandatoryParametersSet = true;

		url.getSegments().remove(0);
		int start = 0;
		for (String projectName: Splitter.on("/").split(parameters.get(PARAM_PROJECT).toString())) {
			url.getSegments().add(start++, projectName);
		}
		parameters.remove(PARAM_PROJECT);
		
		int dropped = 0;
		for (int i = 1; i < mountSegments.length; ++i) {
			String placeholder = placeholderProvider.getPlaceholder(mountSegments[i]);
			String optionalPlaceholder = placeholderProvider.getOptionalPlaceholder(mountSegments[i]);
			if (placeholder != null) {
				if (parameters.getNamedKeys().contains(placeholder)) {
					url.getSegments().set(i - dropped + start -1, parameters.get(placeholder).toString());
					parameters.remove(placeholder);
				} else {
					mandatoryParametersSet = false;
					break;
				}
			} else if (optionalPlaceholder != null) {
				if (parameters.getNamedKeys().contains(optionalPlaceholder)) {
					url.getSegments().set(i - dropped + start - 1, parameters.get(optionalPlaceholder).toString(""));
					parameters.remove(optionalPlaceholder);
				} else {
					url.getSegments().remove(i - dropped + start - 1);
					dropped++;
				}
			}
		}

		return mandatoryParametersSet;
	}
	
	public static PageParameters extractPageParameters(final Request request, int segmentsToSkip,
			final IPageParametersEncoder encoder) {
		// strip the segments and first query parameter from URL
		Url normalizedUrl = normalize(request.getUrl());
		if (normalizedUrl == null)
			normalizedUrl = new Url(request.getUrl());
		while ((segmentsToSkip > 0) && (normalizedUrl.getSegments().isEmpty() == false)) {
			normalizedUrl.getSegments().remove(0);
			--segmentsToSkip;
		}

		if (!normalizedUrl.getQueryParameters().isEmpty()
				&& Strings.isEmpty(normalizedUrl.getQueryParameters().get(0).getValue())) {
			removeMetaParameter(normalizedUrl);
		}

		return encoder.decodePageParameters(normalizedUrl);
	}
	
	private static void removeMetaParameter(Url url) {
 		if (MapperUtils.parsePageComponentInfoParameter(url.getQueryParameters().get(0)) != null)
			url.getQueryParameters().remove(0);
	}
	
}
