echo off
IF [%1]==[] (
  FOR /F "tokens=1* delims==" %%A IN (mViewer.properties) DO (
    IF "%%A"=="WINSTONE_PORT" set port=%%B
        )
  ECHO Using Default Http Port %port%
) ELSE (
  set port=%1
  ECHO Using Http Port %1
)
echo on
java -jar winstone-0.9.10.jar --httpPort=%port% --ajp13Port=-1 --warfile=mViewer.war