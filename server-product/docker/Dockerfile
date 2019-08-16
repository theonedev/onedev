FROM openjdk:8-jdk
COPY build /app
COPY entrypoint.sh idle.sh /root/bin/
COPY kubectl /usr/local/bin/
EXPOSE 6610
CMD ["/root/bin/entrypoint.sh"]
