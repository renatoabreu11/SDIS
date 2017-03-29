package network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface IClientPeer extends Remote {

    /**
     * @param fileData the file in bytes.
     * @param pathname the system path for the file to backup.
     * @param replicationDegree number of servers to distribute the chunks.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     */
    void BackupFile(byte[] fileData, String pathname, int replicationDegree) throws IOException, NoSuchAlgorithmException, InterruptedException;

    /**
     * @param pathname
     */
    void RestoreFile(String pathname) throws IOException, InterruptedException;

    /**
     * @param pathname the system path for the file to backup.
     * @throws RemoteException
     */
    void DeleteFile(String pathname) throws IOException, NoSuchAlgorithmException;

    void ManageDiskSpace(long maxDiskSpace) throws IOException;

    void RetrieveInformation() throws IOException;
}
