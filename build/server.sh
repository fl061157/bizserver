#!/bin/sh
#

RUNUSER=faceshow

GRACETIME=30

SCRIPT_FULLPATH=$(cd "$(dirname "$0")"; pwd)
BASEDIR=$(cd "$SCRIPT_FULLPATH/../.."; pwd)
RUNDIR=$(cd "$SCRIPT_FULLPATH/.."; pwd)
NODEFILE=$BASEDIR/conf/node

if [[ ! -f $SCRIPT_FULLPATH/project.sh ]]; then
    echo "project.sh not found."
    exit 1;
fi

source $SCRIPT_FULLPATH/project.sh

CLASSPATH=$RUNDIR/conf:$RUNDIR/lib/*
JAVAOPTS_DEFAULT="-Xms256m -Xmx768m"
JAVAOPTS="-server -Djava.io.tmpdir=$BASEDIR/tmp -Djava.awt.headless=true $APP_JAVAOPTS"
SYSTEMOUT=$BASEDIR/logs/system.out
SYSTEMERR=$BASEDIR/logs/system.err
PIDFILE=$BASEDIR/var/run.pid

TOMCAT_CONTEXT_CONFIG_TEMPLATE=$RUNDIR/bin/context.xml

pid=0

if [ ! -f $NODEFILE ] ; then
    echo "$NODEFILE not found."
    exit 1;
fi

runuser_config="`cat $NODEFILE |awk -F "=" '/RUNUSER/{print $2}'`"
if [ ! "x$runuser_config" == "x" ] ; then
    RUNUSER=$runuser_config
fi

java_home_config="`cat $NODEFILE |awk -F "=" '/JAVA_HOME/{print $2}'`"
if [ ! "x$java_home_config" == "x" ] ; then
    JAVA_HOME=$java_home_config
fi

if [ -n "$JAVA_HOME" ]; then
    echo "use java home : $JAVA_HOME";
    java="$JAVA_HOME"/bin/java
    JSTACK="$JAVA_HOME"/bin/jstack
    JMAP="$JAVA_HOME"/bin/jmap
    JSTAT="$JAVA_HOME"/bin/jstat
    if [ -x "$java" ]; then
       JAVA="$java"
    fi
else
    JAVA=java
    JSTACK=jstack
    JMAP=jmap
    JSTAT=jstat
fi

if [ -z $JAVA ] ; then
    echo Unable to find java executable. Check JAVA_HOME and PATH environment variables. > /dev/stderr
    exit 1;
else
    JAVA_VERSION=`$JAVA -version 2>&1| awk 'NR==1{print $0}'`
    JDK_VERSION=`$JAVA -version 2>&1| awk 'NR==1{print $0}'|grep '1.8'`
    echo "$JAVA_VERSION"
    if [ "x$JDK_VERSION" == "x" ] ; then
       echo "java version is $JAVA_VERSION.should be 1.8"
       exit 1
    fi
fi

#check web config
#TOMCAT=tomcat
#WEB_CONTEXT_PATH=/
#WEB_PORT=8081
#WEB_AJP_PORT=8019
#WEB_ADMIN_PORT=10005
TOMCAT=`cat $NODEFILE |awk -F "=" '/TOMCAT/{print $2}'`

if [ ! "x$TOMCAT" == "x" ] ; then
    echo "find tomcat : $TOMCAT"
    if [ ! -d $BASEDIR/$TOMCAT ] ; then
        echo "tomcat not found."
        exit 1;
    fi
    if [ ! -x $BASEDIR/$TOMCAT/bin/catalina.sh ] ; then
        echo "tomcat catalina.sh not found.";
        exit 1;
    fi
else
    if [[ "x$JAVAARGS" == "x" ]]; then
        echo "Main class not found."
        exit 1;
    fi
fi

show_application_version() {
    if [[ -d $RUNDIR/.git ]] && [[ -f $RUNDIR/.git/config ]]; then
        cd $RUNDIR
        curBranch=`git branch |awk '/\*/{print $2}'`
        echo "Application version : $curBranch"
    fi
}

