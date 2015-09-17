#!/bin/bash

if [ $# -eq 3 ] ; then
    INSTALL_PATH=$1
    JAVA_PATH=$2
    WORDNET_PATH=$3
    E1="s|[+]CHANGE_WITH_PATH_TO_TER[+]|${INSTALL_PATH}|g";
    E2="s|[+]CHANGE_WITH_PATH_TO_JAVA[+]|${JAVA_PATH}|g";
    E3="s|[+]CHANGE_WITH_PATH_TO_WORDNET[+]|${WORDNET_PATH}|g";
    for f in "bin/terp" "bin/terpa" "bin/create_phrasedb" "bin/optimize_terp" "bin/tercom" "bin/terp_ter" "data/data_loc.param"
    do
        echo Creating script $f from $f.templ
	sed -e  ${E1} -e ${E2} -e ${E3} ${INSTALL_PATH}/${f}.templ > ${INSTALL_PATH}/${f}
    	chmod 755 ${INSTALL_PATH}/${f}
    done

    for f in "data/data_loc.param"
    do
        echo Creating parameter file $f from $f.templ
	sed -e  ${E1} -e ${E2} -e ${E3} ${INSTALL_PATH}/${f}.templ > ${INSTALL_PATH}/${f}
    done
else
    echo "usage: setup_bin.sh PATH_TO_TERP PATH_TO_JAVA PATH_TO_WORDNET";
    echo "  PATH_TO_TERP is the path to the TERp installation root directory";
    echo "  PATH_TO_JAVA is the path to the java installation (so that PATH_TO_JAVA/bin/java exists)";
    echo "  PATH_TO_WORDNET is the path to the WordNet 3.0 installation (so that PATH_TO_WORDNET/dict/ exists)";
    exit 1;    
fi

