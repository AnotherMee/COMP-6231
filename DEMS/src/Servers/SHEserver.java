package Servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ServerInterfaces.BookingCapacity;
import ServerInterfaces.SHEimplement;

public class SHEserver 
{
	public static void main(String args[]) throws Exception
	{
		Runtime.getRuntime().addShutdownHook(new ShutDownWork());
		
		
		SHEimplement she_inters = new SHEimplement();
//		BookingCapacity bookCap1 = new BookingCapacity(1); 
//		BookingCapacity bookCap2 = new BookingCapacity(3); 
//		BookingCapacity bookCap3 = new BookingCapacity(3); 
////		BookingCapacity bookCap4 = new BookingCapacity(3);
////		BookingCapacity bookCap5 = new BookingCapacity(3);
////		BookingCapacity bookCap6 = new BookingCapacity(3);
////		
////		she_inters.addEvent("SHEA060220", "Trade Shows", bookCap6);
////		
////		she_inters.addEvent("SHEE160320", "Trade Shows", bookCap5);
//		she_inters.addEvent("SHEE120320", "Trade Shows", bookCap3); 
//		she_inters.addEvent("SHEE130320", "Trade Shows", bookCap1); 
//		she_inters.addEvent("SHEE140320", "Trade Shows", bookCap2); 
////		she_inters.addEvent("SHEE150320", "Trade Shows", bookCap4);
////		
////		
////		// booking
//		she_inters.bookEvent("SHEC123","SHEE120320", "Trade Shows");
////		she_inters.bookEvent("SHEC122","SHEE120320", "Trade Shows");
////		she_inters.bookEvent("SHEC123","MTLE140320", "Trade Shows");
		
		
		
		String getInfo;
		String sendInfo = null;
		String registryURL;
		int port = 5002;
		startRegistry(5002);

		Naming.rebind("rmi://localhost:" + port + "/SHE", she_inters);
		System.out.println("SHE_Server is On");
		         
        DatagramSocket aSocket = null;
	    try{
				aSocket = new DatagramSocket(6002);
	        	byte[] buffer = new byte[1000];
	        	byte [] m;
	        	while(true)
		        {
	        		DatagramPacket request = new DatagramPacket(buffer, buffer.length);
		        	aSocket.receive(request);
		        	getInfo = new String(request.getData()).trim();
		        	if (getInfo.contains("list"))
		        	{
		        		String[] info = getInfo.split(",");
		        		String eventType = info[0];
		        		sendInfo = she_inters.getOwnlistEventAvailability(eventType);	
		        	}
		        	else if (getInfo.contains("book"))
		        	{
		        		String[] info = getInfo.split(",");
		        		String customerID = info[0];
		        		String eventID = info[1];
		        		String eventType = info[2];
		        		sendInfo = she_inters.bookEvent(customerID, eventID, eventType);
		        	}
		        	else if (getInfo.contains("cshashmap"))
		        	{
		        		String[] info = getInfo.split(",");
		        		String customerID = info[0];
		        		sendInfo = she_inters.getBookingSchedule(customerID);
		        	}
		        	else if (getInfo.contains("cancel"))
		        	{
		        		String[] info = getInfo.split(",");
		        		String customerID = info[0];
		        		String eventID = info[1];
		        		String eventType = info[2];
		        		sendInfo = she_inters.cancelEvent(customerID, eventID, eventType);
		        	}
		        	m = sendInfo.getBytes();
		        	DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());
		        	aSocket.send(reply);
		        }
	        }
	        catch(SocketException e){
	        	System.out.println("Socket: " + e.getMessage());
	        }
	        catch(IOException e) {
	        	System.out.println("IO: " + e.getMessage());
	        }
		
		}

	private static void startRegistry(int port) throws RemoteException 
	{
		try 
		{
			Registry registry = LocateRegistry.getRegistry(port);
			registry.list(); 
		}
		catch (RemoteException e) 
		{
			System.out.println("RMI registery cannot be located at port:" + port);
			Registry registry = LocateRegistry.createRegistry(port);
			System.out.println("RMI registery created at port:" + port);
		}
		
	}
        

}
