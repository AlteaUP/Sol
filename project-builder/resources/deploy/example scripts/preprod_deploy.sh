#!/bin/bash
## Copy dist zips + hybris/config + clswebervices.war in /opt/hybris/deploy -> launch -> ??? -> PROFIT

## "l'esercizio é lasciato al lettore"
echo "Configure the script KPLSTHXBYE!" && exit 1

export JAVA_HOME=/opt/jdk1.6.0_33
ENV=[STAGING|PRODUCTION]
NODE=0[0-9]

#/etc/init.d/hybris stop 

## unpack
cd /opt/hybris/deploy &&
tar xvf /tmp/dist.tar &&
rm -fr /opt/hybris/hybris/bin/* &&
unzip hybrisServer-Platform.zip -d /opt/hybris/  && unzip hybrisServer-AllExtensions.zip -d /opt/hybris/ &&
chmod +x /opt/hybris/hybris/bin/ext-content/mediaconversion/resources/mediaconversion/imagemagick/bin/linux/x86_64/* &&
## broken build callbacks, CXF libs missing. whatever, no time to actually fix it.
sed -i.bak '/<macrodef name=".*_before_build">/,/<\/macrodef>/d' /opt/hybris/hybris/bin/custom/sgmfulfilment/buildcallbacks.xml &&
sed -i.bak '/<macrodef name=".*_before_build">/,/<\/macrodef>/d' /opt/hybris/hybris/bin/custom/connectors/barclaycard/buildcallbacks.xml &&
sed -i.bak '/<macrodef name=".*_before_build">/,/<\/macrodef>/d' /opt/hybris/hybris/bin/custom/connectors/cls/buildcallbacks.xml &&
sed -i.bak '/<macrodef name=".*_before_build">/,/<\/macrodef>/d' /opt/hybris/hybris/bin/custom/connectors/valuelab/buildcallbacks.xml &&

## override config
cp -r hybris/config /opt/hybris/hybris/ &&
cp hybris/config/local$ENV$NODE.properties /opt/hybris/hybris/config/local.properties &&
cat hybris/config/localCommon$ENV.properties >> /opt/hybris/hybris/config/local.properties &&

## build
cd /opt/hybris/hybris/bin/platform && . ./setantenv.sh &&
ant clean &&
ant customize all &&
cp /opt/hybris/deploy/*.war /opt/hybris/hybris/bin/platform/tcServer-6.0/hybris/webapps/ &&
ant deploy 

## on [pre]production only!
# && cp -r /opt/hybris/hybris/bin/custom/b2c/b2cstorefront/web/webroot/_ui/ /DCROOT/hybris/
#chmod -R u=rwx /DCROOT/hybris/_ui/
#chmod -R go=rx /DCROOT/hybris/_ui/
#/etc/init.d/hybris start
