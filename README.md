# Controle de Produtividade - WEG

Este é um aplicativo Android desenvolvido para o gerenciamento e registro da produção diária em linhas de montagem, com foco inicial no departamento de **Bobinagem III B**. O sistema permite que operadores registrem suas atividades de forma rápida, segura e organizada.

## 🚀 Funcionalidades

- **Sistema de Login e Cadastro**: Separação entre acesso (via RE) e cadastro de novos usuários com definição de departamento.
- **Registro de Produção**: Lançamento de quantidades produzidas em seções específicas:
  - Ligação
  - Solda
  - Bandagem
  - Amarração
  - Prensa
  - Acabamento
- **Motores na Linha**: Funcionalidade para informar a quantidade de peças restantes em cada estágio da produção.
- **Histórico Local**: Visualização imediata de todos os registros feitos no dispositivo.
- **Sincronização com a Nuvem**: Opção de envio manual dos dados para um servidor centralizado.
- **Suporte a Modo Escuro**: Interface adaptativa para maior conforto visual.
- **Segurança de Dados**: Isolamento de registros por usuário (cada operador visualiza apenas sua própria produção).

## 💾 Armazenamento de Dados

O aplicativo utiliza uma arquitetura de armazenamento **híbrida** para garantir que o trabalho não seja perdido, mesmo sem conexão com a internet:

### 1. Armazenamento Local (SQLite via Room)
Todos os dados inseridos (Usuários, Produção e Peças Restantes) são salvos imediatamente em um banco de dados interno no celular. Isso permite:
- Funcionamento 100% offline.
- Consulta rápida de histórico sem consumir dados móveis.

### 2. Armazenamento na Nuvem (Firebase Firestore)
O envio para a nuvem ocorre em momentos específicos:
- **Usuários**: Cadastrados na coleção `usuarios_producao` para garantir que o RE seja único globalmente.
- **Produção**: Os dados são enviados apenas quando o usuário clica em **"Enviar Dados"** na tela de listagem.
- **Peças Restantes**: Enviadas no momento do lançamento, caso haja conexão.

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Java
- **Banco de Dados Local**: Room Persistence Library (SQLite)
- **Banco de Dados Cloud**: Firebase Firestore
- **Interface**: Material Design 3
- **Injeção/Gestão de Versões**: Gradle Version Catalog (TOML)

---
*Desenvolvido para otimização de processos e controle de indicadores de produtividade.*
