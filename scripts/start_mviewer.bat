if [%1]==[](
  For /F "tokens=1* delims==" %%A IN (mViewer.properties) DO (
	IF "%%A"=="WINSTONE_PORT" set port=%%B
        )
  ECHO Using Default Http Port %port%
) ELSE(
  port=%1
  ECHO Using Http Port %port%
)

java -jar winstone-0.9.10.jar --httpPort=%port% --ajp13Port=-1 --warfile=mViewer.war

