#!/usr/bin/env bash
cd `dirname $0`
echo "Password1!" | docker login local.harbor.dev:80 --username admin --password-stdin
docker push local.harbor.dev:80/mobile/locomoco-dance:latest
