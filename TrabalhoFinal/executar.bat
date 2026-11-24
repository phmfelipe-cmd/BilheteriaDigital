@echo off
echo Compilando e iniciando Bilheteria Digital...
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin -sourcepath src src\com\bilheteria\model\*.java src\com\bilheteria\service\*.java src\com\bilheteria\ui\*.java src\com\bilheteria\Main.java
if %errorlevel% neq 0 (
    echo [ERRO] Falha na compilacao.
    pause
    exit /b %errorlevel%
)
start javaw -Dfile.encoding=UTF-8 -cp bin com.bilheteria.Main
exit
