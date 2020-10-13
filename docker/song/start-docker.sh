#!/usr/bin/env bash
cd `dirname $0`

docker run -p 8080:8080 -it local.mobile.dev/locomoco-song:latest