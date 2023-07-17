<%
	import org.unbescape.html.HtmlEscape

    if (htmlVersion) {
        print """
            <b>${HtmlEscape.escapeHtml5(alert.subject)}</b>
            <br><br>
            OneDev URL: <a href='${serverUrl}'>${serverUrl}</a>
        """
        if (alert.detail != null) {
            print """
                <br><br>
                Error detail:
                <br>
                <pre style='font-family: monospace;'>${HtmlEscape.escapeHtml5(alert.detail)}</pre>
            """
        }
    } else {
        print "${alert.subject}\n\n"
        print "OneDev URL: ${serverUrl}\n\n"

        if (alert.detail != null) {
            print "Error Detail:\n"
            print alert.detail
        }
    }
%>
