#!/bin/bash

#sudo chmod a+x /home/apo/update_server_param.sh 

clear
#service apo-server stop > /dev/null 2>&1 &
echo "Stop APO server"

/etc/init.d/apo-server stop > /dev/null 2>&1 &
echo "wait 5 secs..."
sleep 5
cd /home/apo
#rm -rf apo_db

myAddress=`ifconfig eth0 2>/dev/null|awk '/inet / {print $2}'|sed 's/addr://'`;
echo 'myIP='$myAddress

myPlatformDefault='APONode-'$myAddress
homeDirDefault=/home/apo
reductorFeeDefault=100

#echo "Type the home dir ($homeDirDefault):"
#read homeDir
if [ -z "$homeDir" ]; then
	homeDir=$homeDirDefault
fi
#echo "Type the platform name to be announced to peers ($myPlatformDefault):"
#read myPlatform
#if [ -z "$myPlatform" ]; then
#	myPlatform=$myPlatformDefault
#fi
#echo "Type the value of reductorFee ($reductorFeeDefault):"
#read reductorFee
if [ -z "$reductorFee" ]; then
	reductorFee=$reductorFeeDefault
fi
#echo "Type the server HALLMARK:"
#read myHallmark
if [ -z "$reductorFee" ]; then
	reductorFee=1
fi
#echo "Type the forger secret phrase:"
#read forgerSecretPhrase
#forgerSecretPhrase=$(sed 's/[\/&]/\\&/g' <<<"$forgerSecretPhrase")
#echo $forgerSecretPhrase

#PEERNODE=(139.162.47.229 139.162.106.228 139.162.180.60 178.79.132.8 45.79.66.180)
#PEERNODE=(sg.r6s6.com jp.r6s6.com de.r6s6.com uk.r6s6.com us.r6s6.com)

case $myAddress in
     139.162.47.229|139.162.106.228|139.162.180.60|178.79.132.8|45.79.66.180)
          #PEERNODE=(sg.r6s6.com jp.r6s6.com de.r6s6.com uk.r6s6.com us.r6s6.com)
          PEERNODE=(139.162.47.229 139.162.106.228 139.162.180.60 178.79.132.8 45.79.66.180)
          PEERNODEBLACKLIST=(172.104.86.9 172.104.60.30 172.104.146.203 178.79.130.64 45.56.116.43 172.104.49.201 172.104.93.22 172.104.143.175 176.58.120.246 45.79.153.137)
          ;;
     172.104.86.9|172.104.60.30|172.104.146.203|178.79.130.64|45.56.116.43)
          PEERNODE=(172.104.86.9 172.104.60.30 172.104.146.203 178.79.130.64 45.56.116.43)
          PEERNODEBLACKLIST=(139.162.47.229 139.162.106.228 139.162.180.60 178.79.132.8 45.79.66.180 172.104.49.201 172.104.93.22 172.104.143.175 176.58.120.246 45.79.153.137)
          ;;
     172.104.39.118|172.104.42.165|172.104.51.191|172.104.51.252|172.104.59.231|139.162.28.101)
          #PEERNODE=(172.104.39.118 172.104.109.68 172.104.147.97 212.111.40.153 198.74.56.18)
          #PEERNODE=(sg.apoacc.com jp.apoacc.com de.apoacc.com uk.apoacc.com us.apoacc.com)
          PEERNODE=(192.168.128.151 192.168.133.36 192.168.154.229 192.168.144.225 192.168.146.185 192.168.152.101)
          #PEERNODEBLACKLIST=(139.162.47.229 139.162.106.228 139.162.180.60 178.79.132.8 45.79.66.180 172.104.86.9 172.104.60.30 172.104.146.203 178.79.130.64 45.56.116.43)
          ;;
     
     *)
          echo 'Undefined myAddress '$myAdress
          exit
          ;;
esac

file=conf/apo-default.properties.base
file_new=conf/apo-default.properties
#peerServerPort=27874
#apiServerPort=27876
#apiServerSSLPort=27876
#uiServerPort=27875

peerServerPort=2052
apiServerPort=2082
apiServerSSLPort=2082
uiServerPort=2086

