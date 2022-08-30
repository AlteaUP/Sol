@echo off
set ANT_OPTS=-Xmx2g -XX:MaxPermSize=1G
set ANT_HOME=%~dp0apache-ant-1.9.1
set PATH=%ANT_HOME%\bin;%PATH%
rem deleting CLASSPATH as a workaround for PLA-8702
set CLASSPATH=
echo Setting ant home to: %ANT_HOME%
ant -version
