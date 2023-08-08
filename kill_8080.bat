@echo off
REM Kill the process running on the specified port
FOR /F "tokens=5" %%P IN ('netstat -a -n -o ^| findstr :8080 ^| findstr LISTENING') DO TaskKill.exe /PID %%P /F
FOR /F "tokens=5" %%P IN ('netstat -a -n -o ^| findstr :2121 ^| findstr LISTENING') DO TaskKill.exe /PID %%P /F
echo 0