currencyName=APO
enableApiTestUI=false
disabledAPITags='Aliases;Digital Goods Store;Monetary System;Debug;Add-ons;'
#ACCOUNTS("Accounts"), ACCOUNT_CONTROL("Account Control"), ALIASES("Aliases"), AE("Asset Exchange"), BLOCKS("Blocks"),
#CREATE_TRANSACTION("Create Transaction"), DGS("Digital Goods Store"), FORGING("Forging"), MESSAGES("Messages"),
#MS("Monetary System"), NETWORK("Networking"), PHASING("Phasing"), SEARCH("Search"), INFO("Server Info"),
#SHUFFLING("Shuffling"), DATA("Tagged Data"), TOKENS("Tokens"), TRANSACTIONS("Transactions"), VS("Voting System"),
#UTILS("Utils"), DEBUG("Debug"), ADDONS("Add-ons");

for PEER in "${PEERNODE[@]}"
do
	if [ $PEER != $myAddress ] ; then
		wellKnownPeers=$wellKnownPeers$PEER'; '
	fi
done
for PEER in "${PEERNODEBLACKLIST[@]}"
do
	if [ $PEER != $myAddress ] ; then
		knownBlacklistedPeers=$knownBlacklistedPeers$PEER'; '
	fi
done

maxPrunableLifetime=1209600
case $myAddress in
     139.162.47.229|172.104.86.9|172.104.39.118)      
          forgerSecretPhrase=''
          myHallmark=''
          maxPrunableLifetime=-1
			case $myAddress in
			     172.104.39.118|172.104.42.165|172.104.51.191|172.104.51.252|172.104.59.231|139.162.28.101)
						myDomain="192.168.128.151"
						#myDomain='sg.apoacc.com'
			          ;;
			     *)
			     		myDomain=$myAddress
			     	  ;;
			esac
          ;;
     139.162.106.228|172.104.60.30|172.104.42.165)      
          forgerSecretPhrase=''
          myHallmark=''
			case $myAddress in
			     172.104.39.118|172.104.42.165|172.104.51.191|172.104.51.252|172.104.59.231|139.162.28.101)
			     		myDomain="192.168.133.36"
						#myDomain=$myAddress
						#myDomain='jp.apoacc.com'
			          ;;
			     *)
			     		myDomain=$myAddress
			     	  ;;
			esac
          ;;
     139.162.180.60|172.104.146.203|172.104.51.191)
          forgerSecretPhrase=''
          myHallmark=''
			case $myAddress in
			     172.104.39.118|172.104.42.165|172.104.51.191|172.104.51.252|172.104.59.231|139.162.28.101)
						myDomain="192.168.154.229"
						#myDomain='de.apoacc.com'
			          ;;
			     *)
			     		myDomain=$myAddress
			     	  ;;
			esac
          ;; 
     178.79.132.8|178.79.130.64|172.104.51.252)
          forgerSecretPhrase=''
          myHallmark=''
          maxPrunableLifetime=-1
			case $myAddress in
			     172.104.39.118|172.104.42.165|172.104.51.191|172.104.51.252|172.104.59.231|139.162.28.101)
						myDomain="192.168.144.225"
						#myDomain='uk.apoacc.com'
			          ;;
			     *)
			     		myDomain=$myAddress
			     	  ;;
			esac
          ;;
     45.79.66.180|45.56.116.43|172.104.59.231)
          forgerSecretPhrase=''
          myHallmark=''
			case $myAddress in
			     172.104.39.118|172.104.42.165|172.104.51.191|172.104.51.252|172.104.59.231|139.162.28.101)
						myDomain="192.168.146.185"
						#myDomain='us.apoacc.com'
			          ;;
			     *)
			     		myDomain=$myAddress
			     	  ;;
			esac
          ;;
     139.162.28.101)
          forgerSecretPhrase=''
          myHallmark=''
			case $myAddress in
			     172.104.39.118|172.104.42.165|172.104.51.191|172.104.51.252|172.104.59.231|139.162.28.101)
						myDomain="192.168.152.101"
			          ;;
			     *)
			     		myDomain=$myAddress
			     	  ;;
			esac
          ;;
     *)
          forgerSecretPhrase=''
          myHallmark=''
          myDomain=$myAddress
          ;;
esac

myPlatform='APONode-'$myDomain
adminPassword='tantoVaLagatta-'$myDomain

forgerSecretPhrase=$(sed 's/[\/&]/\\&/g' <<<"$forgerSecretPhrase")
echo 'Found forgerSecretPhrase='$forgerSecretPhrase

