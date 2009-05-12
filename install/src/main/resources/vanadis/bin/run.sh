#!/bin/bash

# Copyright 2008 Kjetil Valstadsve
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

if [ "z" = $VANADIS_HOME"z" ]
then
    DIR=`dirname $0`
    VANADIS_HOME=`(cd $DIR/.. && pwd)`
fi

if [ "z" = $VANADIS_LOCATION"z" ]
then
    VANADIS_LOCATION="localhost:8000"
fi

ETC=$VANADIS_HOME/etc

LIB=$VANADIS_HOME/lib
CP=
for JAR in $VANADIS_HOME/lib/*
do
    CP=$CP:$JAR
done
CP=${ETC}${CP}

mkdir -p $VANADIS_HOME/var
echo $JAVA_HOME/bin/java $VANADIS_OPTS -classpath $CP net.sf.vanadis.main.Main \
 -blueprint-sheets vanadis-basic \
 -defaultHome $VANADIS_HOME -defaultLocation $VANADIS_LOCATION -defaultRepo repo $* \
 > $VANADIS_HOME/var/command

exec `cat $VANADIS_HOME/var/command`
