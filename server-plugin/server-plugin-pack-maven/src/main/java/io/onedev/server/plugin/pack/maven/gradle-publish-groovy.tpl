plugins {
    id 'maven-publish'
}

publishing {
    publications {
        maven(MavenPublication) {
            from components.java
        }
    }
    repositories {
        maven {
            url '${url}'
            // gradle:has-permission-notice
            credentials {
                username = providers.gradleProperty('onedevUsername').get()
                password = providers.gradleProperty('onedevPassword').get()
            }
<%
if (url.startsWith("http:")) {
print """
            allowInsecureProtocol = true
"""
}
%>        }
    }
}
