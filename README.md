************************************************************************************
*
* Projeto de Trabalho de Mestrado em Ciência da Computação - FACOM
* Aluno: Jean Alexandre Dobre
* Orientador: Prof. Dr. Said Sadique Adi
*
* Trabalho: O Problema da Seleção de Primers Específicos: algoritmos e aplicações
*
************************************************************************************

# web-select-primer

Aplicação web desenvolvida em Play/Java para ser interface gráfica e permitir o melhor uso dos aplicativos k-difference-primer.

Este projeto foi desenvolvido como requisito para entrega do projeto final do Mestrado em Ciência da Computação e seu funcionamento completo foi descrito em detalhes no Artigo http://www.seer.unirio.br/index.php/isys/article/view/6589/7349 e na Dissertação https://posgraduacao.ufms.br/portal/trabalho-arquivos/download/4679.

Utiliza internamente o banco de dados PostgreSQL como repositório de dados.

Para rodar o software é necessário:
 - Ter o Java Instalado, Banco de dados PostgreSQL, C++ e o Playframework versão 1.4.4 https://downloads.typesafe.com/play/1.4.4/play-1.4.4.zip.
 - Baixar, configurar e rodar o projeto K-difference-primer: https://github.com/jeandobre/k-difference-prime
 - Rodar o arquivo web.database para no PostgreSQL para criar o banco de dados.
 - O projeto está configurado para abrir no IntelliJ. Basta abrir o arquivo s.ipr.
 - No terminal digitar: play run --dev
 - Acessar no brower: http://localhost:9000
 
 Pronto!
 
