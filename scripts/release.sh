#!/bin/sh

# don't use the GPG agent, the passphrase must be supplied in the tty
unset GPG_AGENT_INFO

if mvn release:clean release:prepare; then
    mvn release:perform
else
    mvn release:rollback
    mvn release:clean
fi
