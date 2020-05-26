# museu
Projeto no ambito da cadeira SID efetuado por alunos do ISCTE-IUL


**ATENÇÃO:**
  - Recursos partilhados com listas sem tamanho predefinido


TO DO:
  1) Thread Temperatura: algoritmo de detecao medicoes válidas (no relatorio), detetar erro "Sensor temperatura em baixo" (tmp:""), detetar "Sensor temperatura com problemas"(tmp:"NA"). E colocar os respetivos alertas no recurso.
  2) Mesmo para Humidade
  3) Mesmo para Luminosidade
  4) Mesmo para Movimento
  5) Recurso "ShareResourceRegisto": alertar "SendToMysql" sempre que surgir um alerta
  6) "SendToMysql": sleep tempo de migracao, ou acordar caso seja notificado. Quando envia dados, enviar tanto alertas como medicoes e retirá-las do recurso (apenas o numero de medicoes enviadas será retirado da lista - para nao serem apagadas medições por lapso que acabaram de chegar mas que não foram enviadas)
  7) "SendToMysql": inserir dados no mysql atraves do login no utilizador "MigradorMongoMysql" (definido no relatorio)
  8) "SendToMysql": conectar-se ao mysql, em caso de insucesso iniciar algoritmo de reconeccao. Simultaneamente este irá conectar-se ao Mongo para comparar os alertas de *movimento* e *luminosidade* com as rondas planeadas (coleção ronda_planeada) (caso alerta, enviar por email: "Luz Detetada (Sem acesso a rondas extra)"). Caso se verifique um dos outros alertas, envia diretamente por email (explicado funcionamento no relatorio) e remove estes alertas da lista. As medições ficam todas em espera na lista até o mysql ficar ativo.
  9) "SendToMysql": em caso de algoritmo de reconecção não resultar, enviar email a alertar os responsáveis (explicado no relatorio)
  10) "AtualizadorIni": conseguir alertar esta classe atraves do mysql quando surgem alteracoes na tabela sistema. Atualizar os dados da tabela sistema no .ini "CloudToMongo" e "MongoToMysql"
  11) "AtualizadorRonda": sempre que ha um alateração nos dados da tabela rondaplaneada, alertar esta classe de forma a ser procedida à alteração/inserção das rondas planeadas na base de dados Mongo, coleção "ronda_planeada"
  12) Threads Temperatura e Humidade: Esperar um tempo de recuperação após envio de um alerta, de forma a garantir que este processo é incremental, e não irão ser enviados alertas do mesmo tipo repetidos (continuamente). Isto apenas é aplicável a alertas do mesmo tipo: p.e. temperatura: pode ser enviado um "Temperatura a subir rapidamente" seguido de "Incendio", mas não dois "Temperatura a subir rapidamente" ou dois "Incendio". (relatório secção 2.7)


EXTRAS:
  1) 

  
  COMPLETO:
  
  - "CloudToMongo": Insercao Mongo (troca timestamp para java, troca por "na", troca por "", colocacao na colecao, adição dos campos caso não exitam na mensagem enviada pelo sensor, de forma a não dar erro)
