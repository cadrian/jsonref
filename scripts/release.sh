#!/bin/sh

case x"$1" in

    x)
        # Don't use any previously running GPG agent, the passphrase
        # must be supplied via the tty (in particular Gnome's does not
        # play well with my specific passwords manager).

        # Be sure to stop the temporary GPG agent at the end of the
        # program.

        unset GPG_AGENT_INFO
        unset DISPLAY

        pinentry=$(which pinentry-curses)
        if [ -x "$pinentry" ]; then
            export GPG_TTY=$(tty)
            exec gpg-agent --pinentry-program $pinentry --daemon $0 __release__
        else
            # don't use the GPG agent
            exec $0 __release__
        fi
        ;;

    x__release__)
        # The actual release happens now.
        mvn release:clean
        if release:prepare; then
            mvn release:perform
        else
            mvn release:rollback
            mvn release:clean
        fi
        ;;

    *)
        echo "Unsupported args: $@"
        ;;

esac
