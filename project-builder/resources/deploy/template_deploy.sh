#!/bin/bash


## --- ?? -----------------------------------------------------------
echo "Configure the script KPLSTHXBYE!" && exit 1


## TODO: inserire la versione corretta della JDK
export JAVA_HOME=/opt/jdk1.7.0_40
ENV=[STAGING|PRODUCTION]
## TODO: inserire il numero massimo di nodi
NODE=0[0-9]


## --- ?? -----------------------------------------------------------
#/etc/init.d/hybris stop 


## --- Unpack -------------------------------------------------------
cd /opt/hybris/deploy &&

## $1 = dd-mm-yyyy_hh-mm-ss
tar xvf /tmp/dist_$1.tar &&
rm -fr /opt/hybris/hybris/bin/* &&
unzip hybrisServer-Platform.zip -d /opt/hybris/ &&
unzip hybrisServer-AllExtensions.zip -d /opt/hybris/ &&
unzip hybrisServer-ExternalExtensions.zip -d /opt/hybris/ &&
unzip hybrisServer-Config.zip -d /opt/hybris/ &&

chmod +x /opt/hybris/hybris/bin/ext-platform-optional/mediaconversion/resources/mediaconversion/imagemagick/bin/linux/x86_64/* &&


## ---- Copy local config -------------------------------------------
cp -r /opt/hybris/hybris/config/local.properties.default /opt/hybris/hybris/config/local.properties
cp -r /opt/hybris/hybris/config/localextensions.xml.default /opt/hybris/hybris/config/localextensions.xml


## ---- Override config ---------------------------------------------
##cp -r hybris/config /opt/hybris/hybris/ &&
##cp hybris/config/local$ENV$NODE.properties /opt/hybris/hybris/config/local.properties &&
##cat hybris/config/localCommon$ENV.properties >> /opt/hybris/hybris/config/local.properties &&


## ---- Build -------------------------------------------------------
cd /opt/hybris/hybris/bin/platform && . ./setantenv.sh &&
ant clean &&
ant customize all &&

##cp /opt/hybris/deploy/*.war /opt/hybris/hybris/bin/platform/tomcat/hybris/webapps/ &&
cp /opt/hybris/deploy/*.war /opt/hybris/hybris/bin/platform/tcServer/hybris/webapps/ &&
ant deploy 


## --- ?? -----------------------------------------------------------
#/etc/init.d/hybris start
