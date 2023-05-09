#!/bin/bash
mvn package
jpackage  --type dmg --java-options '--enable-preview' --name Booth  -i target -n booth --main-class origami.booth.Main --main-jar booth-*.jar
