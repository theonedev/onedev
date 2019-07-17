FROM openjdk:8-jdk
COPY target/extracted/onedev /app
EXPOSE 6610
CMD ["sh", "-c", "/app/bin/upgrade.sh /opt/onedev && /opt/onedev/bin/server.sh console"]
