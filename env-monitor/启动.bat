@echo off
chcp 65001 >nul
title 绿境环境监测系统

echo.
echo  ================================================
echo    绿境·环境数据监测看板  v1.0
echo  ================================================
echo.

:: 优先使用系统 JAVA_HOME 环境变量
if defined JAVA_HOME (
    echo  [检测] 使用系统 JAVA_HOME: %JAVA_HOME%
    if exist "%JAVA_HOME%\bin\java.exe" (
        goto :checkJar
    ) else (
        echo  [警告] JAVA_HOME 路径无效，尝试查找 JDK 21.0.10...
    )
)

:: 尝试查找 JDK 21.0.10（相对路径）
set JAVA_HOME=%~dp0..\..\jdk-21.0.10
if not exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_HOME=%~dp0..\jdk-21.0.10
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo  [错误] 未找到可用的 JDK，请设置 JAVA_HOME 环境变量或安装 JDK 21.0.10
    pause & exit /b 1
)

echo  [检测] 使用 JDK: %JAVA_HOME%

:checkJar

:: 检查JAR包是否存在，不存在则自动编译
if not exist "%~dp0target\env-monitor-1.0.0.jar" (
    echo  [提示] 检测到未编译，正在执行 Maven 打包...
    echo.
    
    :: 检查Maven是否可用
    where mvn >nul 2>nul
    if %errorlevel% neq 0 (
        echo  [错误] 未找到 Maven，请先在 IDEA 中执行 Maven -> package 编译
        pause & exit /b 1
    )
    
    :: 执行Maven打包
    cd /d "%~dp0"
    call mvn clean package -DskipTests
    
    if %errorlevel% neq 0 (
        echo  [错误] Maven 打包失败，请检查错误信息
        pause & exit /b 1
    )
    
    if not exist "%~dp0target\env-monitor-1.0.0.jar" (
        echo  [错误] 打包后仍未找到 JAR 文件
        pause & exit /b 1
    )
    
    echo.
    echo  [成功] Maven 打包完成！
    echo.
)

echo  [1/2] 启动服务中...
echo  [提示] 首次启动需要约10-15秒，请耐心等待...
echo.

:: 启动Java应用，将输出重定向到日志文件
start "绿境环境监测系统" "%JAVA_HOME%\bin\java.exe" -jar "%~dp0target\env-monitor-1.0.0.jar" > "%~dp0startup.log" 2>&1

:: 等待服务启动
echo  [等待] 正在启动服务，请稍候...
timeout /t 12 /nobreak >nul

echo  [检查] 验证服务是否启动成功...

:: 检查端口是否监听
netstat -ano | findstr ":8888" >nul 2>nul
if %errorlevel% equ 0 (
    echo  [成功] 服务已启动！端口 8888 正在监听
    echo.
    echo  ================================================
    echo    绿境·环境数据监测看板  v1.0
    echo    访问地址：http://localhost:8888
    echo  ================================================
    echo.
    start "" "http://localhost:8888"
) else (
    echo  [警告] 服务可能未正常启动，请检查日志文件：startup.log
    echo.
    echo  [常见原因]
    echo    1. MySQL 数据库未启动
    echo    2. 端口 8888 被占用
    echo    3. 数据库配置错误
    echo.
    echo  查看最新日志：
    type "%~dp0startup.log" | more
)

echo.
echo  按任意键关闭此窗口（服务继续运行）
pause >nul
