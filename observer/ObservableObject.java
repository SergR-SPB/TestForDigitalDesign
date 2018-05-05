package com.directory.observer;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class ObservableObject implements Serializable {

    private static final long serialVersionUID = 3017072285210029909L;

    private String path;                        //путь к файлу или каталогу

    private LocalDateTime creationTime;         //дата создания

    private LocalDateTime lastModifiedTime;     //дата последнего изменения

    //====================================
    //          геттеры/сеттеры
    //====================================
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(LocalDateTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    //получить имя файла или каталога
    public String getName() {
        if (path == null) {
            throw new IllegalStateException("path is null");
        }
        Path path = Paths.get(this.path);

        return path.getName(path.getNameCount()-1).toString();
    }

    public boolean isFile() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    //был ли текущий объект изменен по сравнению с объектом о
    public boolean isModify(ObservableObject o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return isModifyTemplate(o);
    }

    //абстрактный метод для определения был ли объект изменен
    protected abstract boolean isModifyTemplate(ObservableObject o);

    //был ли текущий объект перемещен по сравнению с объектом о
    public boolean isMove(ObservableObject o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return isMoveTemplate(o);
    }

    //абстрактный метод для определения был ли объект перемещен
    protected abstract boolean isMoveTemplate(ObservableObject o);



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObservableObject that = (ObservableObject) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(creationTime, that.creationTime) &&
                Objects.equals(lastModifiedTime, that.lastModifiedTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(path, creationTime, lastModifiedTime);
    }

    @Override
    public String toString() {
        return "ObservableObject{" +
                "path='" + path + '\'' +
                ", creationTime=" + creationTime +
                ", lastModifiedTime=" + lastModifiedTime +
                '}';
    }
}
