#!/usr/bin/env bash
cd `dirname $0`
mkdir ./tmp
cp ../../app/locomoco/locomoco-song/build/libs/locomoco-song.war ./tmp/
docker build . --no-cache -t local.harbor.dev:80/mobile/locomoco-song:latest
rm -rf ./tmp
