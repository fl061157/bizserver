#!/bin/sh
#

SCRIPT_FULLPATH=$(cd "$(dirname "$0")"; pwd)
BASEDIR=$SCRIPT_FULLPATH/..

if [[ ! -f $SCRIPT_FULLPATH/project.sh ]]; then
    echo "project.sh not found."
    exit 1;
fi

source $SCRIPT_FULLPATH/project.sh

DEPLOYDIR=$BASEDIR/../$PROJECTFOLDER

CHANGELOG=""
CLASSLOG=""
GITLOG=""

extname=${JARNAME##*.}
if [[ "x$extname" == "xjar" ]] || [[ "x$extname" == "xwar" ]] ; then
    echo "build $extname project."
else
    echo "unknow project type."
    exit 1;
fi

check_deploy_dirs () {
    echo "check deplay dir '$DEPLOYDIR'"
    if [ ! -d $DEPLOYDIR ] ; then
        echo "dir '$DEPLOYDIR' not found."
        exit 1;
    fi

    for subdir in conf jni bin ; do
        if [ ! -d $DEPLOYDIR/$subdir ] ; then
            echo "mkdir $DEPLOYDIR/$subdir"
            mkdir $DEPLOYDIR/$subdir
        fi
    done

    if [[ "x$extname" == "xjar" ]] ; then
        if [ ! -d $DEPLOYDIR/lib ] ; then
            echo "mkdir $DEPLOYDIR/lib"
            mkdir $DEPLOYDIR/lib
        fi
    fi
}

build_jar() {
    echo "build jar."
    mvn clean || exit 1;
    mvn -Dmaven.test.skip=true package || exit 1;
    echo "build jar done."
}

copy_jar() {
    if [[ ! -f $BASEDIR/target/$JARNAME ]]; then
        echo "file $BASEDIR/target/$JARNAME not found."
        exit 1;
    fi
    if [[ "x$extname" == "xjar" ]]; then
        cp -f $BASEDIR/target/$JARNAME $BASEDIR/target/lib/ || exit 1;
    elif [[ "x$extname" == "xwar" ]]; then
        if [[ "x$DOC_BASE" == "x" ]]; then
            echo "DOC_BASE not found."
            exit 1;
        fi
        echo "use doc base : $DEPLOYDIR/$DOC_BASE"
        if [[ ! -d $DEPLOYDIR/$DOC_BASE ]]; then
            echo "mkdir $DEPLOYDIR/$DOC_BASE"
            mkdir -p $DEPLOYDIR/$DOC_BASE
        fi
        if [[ -d $BASEDIR/target/war_temp ]]; then
            rm -rf $BASEDIR/target/war_temp
        fi
        mkdir -p $BASEDIR/target/war_temp
        cd $BASEDIR/target/war_temp
        jar xf $BASEDIR/target/$JARNAME
        cd $BASEDIR
        copy_dir target/war_temp $DOC_BASE
        rm -rf $BASEDIR/target/war_temp
    else
        echo "unknow file type."
        exit 1;
    fi
}

copy_dir() {
    echo "copy dir : $1 to $2"
    if [ ! -d $BASEDIR/$1 ] ; then
        echo "dir '$BASEDIR/$1' not found."
        return;
    fi

    if [ ! -d $DEPLOYDIR/$2 ] ; then
        echo "mdkdir $2"
        CHANGELOG="$CHANGELOG \nmkdir $2"
        mkdir $DEPLOYDIR/$2
    fi

    for destfile in $DEPLOYDIR/$2/* ; do
        destfilename=${destfile##*/}
        if [ -f $destfile ] && [ ! -f $BASEDIR/$1/$destfilename ] ; then
            CHANGELOG="$CHANGELOG \ndelete $2/$destfilename"
            echo "remove file : $2/$destfilename"
            rm -f $destfile
        fi
        if [ -d $destfile ] && [ ! -d $BASEDIR/$1/$destfilename ]; then
            echo "remove dir : $2/$destfilename"
            rm -rf $destfile
        fi
    done

    for copyfile in $BASEDIR/$1/* ; do
        copyfilename=${copyfile##*/}
        if [ -d $copyfile ] ; then
            echo "copy dir : $1/$copyfilename"
            copy_dir $1/$copyfilename $2/$copyfilename
        elif [ -f $copyfile ] ; then
            if [ -f $DEPLOYDIR/$2/$copyfilename ] ; then
                diff=`diff $copyfile $DEPLOYDIR/$2/$copyfilename`
                if [ ! "x${diff}" == "x" ] ; then
                    echo "remove old file : $2/$copyfilename"
                    rm $DEPLOYDIR/$2/$copyfilename
                    CHANGELOG="$CHANGELOG \nmodify $2/$copyfilename"
                    echo "copy file : $2/$copyfilename"
                    cp $copyfile $DEPLOYDIR/$2/$copyfilename
                fi
            else
                CHANGELOG="$CHANGELOG \nadd $2/$copyfilename"
                echo "copy file : $2/$copyfilename"
                cp $copyfile $DEPLOYDIR/$2/$copyfilename
            fi
        fi
    done
}