do_start() {

    if [ ! $USER == $RUNUSER ] ; then
       echo "current user:$USER, need to swith to $RUNUSER"
       exit 1;
    fi

    show_application_version

    cd $BASEDIR

    node=`cat $NODEFILE |awk -F "=" '/EVN_ID/{print $2}'`
    if [ "x$node" == "x" ] ; then
        node=`cat $NODEFILE |awk -F "=" '/ENV_ID/{print $2}'`
        if [ "x$node" == "x" ] ; then
            echo "node is null.";
            exit 1;
        fi
    fi

    java_opt_config="`cat $NODEFILE |awk -F "=" '/JAVAOPTS/'`"
    if [ "x$java_opt_config" == "x" ] ; then
       JAVAOPTS="$JAVAOPTS $JAVAOPTS_DEFAULT -Dspring.profiles.active=$node"
    else
       java_opt_config=`echo ${java_opt_config#*=}`
       JAVAOPTS="$JAVAOPTS $java_opt_config -Dspring.profiles.active=$node"
    fi

    if [ -d $RUNDIR/jni ] ; then
        JAVAOPTS="$JAVAOPTS -Djava.library.path=$RUNDIR/jni"
    fi

    if [ -f $BASEDIR/conf/logback.xml ] ; then
        JAVAOPTS="$JAVAOPTS -Dlogback.configurationFile=$BASEDIR/conf/logback.xml"
    fi

    if [ ! -d $BASEDIR/logs ] ; then
        mkdir $BASEDIR/logs
    fi

    if [ ! -d $BASEDIR/var ] ; then
        mkdir $BASEDIR/var
    fi

    if [ ! -d $BASEDIR/tmp ] ; then
        mkdir $BASEDIR/tmp
    fi

    if [ "x$1" == "xconsole" ] ; then
        if [ "x$TOMCAT" == "x" ] ; then
            CLASSPATH=$CLASSPATH $JAVA $JAVAOPTS -Dlogback.configurationFile=$BASEDIR/conf/logback-console.xml $JAVAARGS $node
            exit 0;
        else
            CATALINA_OPTS=$JAVAOPTS
            exec $BASEDIR/$TOMCAT/bin/catalina.sh console
        fi
    fi

    get_pid
    if [ ! "x$pid" == "x" ] && [ $pid -gt 0 ] ; then
         echo "$PROGNAME already running with PID $pid"
         exit 1;
    fi

    if [ -f $PIDFILE ] ; then
        rm -f $PIDFILE
    fi

    # encoding might be broken otherwise
    export LANG=en_US.UTF-8

    if [ "x$TOMCAT" == "x" ] ; then
        #pid=$(su -m -c "CLASSPATH=$CLASSPATH nohup $JAVA -cp $JAVAOPTS $JAVAARGS $node 1>>$SYSTEMOUT 2>>$SYSTEMERR & echo \$! " "$RUNUSER")
        pid=`CLASSPATH=$CLASSPATH nohup $JAVA $JAVAOPTS $JAVAARGS $node 1>>$SYSTEMOUT 2>>$SYSTEMERR & echo \$!`
        echo "$PROGNAME running with pid $pid"

        if [ ! $pid -gt 0 ] ; then
            echo "start failed."
            exit 1;
        fi

        echo $pid > $PIDFILE
    else
        WEB_PORT=`cat $NODEFILE |awk -F "=" '/WEB_PORT/{print $2}'`
        if [ "x$WEB_PORT" == "x" ] ; then
            WEB_PORT=8080
        else
            if [[ ! $WEB_PORT =~ ^-?[0-9]+$ ]] ; then
                echo "tomcat port : $WEB_PORT error.";
                exit 1;
            fi
        fi
        echo "use web port : $WEB_PORT"

        WEB_AJP_PORT=`cat $NODEFILE |awk -F "=" '/WEB_AJP_PORT/{print $2}'`
        if [ "x$WEB_AJP_PORT" == "x" ] ; then
            WEB_AJP_PORT=8009
        else
            if [[ ! $WEB_AJP_PORT =~ ^-?[0-9]+$ ]] ; then
                echo "tomcat ajp port : $WEB_AJP_PORT error.";
                exit 1;
            fi
        fi
        echo "use web ajp port : $WEB_AJP_PORT"

        WEB_ADMIN_PORT=`cat $NODEFILE |awk -F "=" '/WEB_ADMIN_PORT/{print $2}'`
        if [ "x$WEB_ADMIN_PORT" == "x" ] ; then
            WEB_ADMIN_PORT=8005
        else
            if [[ ! $WEB_ADMIN_PORT =~ ^-?[0-9]+$ ]] ; then
                echo "tomcat ajp port : $WEB_AJP_PORT error.";
                exit 1;
            fi
        fi
        echo "use web admin port : $WEB_ADMIN_PORT"

        WEB_CONTEXT_PATH=$CONTEXT_PATH
        if [ "x$WEB_CONTEXT_PATH" == "x" ] ; then
            WEB_CONTEXT_PATH=/
        fi

        if [ "x$DOC_BASE" == "x" ] || [ ! -d $RUNDIR/$DOC_BASE ] ; then
            echo "web doc base dir not found";
            exit 1;
        fi
        DOCBASE=$RUNDIR/$DOC_BASE
        echo "docbase is : $DOCBASE"
        echo "context path : $WEB_CONTEXT_PATH"

        if [ ! -f $TOMCAT_CONTEXT_CONFIG_TEMPLATE ] ; then
            echo "tomcat context config template file '$TOMCAT_CONTEXT_CONFIG_TEMPLATE' not found ."
            exit 1;
        fi

        if [ ! -d $BASEDIR/$TOMCAT/conf/Catalina/localhost ] ; then
            mkdir -p $BASEDIR/$TOMCAT/conf/Catalina/localhost
        fi

        if [ "x$WEB_CONTEXT_PATH" == "x/" ] ; then
            web_context_file=$BASEDIR/$TOMCAT/conf/Catalina/localhost/ROOT.xml
        else
            web_context_file=$BASEDIR/$TOMCAT/conf/Catalina/localhost/${WEB_CONTEXT_PATH:1}.xml
        fi

        if [ ! -f $web_context_file ] || [ $web_context_file -ot $0 ] ; then
            echo "create tomcat context file : $web_context_file"
            context_content=`cat $TOMCAT_CONTEXT_CONFIG_TEMPLATE`
            context_content=${context_content/DOCBASE/$DOCBASE}
            context_content=${context_content/WEB_CONTEXT_PATH/$WEB_CONTEXT_PATH}
            echo "$context_content" > $web_context_file
        fi

        if [ -f $RUNDIR/bin/server.xml ] ; then
            if [ $RUNDIR/bin/server.xml -nt $BASEDIR/$TOMCAT/conf/server.xml ] || [ $NODEFILE -nt $BASEDIR/$TOMCAT/conf/server.xml ] ; then
                echo "update tomcat server.xml"
                server_content=`cat $RUNDIR/bin/server.xml`
                server_content=${server_content/WEB_PORT/$WEB_PORT}
                server_content=${server_content/WEB_AJP_PORT/$WEB_AJP_PORT}
                server_content=${server_content/WEB_ADMIN_PORT/$WEB_ADMIN_PORT}
                echo "$server_content" > $BASEDIR/$TOMCAT/conf/server.xml
            fi
        fi

        # for a in {1..10} ; do
        # done

        export CATALINA_OPTS=$JAVAOPTS
        export CATALINA_PID=$PIDFILE
        export CATALINA_TMPDIR=$BASEDIR/tmp
        eval $BASEDIR/$TOMCAT/bin/catalina.sh start;
    fi

    # wait for process to start

    count=0;
    until [ -f $PIDFILE ] || [ $count -gt $GRACETIME ] ; do
        sleep 1
        let count=$count+1;
    done

    sleep 1

    get_pid

    # count=0;
    # until [ "x$pid" != "x" ] || [ $count -gt $GRACETIME ] ; do
    #     sleep 1
    #     let count=$count+1;
    #     get_pid
    # done

    if [ "x$pid" == "x" ] ; then
        echo "Process did not start!"
        exit 1;
    fi

    echo "Started with PID: $pid"
}

