#!/usr/bin/env bash
cd `dirname $0`

docker run -p 8080:8080 -it local.harbor.dev/mobile/locomoco-dance:latest