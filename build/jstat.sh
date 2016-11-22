#!/bin/sh
#

RUNDIR=$(cd `dirname $0`; pwd)
if [ ! -d $RUNDIR/../bin ] ; then
    echo "No deploy dir."
    exit 1;
fi
$RUNDIR/server.sh jstat
