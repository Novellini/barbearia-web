# Barbearia Web

Versão web do sistema AgendaBarber, desenvolvida com Java 21, Spring Boot, Thymeleaf e MySQL. O menu, os formulários e as tabelas se ajustam a telas menores.

A [versão desktop em Java Swing](https://github.com/Novellini/BarbeariaSistema) pode usar o mesmo banco.

## Funcionalidades

- Login, painel com resumo do dia e agenda por data.
- Cadastro e busca de clientes, barbeiros e serviços.
- Agendamento de um ou mais serviços com cálculo da duração, consulta de horários livres e proteção contra sobreposição.
- Registro de pagamentos presenciais e acompanhamento do status dos atendimentos.
- Relatórios com filtro de período, movimento diário, recebimentos e ranking de barbeiros e serviços.

## Como executar

1. Instale o **JDK 21** e o **MySQL 8**.
2. Para criar um banco novo, rode `mysql -u root -p < database/barbearia.sql`. Esse arquivo recria as tabelas. Se você já usa a versão anterior do banco, execute [a atualização de duração](database/atualizar_duracao_agendamentos.sql) uma vez, com os dois aplicativos fechados.
3. Copie `config/application.properties.exemplo` para `config/application.properties` e informe a senha do MySQL. Você também pode definir `DB_PASSWORD`. A conexão padrão usa `jdbc:mysql://localhost:3306/barbearia` e o usuário `root`.
4. Na raiz deste repositório, execute `./mvnw spring-boot:run` no Linux/macOS ou `mvnw.cmd spring-boot:run` no Windows. No IntelliJ, importe o `pom.xml`, selecione o JDK 21 e execute `BarbeariaWebApplication` com a raiz do projeto como diretório de trabalho.
5. Abra [localhost:8080](http://localhost:8080). O login de desenvolvimento é `admin` / `admin`. Defina `APP_USUARIO` e `APP_SENHA` para usar outras credenciais.

Para testar a agenda, cadastre um cliente, um barbeiro ativo e um serviço ativo. O Spring usa o esquema já criado no MySQL. O teste de inicialização do Maven também precisa de um banco configurado.

## Organização

- `model` mapeia as tabelas do banco com JPA.
- `repository` concentra o acesso aos dados. O relatório usa consultas SQL para calcular os totais e rankings.
- `service` reúne as regras da agenda e dos pagamentos.
- `controller` recebe as requisições e envia os dados aos templates.
- `src/main/resources/templates` contém as páginas Thymeleaf. O CSS e o JavaScript ficam em `static`.

## Autor

[Gustavo Novellini](https://www.linkedin.com/in/gustavo-novellini-665a74264)
