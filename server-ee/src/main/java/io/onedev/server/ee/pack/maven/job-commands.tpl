cat << EOF > settings.xml
<settings>
  <servers>
    <server>
      <id>onedev</id>
      <!-- Use job token as user name so that OneDev can know which build is ${permission.equals("write")? "deploying": "using"} packages -->
      <username>@job_token@</username>
      <!-- Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission -->
      <password>@secret:access-token@</password>
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
%></settings>
EOF

mvn --settings settings.xml clean ${permission.equals("write")? "deploy": "test"}