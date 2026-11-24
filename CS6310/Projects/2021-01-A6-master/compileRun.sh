#!/bin/bash

# Download jackson libs to lib folder:
#   wget -P lib/ https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.0/jackson-annotations-2.17.0.jar
# 
# Compile the source including jackson libs:
#   javac -cp ".:lib/*" A35_PowerCompany.java
#
# Run java program specifying jackson libs:
#
#   java -cp ".:lib/*" A35_PowerCompany



javac -cp ".:lib/*" A35_PowerCompany.java
java -cp ".:lib/*" A35_PowerCompany

