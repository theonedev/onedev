@echo off
setlocal
rem Java service launcher. Service management prompts for administrator permission when needed.
set "_ARCH=%PROCESSOR_ARCHITECTURE%"
if defined PROCESSOR_ARCHITEW6432 set "_ARCH=%PROCESSOR_ARCHITEW6432%"
if /i "%_ARCH%"=="AMD64" set "_BINARY=wrapper-windows-x86-64.exe"
if /i "%_ARCH%"=="ARM64" set "_BINARY=wrapper-windows-arm-64.exe"
if not defined _BINARY (
    echo Wrapper requires Windows amd64 or arm64. 1>&2
    exit /b 1
)
"%~dp0..\boot\%_BINARY%" --script "%~n0" "%~dp0..\conf\wrapper.conf" %*
exit /b %ERRORLEVEL%
