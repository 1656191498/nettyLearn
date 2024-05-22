#!/bin/bash
if which docker >/dev/null 2>&1; then
    echo "docker exists"
else
    echo "docker not exists"
    echo "apt update"
    apt update
    echo "apt upgrade -y"
    apt upgrade -y
    echo "apt install docker.io -y"
    apt install docker.io -y
    echo "apt install docker-compose -y"
    apt install docker-compose -y
fi

if which docker-compose >/dev/null 2>&1; then
    echo "docker-compose exists"
else
    echo "docker-compose not exists"
    echo "apt install docker-compose -y"
    apt install docker-compose -y
fi

docker-compose stop
docker-compose up -d