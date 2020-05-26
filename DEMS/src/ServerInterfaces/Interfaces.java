package ServerInterfaces;

import java.rmi.*;




public interface Interfaces extends Remote
{
	String addEvent(String eventIDs, String eventType, BookingCapacity bookCap) throws RemoteException;
	String removeEvent(String eventID, String eventType) throws RemoteException;
	String listEventAvailability(String eventType)throws RemoteException;
	String getOwnlistEventAvailability(String eventType)throws RemoteException;
	String bookEvent (String customerID, String eventID, String eventType)throws RemoteException;
	String getBookingSchedule (String customerID)throws RemoteException;
	String cancelEvent(String customerID, String eventID, String eventType)throws RemoteException;
}
