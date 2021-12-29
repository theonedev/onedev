# Build the docker image for onedev server
FROM ubuntu:18.04
COPY app /app
COPY entrypoint.sh idle.sh /root/bin/
COPY kubectl /usr/local/bin/
COPY docker /usr/local/bin/
RUN apt-get update && apt-get install -y locales iproute2 curl git git-lfs openjdk-8-jre-headless && rm -rf /var/lib/apt/lists/*
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    locale-gen
ENV LANG en_US.UTF-8  
ENV LANGUAGE en_US:en  
ENV LC_ALL en_US.UTF-8 
EXPOSE 6610
EXPOSE 6611
CMD ["/root/bin/entrypoint.sh"]
