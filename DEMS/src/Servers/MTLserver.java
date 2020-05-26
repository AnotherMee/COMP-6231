package Servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import ServerInterfaces.BookingCapacity;
import ServerInterfaces.MTLimplement;


public class MTLserver 
{
	

	public static void main(String args[]) throws Exception
	{
		Runtime.getRuntime().addShutdownHook(new ShutDownWork());
		MTLimplement mtlInters = new MTLimplement();
		
//		BookingCapacity bookCap1 = new BookingCapacity(3); 
//		BookingCapacity bookCap2 = new BookingCapacity(3); 
//		BookingCapacity bookCap3 = new BookingCapacity(3); 
//		BookingCapacity bookCap4 = new BookingCapacity(3);
//		BookingCapacity bookCap5 = new BookingCapacity(3);
//		
//		mtlInters.addEvent("MTLE110320", "Trade Shows", bookCap5);
//		mtlInters.addEvent("MTLE120320", "Trade Shows", bookCap3); 
//		mtlInters.addEvent("MTLE130320", "Trade Shows", bookCap1); 
//		mtlInters.addEvent("MTLE140320", "Trade Shows", bookCap2); 
//		mtlInters.addEvent("MTLE150320", "Trade Shows", bookCap4);
//		
//		// booking
//		mtlInters.bookEvent("SHEC123","MTLE120320", "Trade Shows");
//		mtlInters.bookEvent("SHEC123","MTLE130320", "Trade Shows");
//		//mtlInters.bookEvent("SHEC123","MTLE140320", "Trade Shows");
		 	
		String getInfo;
		String sendInfo = "send";
		String registryURL = null;
		int port = 5001;
						
		startRegistry(5001);

		
        Naming.rebind("rmi://localhost:" + port + "/MTL",mtlInters);
        System.out.println("MTL_Server is On");
        
        DatagramSocket aSocket = null;
	    try{
				aSocket = new DatagramSocket(6001);
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
		        		sendInfo = mtlInters.getOwnlistEventAvailability(eventType);
		        	}
		        	else if (getInfo.contains("book"))
		        	{
		        		String[] info = getInfo.split(",");
		        		String customerID = info[0];
		        		String eventID = info[1];
		        		String eventType = info[2];
		        		sendInfo = mtlInters.bookEvent(customerID, eventID, eventType);
		        		System.out.println(customerID + eventID + eventType);
		        	}
		        	else if (getInfo.contains("cshashmap"))
		        	{
		        		String[] info = getInfo.split(",");
		        		String customerID = info[0];
		        		sendInfo = mtlInters.getBookingSchedule(customerID);
		        	}
		        	else if (getInfo.contains("cancel"))
		        	{
		        		String[] info = getInfo.split(",");
		        		String customerID = info[0];
		        		String eventID = info[1];
		        		String eventType = info[2];
		        		sendInfo = mtlInters.cancelEvent(customerID, eventID, eventType);
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
