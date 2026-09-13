@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################
@rem
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

setlocal enabledelayedexpansion

set DEFAULT_JVM_OPTS=""

for %%i in ("%GRADLE_HOME%" "%JAVA_HOME%") do (
    if "%USERPROFILE%" == "" goto skip_userprofile
    for /f "tokens=*" %%j in ('dir /b /s /a:d "%USERPROFILE%\AppData\Local\Java\javahome*" 2^>nul') do (
        set "JAVA_HOME=%%j"
        goto skip_userprofile
    )
)
:skip_userprofile

if "%JAVA_HOME%"==" " (
    echo.
    echo Error: JAVA_HOME not found in your environment. !!!
    echo Please set the JAVA_HOME variable in your environment to match the
    echo location of your Java installation.
    echo.
    goto fail
)

for /F "usebackq tokens=1,2 delims==" %%A in ("%GRADLE_HOME%\gradle\wrapper\gradle-wrapper.properties") do (
  if "%%A"=="distributionUrl" set DISTRIBUTION_URL=%%~B
)

setlocal enabledelayedexpansion

if exist "%GRADLE_HOME%" (
    set GRADLE_HOME=%GRADLE_HOME%
) else (
    for /f "tokens=*" %%i in ('where gradle.bat 2^>nul') do set "GRADLE_HOME=%%~di%%~pi.."
)

if not exist "%GRADLE_HOME%" (
    echo.
    echo Error: Gradle installation not found !!!
    echo.
    goto fail
)

cd /d "%~dp0"

"%JAVA_HOME%\bin\java.exe" %DEFAULT_JVM_OPTS% -Dorg.gradle.appname=%APP_BASE_NAME% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal
endlocal & exit /b %ERRORLEVEL%
