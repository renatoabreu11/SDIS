package TP;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Evenilink on 15/03/2017.
 */
public interface IVehicles extends Remote {
    String RegisterCar(String plate, String owner) throws RemoteException;
    String GetCar(String plate) throws RemoteException;
}