copy_file() {
    if [ -f $BASEDIR/$1 ] ; then
        pdir=`dirname $DEPLOYDIR/$2`
        if [ ! -d  $pdir ] ; then
            mkdir -p $pdir
        fi
        if [ -f $DEPLOYDIR/$2 ] ; then
            diff=`diff $BASEDIR/$1 $DEPLOYDIR/$2`
            if [ ! "x${diff}" == "x" ] ; then
                echo "remove old file : $2"
                rm -f $DEPLOYDIR/$2
                CHANGELOG="$CHANGELOG \nadd $2"
                echo "copy file : $1 to $2"
                cp $BASEDIR/$1 $DEPLOYDIR/$2
            fi
        else
            CHANGELOG="$CHANGELOG \nadd $2"
            echo "copy file : $2"
            cp $BASEDIR/$1 $DEPLOYDIR/$2
        fi
    fi
}

get_diff_info() {
    echo "get $JARNAME diff info."
    if [ -f $DEPLOYDIR/lib/$JARNAME ] ; then
        if [ -d $BASEDIR/target/classes ] ; then
            if [ ! -d $BASEDIR/target/tmp ];
            then
                mkdir $BASEDIR/target/tmp
            fi
            cd $BASEDIR/target/tmp
            jar xf $DEPLOYDIR/lib/$JARNAME || exit 1;
            cd $BASEDIR
            for cfile in `find $BASEDIR/target/tmp` ;
            do
                if [ -f $cfile ] ; then
                    classfile=${cfile#$BASEDIR/target/tmp/*}
                    if [ ! "x$classfile" == "xMETA-INF/MANIFEST.MF" ] ; then
                        if [ ! -f $BASEDIR/target/classes/$classfile ] ; then
                            echo "delete $classfile"
                            CLASSLOG="$CLASSLOG\ndelete $classfile"
                        fi
                    fi
                fi
            done

            for cfile in `find $BASEDIR/target/classes` ; do
                if [ -f $cfile ] ; then
                    classfile=${cfile#$BASEDIR/target/classes/*}
                    if [ "x${classfile##*.}" == "xclass" ] ; then
                        #echo "diff file $classfile"
                        diff=`diff $cfile $BASEDIR/target/tmp/$classfile`
                        if [ ! "x${diff}" == "x" ]
                        then
                            echo "class change : $classfile"
                            CLASSLOG="$CLASSLOG\nupdate $classfile"
                        fi
                    fi
                fi
            done
        fi
    fi

    rm -rf $BASEDIR/target/tmp

    if [ "x$CLASSLOG" == "x" ] ; then
        echo "no class file changed"
    else
        echo -e "changed class files : $CLASSLOG"
    fi
}

get_version() {
    cd $BASEDIR
    ver=`git rev-list --all | head -1`
    now=`date +"%Y-%m-%d %H:%M:%S"`
    echo "$now $ver" >> $DEPLOYDIR/version.txt
}

update_changelog() {
    now=`date +"%Y-%m-%d %H:%M:%S"`
    echo -e "---\n$now" >> $DEPLOYDIR/changelogs.txt

    if [[ -f $DEPLOYDIR/changetime.txt ]]; then
        since=`tail -1 $DEPLOYDIR/changetime.txt`
    fi

    echo $now >> $DEPLOYDIR/changetime.txt

    if [[ "x$since" == "x" ]]; then
        since="2.weeks"
    fi

    GITLOG=`cd $BASEDIR; git log --pretty=format:"%h - %ad : %an : %s" --since="$since" --date=iso`

    echo -e "git commit log : $GITLOG"

    if [ ! "x$CHANGELOG" == "x" ] ; then
        echo -e "$CHANGELOG" >> $DEPLOYDIR/changelogs.txt
    fi

    if [ "x$CLASSLOG" == "x" ]; then
        echo -e "no class file changed" >> $DEPLOYDIR/changelogs.txt;
    else
        echo -e "changed class files : $CLASSLOG" >> $DEPLOYDIR/changelogs.txt;
    fi
}

change_branch(){
    cd $BASEDIR
    curBranch=`git branch |awk '/\*/{print $2}'`
    echo "current_branch:$curBranch"
    cd $DEPLOYDIR
    dstBranch=`git branch |awk '/\*/{print $2}'`
    if [ ! "x$curBranch" == "x$dstBranch" ] ; then
        fetchResult=`git fetch`
        ret=$?
        if [[ $ret != 0 ]]; then
            echo "git fetch. error."
            exit 1;
        fi
        export curBranch
        containCurBranch=`git branch |awk '/'"$curBranch"'/{print $0}'|tr -d "[*][ ]"`
        if [ ! "x$curBranch" == "x$containCurBranch" ] ; then
            result=`git checkout -b $curBranch`
            ret=$?
            echo "git checkout result:$result"
            if [[ $ret != 0 ]]; then
                echo "git checkout -b $curBranch. error."
                exit 1;
            fi
        else
            result=`git checkout $curBranch`
            ret=$?
            echo "git checkout result:$result"
            if [[ $ret != 0 ]]; then
                echo "git checkout $curBranch. error."
                exit 1;
            fi
        fi
        echo "swith $dstBranch to $curBranch"
    fi
    result=`git pull origin $curBranch`
    ret=$?
    echo "git pull result:$result"
    if [[ $ret != 0 ]]; then
        echo "git pull error."
    fi
}

git_push(){
    cd $DEPLOYDIR
    curBranch=`git branch |awk '/\*/{print $2}'`
    commitinfo="package, commits : $GITLOG"
    result=`git add --all && git commit -m "$commitinfo" && git push --progress origin $curBranch:$curBranch`
    ret=$?
    echo "git push result:$result"
    if [[ $ret != 0 ]]; then
        echo "git push error."
        exit 1;
    fi
    tag=`date "+%Y-%m-%d-%H%M%S"`
    result=`git tag -a V${tag} -m "deploy"`
    ret=$?
    echo "git tag as V${tag} result:$result"
    if [[ $ret != 0 ]]; then
        echo "git tag error."
        exit 1;
    fi
}

cd $BASEDIR
check_deploy_dirs
build_jar
change_branch
copy_jar
if [[ "x$extname" == "xjar" ]] ; then
    get_diff_info
    copy_dir target/lib lib
    copy_dir src/main/resources conf
fi
copy_dir jni jni
copy_file build/server.sh bin/server.sh
copy_file build/start.sh bin/start.sh
copy_file build/stop.sh bin/stop.sh
copy_file build/status.sh bin/status.sh
copy_file build/netstatus.sh bin/netstatus.sh
copy_file build/update.sh bin/update.sh
copy_file build/project.sh bin/project.sh
copy_file build/jstack.sh bin/jstack.sh
copy_file build/jstat.sh bin/jstat.sh
copy_file build/jmap.sh bin/jmap.sh
copy_file build/jdump.sh bin/jdump.sh
if [[ "x$extname" == "xwar" ]] ; then
    copy_file build/server.xml bin/server.xml
    copy_file build/context.xml bin/context.xml
fi
chmod +x $DEPLOYDIR/bin/*.sh
get_version
update_changelog
git_push
