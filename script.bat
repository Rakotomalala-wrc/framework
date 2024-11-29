@echo off
setlocal

set "nom_projet=sprint9"

set "work=C:\xampp\tomcat\work\Sprint-S4"

set "tomcat_webapps=C:\xampp\tomcat\webapps"

set "libpath=C:\xampp\tomcat\work\Sprint-S4\lib\*"

set "bin=%work%\bin"

javac -cp "%libpath%" -d %bin% src/frameworks/*.java src/controllers/*.java src/annotations/*.java src/util/*.java

jar cf frontcontrol.jar -C "%bin%" .

copy frontcontrol.jar "%libpath%"

del frontcontrol.jar

rem ----------------------------------------------------------------------

set "repertoire_temp=%TEMP%\%nom_projet%"

set "temp_classes=%repertoire_temp%\WEB-INF\classes"

mkdir "%repertoire_temp%"

mkdir "%repertoire_temp%\WEB-INF"

mkdir "%repertoire_temp%\WEB-INF\classes"

mkdir "%repertoire_temp%\WEB-INF\lib"

copy "%work%\pages\*.jsp" "%repertoire_temp%"

copy "%work%\lib\*" "%repertoire_temp%\WEB-INF\lib"

copy "%work%\web.xml" "%repertoire_temp%\WEB-INF"

javac -cp "%libpath%" -d %temp_classes% src/controllers/*.java src/annotations/*.java src/util/*.java

IF EXIST "%repertoire_temp%\*.tmp" (
    del /s /q "%repertoire_temp%\*.tmp"
) ELSE (
    echo Aucun fichier temporaire à supprimer.
)

cd "%repertoire_temp%"
jar -cvf "%nom_projet%.war" *

move /y "%nom_projet%.war" "%tomcat_webapps%"

rmdir /S /Q "%repertoire_temp%"

echo Done!
pause