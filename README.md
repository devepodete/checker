# checker
Java-checker for olympiad programming

Main запускает в отдельном процессе ProgramTesting.Checker, считывает команды из терминала и отправляет их при помощи сокетов на сам чекр.

Формат комад: `COMMAND [ARGS]`.
Список доступных команд на текущий момент:
  - `check contest problem submit` - проверить посылку
  - `exit` - выход

