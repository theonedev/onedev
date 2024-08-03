<%
    if (htmlVersion) {
        print """
            <b>${eventSummary}</b>
            <br>
            <br>
        """

        if (eventBody != null) {
            print """
                ${eventBody}
                <br>
            """
        }

        print """
            <div>Click <a href='${eventUrl}'>this link</a> for details</div>
        """
	} else {
        print "${eventSummary}\n\n"

        if (eventBody != null)
            print "${eventBody}\n\n"

        print "Visit ${eventUrl} for details"
	}
%>
