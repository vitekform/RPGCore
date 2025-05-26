# Build artifact
mvn clean package

# Clean plugins directory
if (Test-Path .\srv\plugins\*.jar) {
    Remove-Item .\srv\plugins\*.jar
}

Copy-Item .\target\rpgcore-*.jar -Destination .\srv\plugins\

# Download server if not exists
if (-not (Test-Path .\srv\server.jar)) {
    Write-Host "server.jar not found!"
    Invoke-WebRequest -Uri "https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/231/downloads/paper-1.21.4-231.jar" -OutFile ".\srv\server.jar"
}

cd .\srv
# Start the server
java -Xmx2048M -Xms2048M -jar server.jar -nogui
cd ..
clear
exit