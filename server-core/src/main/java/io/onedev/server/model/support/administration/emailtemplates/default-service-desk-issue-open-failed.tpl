<%
	import org.unbescape.html.HtmlEscape

    if (htmlVersion) {
        if (exception.message != null) {
            print """
                ${HtmlEscape.escapeHtml5(exception.message)}
                <br>
                <br>
                Contact site administrator if necessary
            """
        } else {
            print """
                Please contact site administrator
            """
        }
    } else {
        if (exception.message != null) {
            print "${exception.message}\n\n"
            print "Contact site administrator if necessary"
        } else {
            print "Please contact site administrator"
        }
    }
%>
