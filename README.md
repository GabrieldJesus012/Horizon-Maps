# Horizon Nav BR

Cópia pessoal do [Horizon Nav](https://github.com/benbishop028-arch/Horizon-Maps), ajustada para uso no Brasil:

- identificação própria (`com.headunit.nav.br`, "Horizon Nav BR"): instala ao lado do original e só recebe atualizações deste repositório;
- assinatura com chave própria (`horizon-br.keystore`, protegida pela senha no secret `KEYSTORE_PASSWORD`);
- quilômetros e o pacote offline do Brasil como padrão.

O código fica em `HorizonNav.zip`. A cada envio, o GitHub Actions compila e publica o APK em:
`https://github.com/GabrieldJesus012/Horizon-Maps/releases/download/app/HorizonNav-BR.apk`

Uso pessoal, sem redistribuição.

## Tradução

A interface, as mensagens e a voz de navegação estão em português. A pasta `traducao/` guarda as ferramentas e os mapas de
tradução (`inglês ⇒ português`), para reaplicar tudo quando o autor publicar uma versão nova: `traducao/aplicar.sh <pasta do projeto original>`.
