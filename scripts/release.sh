#!/bin/sh

if mvn release:clean release:prepare; then
    mvn release:perform
else
    mvn release:rollback
    mvn release:clean
fi
