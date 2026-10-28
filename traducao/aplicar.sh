#!/bin/sh
# Reaplica a tradução e os ajustes BR sobre uma versão nova do código do autor.
# Uso (dentro desta pasta): ./aplicar.sh /caminho/para/HorizonNav
#   - a pasta indicada deve ser o projeto ORIGINAL do autor (descompactado do HorizonNav.zip dele)
#   - os textos são trocados só onde batem exatamente; o script avisa (UNUSED / PATCH MISS) o que mudou no original
set -e
cd "$(dirname "$0")"
K="$1/app/src/main/kotlin/com/headunit/launcher"
cp -r novos/app "$1/"
python3 genindex.py
python3 apply.py "$K" mapas/*.txt
python3 patch_code.py "$K" code_extra.py
echo "Pronto. Ajuste também app/build.gradle.kts (id .br, nome, assinatura) como na versão atual."
