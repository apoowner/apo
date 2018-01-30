----
# OPERATORS GUIDE #

----
## How to verify the NRS? ##
  Releases are signed by Jean-Luc using [GPG](https://en.wikipedia.org/wiki/GNU_Privacy_Guard). It is **highly** recommended to verify the signature every time you download new version. [There are some notes](https://bitcointalk.org/index.php?topic=345619.msg4406124#msg4406124) how to do this. [This script](https://github.com/nxt-ext/nxt-kit/blob/master/distrib/safe-nxt-download.sh) automates this process on Linux.

----
## How to configure the NRS? ##

  - config files under `conf/`
  - options are described in config files
  - **do not edit** `conf/nxt-default.properties` **nor** `conf/logging-default.properties`
  - use own config file instead: `conf/nxt.properties` or `conf/logging.properties`
  - only deviations from default config

----
## How to update the NRS? ##

  - **if configured as described above**, just unpack a new version over the existing installation directory
  - next run of NRS will upgrade database if necessary
  
----

## How to manage multiple NRS-nodes? ##
  Check [Nxt-Kit's homepage](https://github.com/nxt-ext/nxt-kit) for more information.

----

# USEFULL COMMANDS TO SETUP NEW NXT BASED BLOCKCHAIN #

Modify the listed scripts to be used for own scope replacing the word `xxx` with your preferred 3 chars name (i.e. `ZOO`) __maintain case__.

----
## How to prepare server (UBUNTU 16.04 distro)?
```
vi /etc/default/locale
LC_ALL=en_US.UTF-8
LANG=en_US.UTF-8
LANGUAGE=en_US.UTF-8

vi /etc/hostname
vi /etc/hosts

apt-get update
apt-get upgrade
reboot
apt-get install unzip zip openjdk-8-jdk git
```
### Create service ###
#### create file /etc/init/xxx-server.conf ####
```
#!/bin/sh
### BEGIN INIT INFO
# Provides: xxx-server
# Required-Start: $remote_fs $syslog
# Required-Stop: $remote_fs $syslog
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: Xxx
# Description: This file starts and stops xxx-server
#
### END INIT INFO

HOME_DIR=/home/xxx

if [ -x jre/bin/java ]; then
        JAVA=./jre/bin/java
else
        JAVA=java
fi

case "$1" in
 start)
        PID=`ps aux | grep -v grep | grep xxx.type=mainnet | tr -s ' ' | cut -f 2 -d ' '`
        if [ -z "$PID" ] ; then
          echo "Starting Xxx"
          cd $HOME_DIR
          nohup ${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/* -Dxxx.type=mainnet xxx.Xxx > /dev/null 2>&1 &
        else
          echo "XXX is already running"
        fi
        ;;
 stop)
        PID=`ps aux | grep -v grep | grep xxx.type=mainnet | tr -s ' ' | cut -f 2 -d ' '`
        if [ -z "$PID" ] ; then
          echo "Xxx is not running"
          exit 1
        fi
        echo "Stopping Xxx PID $PID"
        kill $PID
        sleep 10
        ;;
 restart)
        PID=`ps aux | grep -v grep | grep xxx.type=mainnet | tr -s ' ' | cut -f 2 -d ' '`
        if [ -z "$PID" ] ; then
          echo "Xxx is not running"
        else
          echo "Stopping Xxx PID $PID"
          kill $PID
          sleep 10
        fi
        cd $HOME_DIR
        echo "Starting Xxx"
        nohup ${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/* -Dxxx.type=mainnet xxx.Xxx > /dev/null 2>&1 &
        ;;
 status)
        echo "Checking xxx-server..."
        PID=`ps aux | grep -v grep | grep xxx.type=mainnet | tr -s ' ' | cut -f 2 -d ' '`
        if [ -z "$PID" ] ; then
          echo "Xxx is not running"
        else
          echo "Xxx is running with PID="$PID
        fi
        ;;
 *)
        echo "Usage: xxx-server {start|stop|restart}" >&2
        exit 3
        ;;
esac
```

#### update-rc.d ####
```
sudo chmod a+x /etc/init/xxx-server.conf
ln -s /etc/init/xxx-server.conf /etc/init.d/xxx-server
update-rc.d xxx-server defaults
```
#### create script ~/checkXXXserver.sh for crontab ####
```
#!/bin/sh

PID=`ps aux | grep -v grep | grep xxx.type=mainnet | tr -s ' ' | cut -f 2 -d ' '`
if [ -z "$PID" ] ; then
        echo "Start Xxx"
        /etc/init.d/xxx-server stop
        /etc/init.d/xxx-server start
else
        echo "XXX is running"
fi
```
#### update crontab ####

By `crontab -e` command, modifying the crontab adding `*/15 * * * * ~/checkXXXserver.sh`
Means each 15 mins the script `~/checkXXXserver.sh` will be executed
### Create firewall rule ###


insert into `/etc/rc.local` this `iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to 27876`
if needed remove the existing rule use `iptables -t nat -D PREROUTING 1`
__NO NEEDED IF USE CADDY (see below)__
----
## How to install/update software? ##
### Clone the git repository (only first time) ###
```
git clone https://[REPO_URL]/xxx.git /home/xxx/
```

### Update command ###

__Note: Need to define which branch you want to use [gitBranchName]__
```
cd /home/xxx
git checkout [gitBranchName]
git reset --hard origin/[gitBranchName]
git pull origin [gitBranchName]
[git password request]
```
Than execute the script `/home/xxx/update_server_param.sh &`
### Check working log ###
`tail -f /home/xxx/logs/xxx.log`

### Caddy ###
#### Install caddy ####
If exist remove `iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to 27876` from `vi /etc/rc.local`
`curl https://getcaddy.com | bash`
#### Create conf Caddyfile ####

Create new file with command `vi /root/Caddyfile` and insert the code for each domain you want:
```
sg.xxxacc.com {
    proxy / localhost:27876 {
        header_upstream Host {host}
    }
}
www.xxxacc.com {
    proxy / localhost:27876 {
        header_upstream Host {host}
    }
}
...
```
#### Pair DNS name ####
`ulimit -n 8192`
`caddy --host us.xxxacc.com`
#### Create service for caddy ####
`vi /etc/init/caddy.conf`
```
#!/bin/bash
# Caddy daemon
# chkconfig: 345 20 80
# description: Caddy daemon
# processname: caddy

DAEMON_PATH="/usr/local/bin"

DAEMON='./caddy'
DAEMONOPTS="-conf=/root/Caddyfile -log /var/log/caddy.log"

NAME=caddy
DESC="Caddy upstart"
PIDFILE=/var/run/$NAME.pid
SCRIPTNAME=/etc/init.d/$NAME

case "$1" in
start)
    printf "%-50s" "Starting $NAME..."
    cd $DAEMON_PATH
    PID=`$DAEMON $DAEMONOPTS > /dev/null 2>&1 & echo $!`
    echo "Saving PID" $PID " to " $PIDFILE
        if [ -z $PID ]; then
            printf "%s\n" "Fail"
        else
            echo $PID > $PIDFILE
            printf "%s\n" "Ok"
        fi
;;
status)
        printf "%-50s" "Checking $NAME..."
        if [ -f $PIDFILE ]; then
            PID=`cat $PIDFILE`
            if [ -z "`ps axf | grep ${PID} | grep -v grep`" ]; then
                printf "%s\n" "Process dead but pidfile exists"
            else
                echo "Running"
            fi
        else
            printf "%s\n" "Service not running"
        fi
;;
stop)
        printf "%-50s" "Stopping $NAME"
            PID=`cat $PIDFILE`
            cd $DAEMON_PATH
        if [ -f $PIDFILE ]; then
            kill -HUP $PID
            printf "%s\n" "Ok"
            rm -f $PIDFILE
        else
            printf "%s\n" "pidfile not found"
        fi
;;

restart)
    $0 stop
    $0 start
;;

*)
        echo "Usage: $0 {status|start|stop|restart}"
        exit 1
esac
```
#### Set as service ####
```
sudo chmod a+x /etc/init/caddy.conf
ln -s /etc/init/caddy.conf /etc/init.d/caddy
update-rc.d caddy defaults
```
----