do_stop() {
    if [ ! $USER == $RUNUSER ] ; then
       echo "current user:$USER, need to swith to $RUNUSER"
       exit 1;
    fi
    get_pid
    if [ ! "x$pid" == "x" ] && [ $pid -gt 0 ] ; then
        echo "Stopping $pid"

        if [ "x$TOMCAT" == "x" ] ; then
            kill -15 $pid > /dev/null
        else
            export CATALINA_OPTS=$JAVAOPTS
            export CATALINA_PID=$PIDFILE
            export CATALINA_TMPDIR=$BASEDIR/tmp
            eval $BASEDIR/$TOMCAT/bin/catalina.sh stop
        fi

        count=0;
        until [ `ps -p $pid 2> /dev/null | grep -c $pid 2> /dev/null` -eq '0' ] || [ $count -gt $GRACETIME ] ; do
            sleep 1
            let count=$count+1;
        done

        if [ $count -gt $GRACETIME ]; then
            echo "Force stop of $PROGNAME"
            kill -9 $pid
        fi
        echo "Stopped"
    else
        echo "no running"
    fi
}

do_status() {
    show_application_version
    get_pid
    if [ ! "x$pid" == "x" ] && [ $pid -gt 0 ] ; then
        res=`ps -p $pid 2> /dev/null | grep -c $pid 2> /dev/null`
        if [ $res -eq '0' ] ; then
            echo "$PROGNAME is not running"
            exit 1;
        else
            echo "$PROGNAME is running with PID $pid"
        fi
    else
        echo "$PROGNAME is not running"
        exit 1;
    fi
}

