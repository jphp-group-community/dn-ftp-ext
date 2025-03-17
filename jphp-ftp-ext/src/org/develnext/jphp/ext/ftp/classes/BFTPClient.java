package org.develnext.jphp.ext.ftp.classes;

import php.runtime.env.Environment;
import php.runtime.invoke.Invoker;
import php.runtime.lang.BaseObject;
import php.runtime.memory.ArrayMemory;
import php.runtime.memory.LongMemory;
import php.runtime.memory.StringMemory;
import php.runtime.memory.support.MemoryStringUtils;
import php.runtime.reflection.ClassEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.develnext.jphp.ext.ftp.FTPExtension;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPCommunicationListener;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;
import php.runtime.Memory;
import php.runtime.annotation.Reflection.*;

@Namespace(FTPExtension.NS)
@Name("FTPClient")
public class BFTPClient extends BaseObject {

    protected FTPClient client;
    protected FTPCommunicationListener receiveListener;
    protected FTPCommunicationListener sentListener;

    //
    public BFTPClient(Environment env, ClassEntity clazz) {
        super(env, clazz);
    }

    //
    @Signature
    public void __construct() {
        client = new FTPClient();
    }

    //
    @Signature
    public ArrayMemory connect(Environment env, String ip, int port) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        ArrayMemory res = new ArrayMemory();
        String[] connectResult = client.connect(ip, port);
        for (String str : connectResult)
            res.add(str);
        return res;
    }

    //
    @Signature
    public ArrayMemory connect(Environment env, String ip) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        return this.connect(env, ip, client.getSecurity() == FTPClient.SECURITY_FTPS ? 990 : 21);
    }

    //
    @Signature
    public void login(String username, String password) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        client.login(username, password);
    }

    //
    @Signature
    public void login(String username, String password, String acc) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        client.login(username, password, acc);
    }

    //
    @Signature
    public void disconnect(boolean quit) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        client.disconnect(quit);
    }

    //
    @Signature
    public void disconnect() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        client.disconnect(true);
    }

    // Получить текущую директорию
    @Signature
    public Memory currentDirectory() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        return StringMemory.valueOf(client.currentDirectory());
    }

    //  Сменить директорию
    @Signature
    public void changeDirectory(String dir) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        client.changeDirectory(dir);
    }

    // Получить родительскую директория
    @Signature
    public void parentDirectory() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        client.changeDirectoryUp();
    }

    // Переименовать файл или директорию
    @Signature
    public void rename(String oldName, String newName) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        client.rename(oldName, newName);
    }

    // Проверка на существование файла или папки по имени в текущей папке
    @Signature
    public Memory hasFile(String fileName) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        // Получаем список файлов и папок в текущей директории
        FTPFile[] files = client.list();

        // Проходим по списку и проверяем, есть ли объект с указанным именем
        for (FTPFile file : files) {
            if (file.getName().equals(fileName)) {
                return Memory.TRUE; // Объект найден (файл или папка)
            }
        }

        // Если объект не найден, возвращаем FALSE
        return Memory.FALSE;
    }

    // Удалить файл
    @Signature
    public void deleteFile(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        client.deleteFile(path);
    }

    // Общая функция удаления файлов и папок по имени
    @Signature
    public void delete(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        // Получаем информацию о файле или директории
        FTPFile[] files = client.list(path);

        if (files.length == 1) {
            FTPFile file = files[0];

            if (file.getType() == FTPFile.TYPE_DIRECTORY) {
                // Если это директория, удаляем её
                client.deleteDirectory(path);
            } else if (file.getType() == FTPFile.TYPE_FILE) {
                // Если это файл, удаляем его
                client.deleteFile(path);
            }

        }
    }

    //
    @Signature
    public Memory fileSize(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        return LongMemory.valueOf(client.fileSize(path));
    }


    // Создать директорию
    public void createDirectory(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        client.createDirectory(path);
    }

    // Удалить директорию
    @Signature
    public void deleteDirectory(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        client.deleteDirectory(path);
    }


    //
    @Signature
    public ArrayMemory list(Environment env, String fileSpec) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        ArrayMemory res = new ArrayMemory();
        for (FTPFile file : client.list(fileSpec))
            res.add(new FTPFileWrapper(env, file));
        return res;
    }

    //
    @Signature
    public ArrayMemory list(Environment env) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        return list(env, null);
    }

    //
    @Signature
    public void downloadL(String file, File to, Invoker invoker) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {

        FTPDataTransferListener listener = new FTPDataTransferListener() {
            @Override
            public void aborted() {
                try {
                    invoker.call(StringMemory.valueOf("aborted"));
                } catch (Throwable e) {
                }
            }

            @Override
            public void completed() {
                try {
                    invoker.call(StringMemory.valueOf("completed"));
                } catch (Throwable e) {
                }
            }

            @Override
            public void failed() {
                try {
                    invoker.call(StringMemory.valueOf("failed"));
                } catch (Throwable e) {
                }
            }

            @Override
            public void started() {
                try {
                    invoker.call(StringMemory.valueOf("started"));
                } catch (Throwable e) {
                }
            }

            @Override
            public void transferred(int bytes) {
                try {
                    invoker.call(StringMemory.valueOf("transferred"), LongMemory.valueOf(bytes));
                } catch (Throwable e) {
                }
            }
        };
        client.download(file, to, listener);
    }

    //
    @Signature
    public void download(String file, File to) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        client.download(file, to);
    }

    //
    @Signature
    public void upload(File file, Invoker invoker) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        FTPDataTransferListener listener = new FTPDataTransferListener() {
            @Override
            public void aborted() {
                try {
                    invoker.call(StringMemory.valueOf("aborted"));
                } catch (Throwable e) {
                }
            }

            @Override
            public void completed() {
                try {
                    invoker.call(StringMemory.valueOf("completed"));
                } catch (Throwable e) {
                }
            }

            @Override
            public void failed() {
                try {
                    invoker.call(StringMemory.valueOf("failed"));
                } catch (Throwable e) {
                }
            }

            @Override
            public void started() {
                try {
                    invoker.call(StringMemory.valueOf("started"));
                } catch (Throwable e) {
                }
            }

            @Override
            public void transferred(int bytes) {
                try {
                    invoker.call(StringMemory.valueOf("transferred"), LongMemory.valueOf(bytes));
                } catch (Throwable e) {
                }
            }
        };
        client.upload(file, listener);
    }

    //
    @Signature
    public void upload(File file) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        client.upload(file);
    }

    //
    @Signature
    public void abortCurrentDataTransfer(boolean sendAborCommand) throws IOException, FTPIllegalReplyException {
        client.abortCurrentDataTransfer(sendAborCommand);
    }

    //
    @Signature
    public void setPassive(boolean passive) {
        client.setPassive(passive);
    }

    //
    @Signature
    public Memory isConnected() {
        return client.isConnected() ? Memory.TRUE : Memory.FALSE;
    }

    //
    @Signature
    public Memory isAuthenticated() {
        return client.isAuthenticated() ? Memory.TRUE : Memory.FALSE;
    }

    //
    @Signature
    public Memory isPassive() {
        return client.isPassive() ? Memory.TRUE : Memory.FALSE;
    }

    //
    @Signature
    public Memory getHost() {
        return StringMemory.valueOf(client.getHost());
    }

    //
    @Signature
    public Memory getPort() {
        return LongMemory.valueOf(client.getPort());
    }

    //
    @Signature
    public Memory getUsername() {
        return StringMemory.valueOf(client.getUsername());
    }

    //
    @Signature
    public Memory getPassword() {
        return StringMemory.valueOf(client.getPassword());
    }

    //
    @Signature
    public void setReceiveListener(Invoker callback) {
        if (receiveListener != null)
            client.removeCommunicationListener(receiveListener);
        if (callback == null)
            return;
        receiveListener = new FTPCommunicationListener() {
            @Override
            public void received(String arg0) {
                try {
                    callback.call(StringMemory.valueOf(arg0));
                } catch (Throwable e) {
                }
            }

            @Override
            public void sent(String arg0) {

            }
        };
        client.addCommunicationListener(receiveListener);
    }

    //
    @Signature
    public void setSentListener(Invoker callback) {
        if (sentListener != null)
            client.removeCommunicationListener(sentListener);
        if (callback == null)
            return;
        sentListener = new FTPCommunicationListener() {
            @Override
            public void received(String arg0) {
            }

            @Override
            public void sent(String arg0) {
                try {
                    callback.call(StringMemory.valueOf(arg0));
                } catch (Throwable e) {
                }
            }
        };
        client.addCommunicationListener(sentListener);
    }

    //
    @Signature
    public Memory getFileUrl(String fileName) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        // Получаем текущую директорию
        String currentDir = client.currentDirectory();

        // Формируем путь к файлу
        String filePath = currentDir.endsWith("/") ? currentDir + fileName : currentDir + "/" + fileName;

        // Получаем хост и порт
        String host = client.getHost();
        int port = client.getPort();

        // Формируем URL
        String url = "ftp://" + host + ":" + port + filePath;

        // Возвращаем URL как строку
        return StringMemory.valueOf(url);
    }

    //
    @Signature
    public Memory getFileExt(String fileName) {
        // Находим индекс последней точки в имени файла
        int lastDotIndex = fileName.lastIndexOf('.');

        // Если точка найдена и она не является последним символом в строке
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            // Возвращаем подстроку после последней точки
            return StringMemory.valueOf(fileName.substring(lastDotIndex + 1));
        }

        // Если расширение не найдено, возвращаем пустую строку
        return StringMemory.valueOf("");
    }

    //
    @Signature
    public String getTypeByName(String name) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        // Получаем список файлов и директорий в текущей директории
        FTPFile[] files = client.list();

        for (FTPFile file : files) {
            if (file.getName().equals(name)) {
                // Если имя совпадает, возвращаем тип элемента в виде строки
                if (file.getType() == FTPFile.TYPE_DIRECTORY) {
                    return "dir";
                } else if (file.getType() == FTPFile.TYPE_FILE) {
                    return "file";
                }
            }
        }

        return "false";
    }
}