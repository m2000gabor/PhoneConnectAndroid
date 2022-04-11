package hu.elte.sbzbxr.phoneconnect.model.persistance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class FileInFolderDescriptor {
    @NonNull private final String filename;
    @NonNull private final String folderName;

    public FileInFolderDescriptor(@Nullable String filename, @Nullable String folderName) {
        if(filename==null) filename="";
        if(folderName==null) folderName="";
        this.filename = filename;
        this.folderName = folderName;
    }

    @NonNull
    public String getFilename() {
        return filename;
    }

    @NonNull
    public String getFolderName() {
        return folderName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInFolderDescriptor that = (FileInFolderDescriptor) o;
        return filename.equals(that.filename) && folderName.equals(that.folderName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, folderName);
    }

    public boolean hasFolder(){return !folderName.equals("");}

    @Override
    public String toString() {
        return "FileInFolderDescriptor{" +
                "filename='" + filename + '\'' +
                ", folderName='" + folderName + '\'' +
                '}';
    }
}
