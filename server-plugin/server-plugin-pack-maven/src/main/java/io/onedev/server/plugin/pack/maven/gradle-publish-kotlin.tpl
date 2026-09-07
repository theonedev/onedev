plugins {
    id("maven-publish")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("${url}")
            // gradle:has-permission-notice
            credentials {
                username = providers.gradleProperty("onedevUsername").get()
                password = providers.gradleProperty("onedevPassword").get()
            }
<%
if (url.startsWith("http:")) {
print """
            isAllowInsecureProtocol = true
"""
}
%>        }
    }
}
