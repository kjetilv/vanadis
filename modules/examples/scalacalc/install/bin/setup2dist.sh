#!/bin/sh

rm -rf target/vanadis-main/
cp -R target/vanadis-felix target/vanadis-main

rm -rf target/vanadis-calcers/
cp -R target/vanadis-felix target/vanadis-calcers

for FIL in adder subtractor divisor multiplier
do
    rm target/vanadis-main/deploy/launch/$FIL.launch.xml
done
for FIL in calculator
do
    rm target/vanadis-calcers/deploy/launch/$FIL.launch.xml
done

cp etc/setup2dist/networker-client.xml target/vanadis-calcers/deploy/launch/networker.xml
cp etc/setup2dist/networker-router.xml target/vanadis-main/deploy/launch/networker.xml
