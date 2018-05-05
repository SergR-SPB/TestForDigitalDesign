package com.directory.observer;

public class Directory extends ObservableObject {

    private static final long serialVersionUID = -6984206968163938800L;

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    protected boolean isModifyTemplate(ObservableObject o) {
        return getPath().equals(o.getPath())
                && getCreationTime().equals(o.getCreationTime())
                && !getLastModifiedTime().equals(o.getLastModifiedTime());
    }

    @Override
    protected boolean isMoveTemplate(ObservableObject o) {
        return !getPath().equals(o.getPath())
                && getName().equals(o.getName())
                && getCreationTime().equals(o.getCreationTime())
                && getLastModifiedTime().equals(o.getLastModifiedTime());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Directory{" +
                "path='" + getPath() + '\'' +
                ", created=" + getCreationTime() +
                ", lastChanged=" + getLastModifiedTime() +
                '}' + "\n";
    }
}
