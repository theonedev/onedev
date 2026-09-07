dependencies {
    ${scope}("${groupId}:${artifactId}:${version}")
}

repositories {
    maven {
        url = uri("${url}")
<%
if (!canAccessAnonymously) {
print """
        // gradle:has-permission-notice
        credentials {
            username = providers.gradleProperty("onedevUsername").get()
            password = providers.gradleProperty("onedevPassword").get()
        }
"""
}
%><%
if (url.startsWith("http:") && permission.equals("read")) {
print """
        // gradle:allow-http-notice
        isAllowInsecureProtocol = true
"""
}
%>    }
}
