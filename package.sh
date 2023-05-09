#!/bin/bash

jpackage  --type dmg --java-options '--enable-preview' --name Booth  -i target -n booth --main-class origami.booth.Main --main-jar booth-0.9.1.jar