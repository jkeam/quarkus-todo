#!/bin/bash

# mvn clean package
mvn exec:java -Dexec.mainClass=io.keam.GenerateToken -Dexec.classpathScope=test -Dsmallrye.jwt.sign.key.location=privateKey.pem
