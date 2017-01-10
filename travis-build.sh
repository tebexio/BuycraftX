#!/bin/bash

if [ "${TRAVIS_JDK_VERSION}" = "openjdk7" ]; then
    mvn clean package -pl bukkit,bungeecord,common,plugin-shared
else
    mvn clean package
fi