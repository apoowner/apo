#!/bin/sh
VERSION=$1
if [ -x ${VERSION} ];
then
	echo VERSION not defined
	exit 1
fi
PACKAGE=apo-client-${VERSION}.zip
echo PACKAGE="${PACKAGE}"

FILES="changelogs classes conf html lib src resource addons"
FILES="${FILES} apo.jar aposervice.jar"
FILES="${FILES} 3RD-PARTY-LICENSES.txt AUTHORS.txt COPYING.txt LICENSE.txt"
FILES="${FILES} DEVELOPERS-GUIDE.md OPERATORS-GUIDE.md README.md README.txt USERS-GUIDE.md"
FILES="${FILES} mint.bat mint.sh run.bat run.sh run-tor.sh run-desktop.sh start.sh stop.sh compact.sh compact.bat sign.sh"
FILES="${FILES} apo.policy apodesktop.policy APO_Wallet.url"
FILES="${FILES} compile.sh javadoc.sh jar.sh package.sh"
FILES="${FILES} win-compile.sh win-javadoc.sh win-package.sh"

echo compile
./win-compile.sh
echo jar
./jar.sh
echo javadoc
rm -rf html/doc/*
./win-javadoc.sh

rm -rf apo
rm -rf ${PACKAGE}
mkdir -p apo/
mkdir -p apo/logs
echo copy resources
cp -a ${FILES} apo
echo gzip
for f in `find apo/html -name *.gz`
do
	rm -f "$f"
done
for f in `find apo/html -name *.html -o -name *.js -o -name *.css -o -name *.json -o -name *.ttf -o -name *.svg -o -name *.otf`
do
	gzip -9c "$f" > "$f".gz
done
echo zip
zip -q -X -r ${PACKAGE} apo -x \*/.idea/\* \*/.gitignore \*/.git/\* \*/\*.log \*.iml apo/conf/apo.properties apo/conf/logging.properties apo/conf/localstorage/\*
rm -rf apo
echo done