package com.directory.observer;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class File extends ObservableObject {
    private static final long serialVersionUID = -3476403620517224862L;

    private String md5Hash;                 //хеш-сумма для файла

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    //вычисление хеш-суммы
    public static String computeMd5Hash(Path path) throws IOException {
        String result = null;
        try {
            byte[] b = Files.readAllBytes(path);
            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
            result = DatatypeConverter.printHexBinary(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected boolean isModifyTemplate(ObservableObject o) {
        return getPath().equals(o.getPath())
                && getCreationTime().equals(o.getCreationTime())
                &&
                (!getLastModifiedTime().equals(o.getLastModifiedTime())
                        || !md5Hash.equals(((File) o).getMd5Hash())
                );
    }

    @Override
    protected boolean isMoveTemplate(ObservableObject o) {
        return !getPath().equals(o.getPath())
                && getName().equals(o.getName())
                && getCreationTime().equals(o.getCreationTime())
                && getLastModifiedTime().equals(o.getLastModifiedTime())
                && md5Hash.equals(((File) o).getMd5Hash());
    }

    @Override
    public boolean isFile() {
        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        File file = (File) o;
        return Objects.equals(md5Hash, file.md5Hash);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), md5Hash);
    }

    @Override
    public String toString() {
        return "File{" +
                "path='" + getPath() + '\'' +
                ", created=" + getCreationTime() +
                ", lastChanged=" + getLastModifiedTime() +
                ", md5Hash='" + md5Hash + '\'' +
                '}' + "\n";
    }
}