if [[ -f $homeDir/$file ]] && [[ -w $homeDir/$file ]]; then
	rm -rf $homeDir/$file_new
	rm -rf $homeDir/apo.properties
	cp $homeDir/$file $homeDir/$file_new
	sed -i -- "s/apo.myAddress=/apo.myAddress=$myDomain/g" "$homeDir/$file_new"
	sed -i -- "s/apo.myPlatform=/apo.myPlatform=$myPlatform/g" "$homeDir/$file_new"
	sed -i -- "s/apo.wellKnownPeers=/apo.wellKnownPeers=$wellKnownPeers/g" "$homeDir/$file_new"
	#sed -i -- "s/apo.defaultPeers=/apo.defaultPeers=$wellKnownPeers/g" "$homeDir/$file_new"
	sed -i -- "s/apo.knownBlacklistedPeers=/apo.knownBlacklistedPeers=$knownBlacklistedPeers/g" "$homeDir/$file_new"
	sed -i -- "s/apo.forgerSecretPhrase=/#apo.forgerSecretPhrase=$forgerSecretPhrase/g" "$homeDir/$file_new"
	#echo $forgerSecretPhrase | sed -i -- "s/apo.forgerSecretPhrase=/apo.forgerSecretPhrase=\1/g" "$homeDir/$file_new"
	sed -i -- "s/apo.reductorFee=/apo.reductorFee=$reductorFee/g" "$homeDir/$file_new"
	sed -i -- "s/apo.adminPassword=/apo.adminPassword=$adminPassword/g" "$homeDir/$file_new"
	#sed -i -- "s/apo.currencyName=/apo.currencyName=$currencyName/g" "$homeDir/$file_new"
	sed -i -- "s/apo.uiServerPort=/apo.uiServerPort=$uiServerPort/g" "$homeDir/$file_new"
	sed -i -- "s/apo.disabledAPITags=/apo.disabledAPITags=$disabledAPITags/g" "$homeDir/$file_new"
	sed -i -- "s/apo.maxPrunableLifetime=/apo.maxPrunableLifetime=$maxPrunableLifetime/g" "$homeDir/$file_new"
	sed -i -- "s/apo.peerServerPort=/apo.peerServerPort=$peerServerPort/g" "$homeDir/$file_new"
	sed -i -- "s/apo.apiServerPort=/apo.apiServerPort=$apiServerPort/g" "$homeDir/$file_new"
	sed -i -- "s/apo.apiServerSSLPort=/apo.apiServerSSLPort=$apiServerSSLPort/g" "$homeDir/$file_new"
	sed -i -- "s/apo.usePeersDb=/apo.usePeersDb=false/g" "$homeDir/$file_new"
	sed -i -- "s/apo.enableApiTestUI=/apo.enableApiTestUI=$enableApiTestUI/g" "$homeDir/$file_new"
	
	
#	case $myAddress in
#	     139.162.47.229|139.162.106.228|139.162.180.60|178.79.132.8|45.79.66.180)
#				sed -i -- "s/apo.myHallmark=/apo.myHallmark=$myHallmark/g" "$homeDir/$file_new"
#	          ;;
#	esac
fi

#case $myAddress in
#     139.162.47.229|139.162.106.228)
			#######################################################
			#               ONLY FIRST TIME                       #
			#echo 'rm -rf /home/apo/apo_db_old'
			#rm -rf /home/apo/apo_db_old
			#echo 'mv /home/apo/apo_db /home/apo/apo_db_old'
			#mv /home/apo/apo_db /home/apo/apo_db_old
			#######################################################
			echo 'rm -rf /home/apo/src/java/apodesktop.old'
			rm -rf /home/apo/src/java/apodesktop.old
			echo 'mv /home/apo/src/java/apodesktop /home/apo/src/java/apodesktop.old'
			mv /home/apo/src/java/apodesktop /home/apo/src/java/apodesktop.old
			echo 'rm -rf /home/apo/jre-old'
			rm -rf /home/apo/jre-old
			echo 'mv /home/apo/jre /home/apo/jre-old'
			mv /home/apo/jre /home/apo/jre-old
          ;;
#     *)
#          echo 'No delete required'
#          ;;
#esac

echo "Recompile APO server"
#/home/apo/compile.sh > /dev/null
/home/apo/compile.sh > /dev/null 2>&1 &
echo "wait 20 secs..."
sleep 20
echo "Finished compiling"
echo "Restart APO server"
#######################################################
#          ONLY FIRST TIME DOUBLE RESTART             #
#/etc/init.d/apo-server restart > /dev/null 2>&1
#######################################################
/etc/init.d/apo-server restart > /dev/null 2>&1 &
#service apo-server restart > /dev/null 2>&1 &
echo "End script"

