# kamtar-transport-api-configuration
Kamtar Transport API - Configuration

## Installation 

### Poste développeur
Cloner le repo en local sur son post. Dans Eclipse, Import > Existing maven Project

### Déploiement sur serveur
vim /etc/mysql/mysql.conf.d/mysqld.cnf
et ajouter çà :
sql_mode = "STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION"

* Création de la base de données et du user (cf application.properties) : 
CREATE DATABASE `kamtar-transport` CHARACTER SET utf8 COLLATE utf8_general_ci;
DROP USER backuser;
CREATE USER 'backuser'@'%' IDENTIFIED BY 'XXX';
GRANT USAGE ON *.* TO 'backuser'@'%' REQUIRE NONE WITH MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0;
GRANT ALL PRIVILEGES ON `kamtar-transport`.* TO 'backuser'@'%';
FLUSH PRIVILEGES;

vim /etc/mysql/mysql.conf.d/mysqld.cnf
et supprimer çà :
sql_mode = "STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION"

* Création des répertoires : 
<pre><code>mkdir -p /var/kamtar-transport/jars/kamtar-transport-api
mkdir -p /var/log/kamtar-transport/
> /var/log/kamtar-transport/kamtar-transport-api-log.log
</code></pre>
* Dépot du JAR, application.properties, start.sh, stop.sh et le log4j2.xml dans /var/kamtar-transport/jars/kamtar-transport-api
* Création du JKS : 
<pre><code>openssl pkcs12 -export -out kamtar.p12 -inkey kamtar.key -in certificate-639696.crt -certfile cert-chain.crt (il faut mettre un mot de passe - le cert-chain.crt  est à récupérer sur kamtar ou carbella)
keytool -importkeystore -srckeystore kamtar.p12 -srcstoretype pkcs12 -destkeystore kamtar.jks -deststoretype JKS (il faut mettre un mot de passe)
keytool -importkeystore -srckeystore kamtar.jks -destkeystore kamtar.jks -deststoretype pkcs12
</code></pre>

* Activation du SSL dans application.properties
* Adapter l'application.properties, start.sh, stop.sh et le log4j2.xml
* mvn clean package
* Déposer le jar, le log4j2.xml, start.sh et l'application.properties au même endroit

* Penser à ouvrir le port coté AWS


* Monter le service
<pre><code>
sudo useradd kamtar-transport-api
sudo passwd kamtar-transport-api
sudo chown kamtar-transport-api:kamtar-transport-api /var/kamtar-transport/jars/kamtar-transport-api/kamtar-transport-api-0.0.3-SNAPSHOT.jar
sudo chmod 500 /var/kamtar-transport/jars/kamtar-transport-api/kamtar-transport-api-0.0.3-SNAPSHOT.jar
chown kamtar-transport-api:kamtar-transport-api /var/kamtar-transport/jars/kamtar-transport-api/*
chown kamtar-transport-api:kamtar-transport-api /var/log/kamtar-transport/*
chown kamtar-transport-api:kamtar-transport-api /var/log/kamtar-transport
</code></pre>

<pre><code>vim /etc/systemd/system/kamtar-transport-api.service</code></pre>

<pre><code>
[Unit]
Description=Microservice de backend de kamtar transport
After=syslog.target
 
[Service]
User=kamtar-transport-api
WorkingDirectory=/var/kamtar-transport/jars/kamtar-transport-api/
ExecStart=/usr/bin/java -jar kamtar-transport-api-0.0.3-SNAPSHOT.jar -Dfile.encoding=UTF-8 -XX:+UseSerialGC -Xss512k -XX:MaxRAM=72m
SuccessExitStatus=143 
TimeoutStopSec=10
Restart=on-failure
RestartSec=5
AmbientCapabilities=CAP_SYS_RAWIO

[Install] 
WantedBy=multi-user.target
</code></pre>


Relivrer et relancer le service
<pre><code>
mv /home/admin/kamtar-transport-api-0.0.3-SNAPSHOT.jar /var/kamtar-transport/jars/kamtar-transport-api/
systemctl daemon-reload
systemctl enable kamtar-transport-api.service
systemctl restart kamtar-transport-api
systemctl status kamtar-transport-api
tail -100f /var/log/kamtar-transport/kamtar-transport-api-logs.log
</code></pre>


Autres commandes à connaître
<pre><code>
systemctl start kamtar-transport-api
systemctl stop kamtar-transport-api
</code></pre>



## Commandes Maven
* mvn clean package
* Création du JAR : kamtar-transport-api package
* Lancement : clean spring-boot:run

