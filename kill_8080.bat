@echo off
REM Kill the process running on the specified port
FOR /F "tokens=5" %%P IN ('netstat -a -n -o ^| findstr :8080') DO TaskKill.exe /PID %%P /F
echo 0