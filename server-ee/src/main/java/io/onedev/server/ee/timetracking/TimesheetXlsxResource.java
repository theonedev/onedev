package io.onedev.server.ee.timetracking;

import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.resource.AbstractResource;

public abstract class TimesheetXlsxResource extends AbstractResource {
	
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		response.disableCaching();
		response.setFileName("timesheet.xlsx");
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) {
				 getTimesheet().exportAsXlsx(getTitle(), attributes.getResponse().getOutputStream());
			}
		});

		return response;
	}
	
	protected abstract String getTitle();
	
	protected abstract TimesheetPanel getTimesheet();
	
}