do_netstatus() {
    do_status
    listen_info=`netstat -nolp 2>/dev/null | grep $pid`
    echo -e "Listen info : \n$listen_info"
    net_info=`netstat -anop 2>/dev/null | grep $pid`
    echo -e "Net info : \n$net_info"
}

do_signal() {
    get_pid
    if [ ! "x$pid" == "x" ] && [ $pid -gt 0 ] ; then
        echo "Signalling $pid"
        kill -s USR2 $pid > /dev/null
    fi
}

do_jstack() {
    do_status
    timestr=`date "+%Y-%m-%d-%H-%M-%S-%N"`
    stack_file="jstack_${pid}_${timestr}.log"
    echo "$JSTACK -J-d64 -F  ${pid}  > $BASEDIR/tmp/${stack_file}"
    eval $JSTACK -J-d64 -F  ${pid}  > $BASEDIR/tmp/${stack_file}
}

do_jstat() {
    do_status
    echo "$JSTAT -gcutil ${pid} 1000 5"
    eval $JSTAT -gcutil ${pid} 1000 5
}

do_jmap() {
    do_status
    #jmap -dump:format=b,file=dumpFileName pid
    #jmap -histo[:live] pid
    timestr=`date "+%Y-%m-%d-%H-%M-%S-%N"`
    map_file="jmap_${pid}_${timestr}.log"
    echo "$JMAP -heap ${pid} > $BASEDIR/tmp/${map_file}"
    eval $JMAP -heap ${pid} > $BASEDIR/tmp/${map_file}
    echo "$JMAP -histo:live ${pid} >> $BASEDIR/tmp/${map_file}"
    eval $JMAP -histo:live ${pid} >> $BASEDIR/tmp/${map_file}
}

do_jdump() {
    do_status
    #jmap -heap pid
    timestr=`date "+%Y-%m-%d-%H-%M-%S-%N"`
    map_file="jmap_dump_${pid}_${timestr}.log"
    echo "$JMAP -dump:format=b,file=$BASEDIR/tmp/${map_file} $pid"
    eval $JMAP -dump:format=b,file=$BASEDIR/tmp/${map_file} $pid
}

do_console() {
    do_start console
}

get_pid(){
    #pid=`ps -ef |grep java |grep messageServer|grep -v grep |awk '{print $2}'`
    if [ -f $PIDFILE ] ; then
        pid=`cat $PIDFILE`
    else
        pid=0
    fi

    if [ ! "x$pid" == "x0" ] && [ ! `ps -p $pid 2> /dev/null | grep -c $pid 2> /dev/null` -gt 0 ] ; then
        pid=0
    fi
}

case "$1" in
start)  echo "Starting $PROGNAME"
        do_start
        ;;
stop)  echo "Stopping $PROGNAME"
        do_stop
        ;;
status)
        do_status
        ;;
netstatus)
        do_netstatus
        ;;
signal)
        do_signal
        ;;
console)
        do_console
        ;;
jstack)
        do_jstack
        ;;
jstat)
        do_jstat
        ;;
jmap)
        do_jmap
        ;;
jdump)
        do_jdump
        ;;
restart)
        do_stop
        do_start
        ;;
*)      echo "Usage: server.sh start|stop|restart|status|netstatus|signal|console|jstack|jstat|jmap|jdump"
        exit 1
        ;;
esac
exit 0
