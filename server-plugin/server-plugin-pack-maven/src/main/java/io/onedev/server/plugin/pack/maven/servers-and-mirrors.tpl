<servers>
  <server>
    <id>onedev</id>
    <!-- Make sure the account has package ${permission} permission over the project -->
    <username>onedev_account_name</username>
    <password>onedev_password_or_access_token</password>
  </server>
</servers>
<%
if (url.startsWith("http:") && permission.equals("read")) {
print """
<!-- Add below to allow accessing via http protocol in new Maven versions -->
<mirrors>
  <mirror>
    <id>maven-default-http-blocker</id>
    <mirrorOf>dummy</mirrorOf>
    <name>Dummy mirror to override default blocking mirror that blocks http</name>
    <url>http://0.0.0.0/</url>
  </mirror>
</mirrors>
"""
}
%>