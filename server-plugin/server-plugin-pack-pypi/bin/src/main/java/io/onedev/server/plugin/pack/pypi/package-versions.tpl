<!DOCTYPE html>
<html>
    <body>
<%
import io.onedev.server.util.UrlUtils
import org.unbescape.html.HtmlEscape

for (def pack in packs) {
    def requiresPython = pack.data.attributes["requires_python"];
    def dataRequiresPython;
    if (requiresPython != null && !requiresPython.isEmpty())
        dataRequiresPython = " data-requires-python=\"" + HtmlEscape.escapeHtml5(requiresPython[0]) + "\"";
    else
        dataRequiresPython = "";
    for (def entry in pack.data.sha256BlobHashes.entrySet()) {
    print """
        <a href="${baseUrl}/${UrlUtils.encodePath(pack.version)}/${UrlUtils.encodePath(entry.key)}#sha256=${entry.value}"${dataRequiresPython}>${HtmlEscape.escapeHtml5(entry.key)}</a>
    """
    }
}
%>
	</body>
</html>