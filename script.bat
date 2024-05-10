@echo off
setlocal

set "work=D:\doc\test1"

set "libpath=C:\xampp\tomcat\lib\servlet-api.jar"

set "output=%work%\src"

set "manifest=%output%\Manifest.txt"

javac --source 17 --target 17 -cp "%libpath%" -d "%output%" "%work%\src\*.java"

cd /d "%output%\controller"

jar cvfm frontcontrol.jar %manifest% -C "%output%\controller" . -C %libpath% .

copy frontcontrol.jar "%work%\lib"

del "%output%\controller\*.class"

pause
