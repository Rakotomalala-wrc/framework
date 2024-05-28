@echo off
setlocal

set "work=D:\BOSY\L2\S4\Mr Naina\SPRINT\SPRINT\src\com\example"

set "libpath=C:\xampp\tomcat\lib\servlet-api.jar"

set "output=%work%\src"

set "%temp=%output%\temp"
 
copy "%output%\annotations\*.java" "%temp%"

copy "%output%\frameworks\*.java" "%temp%"

copy "%output%\controllers\*.java" "%temp%"

copy "%output%\util\*.java" "%temp%"

javac -cp "%libpath%" -d "%output%" "%temp%*.java"

cd /d "%output%\controller"

jar cvf frontcontrol.jar -C "%output%\frameworks" . -C %libpath% .

copy frontcontrol.jar "%work%\jar"

del "%output%\controllers\*.class"

del "%output%\frameworks\*.class"

del "%output%\util\*.class"

del "%output%\annotations\*.class"

pause
