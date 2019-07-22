#!/bin/sh
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v $(which docker):/usr/bin/docker -v /opt/onedev:/opt/onedev -v /usr/bin/git:/usr/bin/git -p 6610:6610 1dev/server
