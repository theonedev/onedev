package io.onedev.server.model.support.pullrequest.actiondata;

public class AddedReviewerData extends ActionData {

	private static final long serialVersionUID = 1L;

	private final String reviewer;
	
	public AddedReviewerData(String reviewer) {
		this.reviewer = reviewer;
	}
	
	@Override
	public String getDescription() {
		return "added reviewer \"" + reviewer + "\"";
	}

}
