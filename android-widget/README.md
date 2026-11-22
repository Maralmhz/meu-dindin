# Meu Dindin — Widget Android

Aplicativo complementar para exibir saldo, receitas e despesas na tela inicial do Android.

## Como configurar pela primeira vez

1. Abra o Meu Dindin no navegador onde seus lançamentos já estão salvos: https://meu-dindin.pages.dev
2. Toque no botão de download no cabeçalho para exportar o arquivo `meu-dindin-backup.json`.
3. Instale e abra o aplicativo complementar Meu Dindin Widget.
4. Toque em **IMPORTAR BACKUP DO NAVEGADOR** e selecione o arquivo exportado.
5. Volte à tela inicial do Android, mantenha o dedo pressionado em um espaço vazio, abra **Widgets**, procure **Meu Dindin Widget** e arraste-o para a tela.

## Atualização e privacidade

- O aplicativo complementar abre o site em uma WebView e lê os lançamentos salvos nela.
- O backup exportado do navegador é importado localmente no aplicativo complementar.
- O resumo financeiro do widget fica salvo no armazenamento privado deste aparelho.
- Nenhum dado financeiro é enviado para um servidor por este aplicativo complementar.
- Enquanto o aplicativo complementar estiver aberto, ele verifica alterações a cada 10 segundos. O Android também agenda atualizações periódicas do widget.

## Limitação importante

O armazenamento do Chrome e o armazenamento interno da WebView são separados pelo Android. Por isso, a primeira transferência dos lançamentos exige exportar o backup no navegador e importá-lo no aplicativo complementar. Alterações feitas posteriormente no navegador não são refletidas automaticamente no widget: exporte e importe um novo backup para atualizar a cópia local.

## Build

O APK de depuração é gerado pelo workflow GitHub Actions `.github/workflows/build-android-widget.yml`, usando Java 17 e Gradle 8.9.
