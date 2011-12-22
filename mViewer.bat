@ECHO OFF
IF DEFINED JAVA_HOME (
	IF DEFINED ANT_HOME (
		REM Given the Full path for ant because it might be the case that ant is not in the path
		CALL %ANT_HOME%/bin/ant
		CALL pause 
		CALL %ANT_HOME%/bin/ant start
	)ELSE (
		ECHO -----------------------------------------------------------------------
		ECHO ANT_HOME is NOT defined. Please download ant and set ANT_HOME variable
		ECHO -----------------------------------------------------------------------
	)
)ELSE (
	ECHO -------------------------
	ECHO JAVA_HOME is NOT defined. 
	ECHO -------------------------
	)
pause