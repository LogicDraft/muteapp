set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
call gradlew.bat --stop
powershell -Command "Remove-Item -Path 'app\build' -Recurse -Force -ErrorAction SilentlyContinue"
call gradlew.bat assembleDebug
