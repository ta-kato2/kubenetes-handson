#!/usr/bin/env bash
cd `dirname $0`
cd ../../app/locomoco/locomoco-base
bash gradlew build

cd ../../../docker/song
rm -rf ./tmp
mkdir ./tmp
cp ../../app/locomoco/locomoco-song/build/libs/locomoco-song.war ./tmp/
docker build . --no-cache -t local.mobile.dev/locomoco-song:latest
