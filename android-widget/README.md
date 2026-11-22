# Widget Android — Meu Dindin

Projeto complementar para exibir saldo, receitas e despesas na tela inicial do Android.

## Integração

O app web mantém os lançamentos na chave `meudindin_data` do `localStorage`. O aplicativo Android complementar deverá abrir o site em uma WebView, ler essa chave após o carregamento e salvar um resumo local no dispositivo. O widget consumirá esse resumo local, sem enviar dados financeiros para um servidor.

## Estado

Estrutura inicial. A implementação Kotlin, o layout do widget e o fluxo de geração do APK serão adicionados em seguida.
