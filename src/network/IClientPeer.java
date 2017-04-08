package network;

import java.io.IOException;
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
    String BackupFile(byte[] fileData, String pathname, int replicationDegree) throws IOException, NoSuchAlgorithmException, InterruptedException;

    /**
     * @param pathname
     */
    byte[] RestoreFile(String pathname) throws IOException, InterruptedException;

    /**
     * @param pathname the system path for the file to backup.
     * @throws RemoteException
     */
    void DeleteFile(String pathname, int type) throws IOException, NoSuchAlgorithmException, InterruptedException;

    /**
     *
     * @param maxDiskSpace
     * @throws IOException
     */
    String ManageDiskSpace(long maxDiskSpace) throws IOException, InterruptedException;

    /**
     *
     * @throws IOException
     */
    String RetrieveInformation() throws IOException, InterruptedException;
}
