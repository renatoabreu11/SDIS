package fileSystem;

public class FileAssociation {

    private String pathname;
    private String fileId;
    private int totalChunks;

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(int totalChunks) {
        this.totalChunks = totalChunks;
    }

    public FileAssociation(String pathname, String fileId, int totalChunks) {
        this.pathname = pathname;
        this.fileId = fileId;
        this.totalChunks = totalChunks;
    }
}
