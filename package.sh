#!/bin/bash 
javapackager -deploy -native image -BsystemWide=true -BjvmOptions=-Xmx4096m \
    -outdir packages -outfile BrickBreaker -srcdir target \
    -srcfiles fx-camera-0.5.jar -appclass sample.Main -name BrickBreaker \
    -title "BrickBreaker demo"