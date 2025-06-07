cat << EOF > settings.xml
<settings>
  <servers>
    <server>
      <id>onedev</id>
      <!-- maven:job-token-notice -->
      <username>@job_token@</username>
      <!-- maven:access-token-notice -->
      <password>@secret:access-token@</password>
    </server>
  </servers>
<%
if (url.startsWith("http:") && permission.equals("read")) {
print """
  <!-- maven:allow-http-notice -->
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
%></settings>
EOF

mvn --settings settings.xml clean ${permission.equals("write")? "deploy": "test"}