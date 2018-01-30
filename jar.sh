#!/bin/sh
java -cp classes apo.tools.ManifestGenerator
/bin/rm -f apo.jar
jar cfm apo.jar resource/apo.manifest.mf -C classes . || exit 1
/bin/rm -f aposervice.jar
jar cfm aposervice.jar resource/aposervice.manifest.mf -C classes . || exit 1

echo "jar files generated successfully"
