package io.onedev.server.util.jobmatch;

import static io.onedev.server.util.jobmatch.JobMatch.getRuleName;
import static io.onedev.server.util.jobmatch.JobMatchLexer.Is;
import static io.onedev.server.util.query.BuildQueryConstants.FIELD_IMAGE;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class ImageCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private String image;
	
	public ImageCriteria(String image) {
		this.image = image;
	}

	@Override
	public boolean matches(Build build) {
		return WildcardUtils.matchString(image, build.getJob().getImage());
	}

	@Override
	public String asString() {
		return quote(FIELD_IMAGE) + " " + getRuleName(Is) + " " + quote(image);
	}
	
}
