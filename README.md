## dn-ftp-ext
Пакет для работы с FTP.
По вопросам - https://vk.com/dn_extension или https://vk.com/broelik.
## Скачать
Последняя версия - https://github.com/broelik/dn-ftp-ext/releases/latest
## Пример кода
Подключение:
```php
$client = new FTPClient;
$client->connect($host, $port); // По умолчания порт = 21

$client->disconnect($sendQuitCommand); // Отключиться
```
Авторизация:
```php
$client->login($username, $password);
```
Работа с клиентом:
```php
# Директории и файлы
$client->rename($old, $new); // Переименовать файл или директорию
$client->hasFile($fname); // Проверка на существование файла или папки по имени в текущей папке
$client->delete($fname); // Удалить файл или директорию по имени
$client->getTypeByName($fname); // Получить тип по имени [dir, file, false]

# Директории
$client->currentDirectory(); // Получить текущую директорию
$client->parentDirectory(); // Получить родительскую директория
$client->changeDirectory($dir); // Сменить директорию
$client->createDirectory($dir); // Создать директорию
$client->deleteDirectory($dir); // Удалить директорию
$client->list(); // Получить список файлов в текущей директории

# Файлы
$client->deleteFile($fname); // Удалить файл
$client->upload($fname); // Загрузить файл
$client->download($fname, $to); // Скачать файл
$client->downloadL($fname, $to, $listener); //
 
```

Работа с файлами полученными через list():
```php
$files = $client->list();
foreach($files as $file) {
    $file->{...};
}

###

$file->getModifiedDate(); //
$file->setModifiedDate(); //
$file->getName(); //
$file->hasFile(); //
$file->hasFolder(); //
$file->setName(); //
$file->getType(); //
$file->setType(); //
$file->getSize(); //
$file->setSize(); //
$file->getLink(); //
$file->setLink(); //
$file->__toString(); //

```

Пример использования:
https://hub.develnext.org/project/SrtCjqnoXnjk

Работает на:
https://www.sauronsoftware.it/projects/ftp4j/manual.php
