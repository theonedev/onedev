<!DOCTYPE html>
<html>
    <body>
<%
import io.onedev.server.util.UrlUtils
import org.unbescape.html.HtmlEscape

for (def name in names) {
    print """
        <a href="/${UrlUtils.encodePath(name.toLowerCase())}/">${HtmlEscape.escapeHtml5(name)}</a>
    """
}
%>
    </body>
</html>