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

        if (replyable)
            print """
                <div>Reply this email to post comment, or click <a href='${eventUrl}'>this link</a> for details</div>
            """
        else
            print """
                <div>Click <a href='${eventUrl}'>this link</a> for details</div>
            """
	} else {
        print "${eventSummary}\n\n"

        if (eventBody != null)
            print "${eventBody}\n\n"

        if (replyable)
            print "Reply this email to post comment, or visit ${eventUrl} for details"
        else
            print "Visit ${eventUrl} for details"
	}
%>

<%
	if (unsubscribable != null) {
	    if (htmlVersion) {
            print """
                <div style='border-top:1px solid #EEE; margin-top:1em; padding-top:1em; color:#666; font-size:0.9em;'>You received this as you
                are participating in this topic.
            """
            if (unsubscribable.getEmailAddress() != null)
                print """
                    Mail to <a href='mailto:${unsubscribable.emailAddress}?subject=Unsubscribe&body=I would like not to get any notifications from this topic'>this address</a> to unsubscribe
                    </div>
                """
            else
                print """
                    To stop receiving notifications of this topic, please visit detail link above and unwatch it
                    </div>
                """
		} else {
            print "\n\n---------------------------------------------\n"
            print "You received this notification as you are participating in this topic. "

            if (unsubscribable.getEmailAddress() != null)
                print "Mail to ${unsubscribable.emailAddress} with any content to unsubscribe"
            else
                print "To stop receiving notifications of this topic, please visit detail link above and unwatch it"
		}
	}
%>