#!/usr/bin/env bash

file=_build/system_info.inc


echo -e "\t* Processeur: `lscpu | grep "Model name" | cut -d " " -f 14-`" > ${file}

echo -e "\t* Vitesse d'écriture disque: `dd if=/dev/zero of=tempfile bs=1M count=10000 conv=fdatasync,notrunc status=progress 2>&1 | tail -n 1 | cut -d " " -f 8-`" >> ${file}

echo -e "\t* Vitesse de lecture disque: `dd if=tempfile of=/dev/null bs=1M count=10000 status=progress 2>&1 | tail -n 1 | cut -d " " -f 8-`" >> ${file}

echo -e "\t* Version du Kernel: `uname -r`" >> ${file}

echo -e "\t* Version de java: `java -version 2>&1 | head -n1`" >> ${file}

bytes=`cat experiment/size.log`
echo -e "\t* Taille des fichiers : `expr ${bytes} / 1024 / 1024` MB" >> ${file}

echo -e "\t* Taille des blocks FS : `stat -f . | grep "Block size" | cut -d " " -f 3`" >> ${file}

rm tempfile
