# Horizon Nav BR

Cópia pessoal do [Horizon Nav](https://github.com/benbishop028-arch/Horizon-Maps), ajustada para uso no Brasil:

- identificação própria (`com.headunit.nav.br`, "Horizon Nav BR"): instala ao lado do original e só recebe atualizações deste repositório;
- assinatura com chave própria (`horizon-br.keystore`, protegida pela senha no secret `KEYSTORE_PASSWORD`);
- quilômetros e o pacote offline do Brasil como padrão.

O código fica em `HorizonNav.zip`. A cada envio, o GitHub Actions compila e publica o APK em:
`https://github.com/GabrieldJesus012/Horizon-Maps/releases/download/app/HorizonNav-BR.apk`

Uso pessoal, sem redistribuição.

## Passo a passo (pelo celular)

### 1. Ativar a compilação (uma vez só)
1. Abra este repositório no navegador e toque na aba **Actions**.
2. Toque em **I understand my workflows, go ahead and enable them**.
3. Vá em **Settings → Secrets and variables → Actions → New repository secret**.
4. Name: `KEYSTORE_PASSWORD` · Secret: a senha da chave (guardada com você). Toque em **Add secret**.
5. Volte em **Actions → Build Horizon Nav BR → Run workflow → Run workflow**.
6. Espere de 10 a 15 minutos até aparecer o ✓ verde.

### 2. Instalar no celular
1. Baixe o APK: `https://github.com/GabrieldJesus012/Horizon-Maps/releases/download/app/HorizonNav-BR.apk`
2. Abra o arquivo e permita instalar de fontes desconhecidas quando o Android pedir.
3. Abra o **Horizon Nav BR** e siga a configuração inicial: cole o token da Mapbox (pk.…) e, se quiser, a chave da TomTom.
4. Em **Configurações → Mapas offline**, baixe o pacote do Brasil no Wi-Fi.

### 3. Fazer aparecer no Android Auto
1. Abra as configurações do Android Auto e toque umas 10 vezes no número da versão (lá embaixo).
2. No menu ⋮ do canto superior, abra **Configurações do desenvolvedor** e ative **Fontes desconhecidas**.
3. Em **Personalizar acesso rápido**, marque o Horizon Nav BR.
4. Force a parada do Android Auto, desconecte e conecte o celular no carro de novo.

### Atualizações
Cada mudança enviada para este repositório gera um APK novo no mesmo link. O próprio app avisa quando há versão nova.

## Tradução

A interface, as mensagens e a voz de navegação estão em português. A pasta `traducao/` guarda as ferramentas e os mapas de
tradução (`inglês ⇒ português`), para reaplicar tudo quando o autor publicar uma versão nova: `traducao/aplicar.sh <pasta do projeto original>`.
