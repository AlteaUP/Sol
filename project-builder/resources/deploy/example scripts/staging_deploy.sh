#!/bin/bash

## "l'esercizio é lasciato al lettore"
echo "Configure the script KPLSTHXBYE!" && exit 1

export JAVA_HOME=/opt/jdk1.6.0_33
ENV=[STAGING|PRODUCTION]
NODE=0[0-9]

#/etc/init.d/hybris stop 

if [ -z $1 ];
        then echo "Missing release tag! Usage: $0 tag" && exit 0; fi

cd /opt/hybris/hybris &&
# TODO: svn magic
svn switch https://mprsvn01/svn/MPS/tags/$1 . &&
chmod +x /opt/hybris/hybris/bin/ext-content/mediaconversion/resources/mediaconversion/imagemagick/bin/linux/x86_64/* &&
## override config
cp /opt/hybris/hybris/config/local$ENV$NODE.properties /opt/hybris/hybris/config/local.properties &&
cat /opt/hybris/hybris/config/localCommon$ENV.properties >> /opt/hybris/hybris/config/local.properties &&

## build
cd /opt/hybris/hybris/bin/platform && . ./setantenv.sh &&
ant clean &&
ant customize all &&
cp /opt/hybris/deploy/*.war /opt/hybris/hybris/bin/platform/tcServer-6.0/hybris/webapps/ &&
ant deploy 

#/etc/init.d/hybris start