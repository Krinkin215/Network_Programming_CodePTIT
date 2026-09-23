package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface ObjectService extends Remote {
    Serializable requestObject(String studentCode, String qCode) throws RemoteException;
    void submitObject(String studentCode, String qCode, Serializable object) throws RemoteException;
}