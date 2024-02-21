-- OLD
#!/bin/sh
echo "Usage: run.sh ( commands ... )  ./run.sh &>/dev/null &"
#/usr/java/jre1.8.0_65/bin/java -Xmx384m -jar fcweb-project-1.0.0.jar
/jdk-11.0.5/bin/java --add-exports java.desktop/sun.font=ALL-UNNAMED -Xmx384m -jar fcweb-project-1.0.0.jar
/jdk-11.0.5/bin/java --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED -jar fcweb-project-1.0.0.jar
echo "exit"
exit 


-- NEW 
nohup -/run.sh &

#!/bin/sh
java -Xmx512m \
	-Dserver.port="8080" \
	-Dspring.datasource.url="jdbc:mysql://172.17.0.1:3306/fcdev2021?serverTimezone=GMT" \
	-Dspring.datasource.username=fcdev2021 \
	-jar fcweb-project-1.0.0.jar

echo "exit"

exit 
