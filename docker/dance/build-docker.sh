#!/usr/bin/env bash
cd `dirname $0`
mkdir ./tmp
cp ../../app/locomoco/locomoco-dance/build/libs/locomoco-dance.war ./tmp/
docker build . --no-cache -t local.harbor.dev:80/mobile/locomoco-dance:latest
rm -rf ./tmp
