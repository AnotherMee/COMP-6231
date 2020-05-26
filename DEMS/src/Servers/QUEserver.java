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
import ServerInterfaces.QUEimplement;


public class QUEserver 
{
	

	public static void main(String args[]) throws Exception
	{
		
		Runtime.getRuntime().addShutdownHook(new ShutDownWork());
		//Test
		
		
		QUEimplement inters = new QUEimplement();
//		BookingCapacity bookCap1 = new BookingCapacity(3);
//		BookingCapacity bookCap2 = new BookingCapacity(3);
////		BookingCapacity bookCap3 = new BookingCapacity(3);
////		BookingCapacity bookCap4 = new BookingCapacity(3);
////		BookingCapacity bookCap5 = new BookingCapacity(3);
////		BookingCapacity bookCap6 = new BookingCapacity(3);
////		BookingCapacity bookCap7 = new BookingCapacity(3);
////		
//		inters.addEvent("QUEE140320", "Trade Shows", bookCap1);
//		inters.addEvent("QUEM180230", "Trade Shows", bookCap2);
////		inters.addEvent("QUEM190230", "Trade Shows", bookCap2);
////		inters.addEvent("QUEM100230", "Trade Shows", bookCap3);
////		inters.addEvent("QUEM090230", "Trade Shows", bookCap4);
////		inters.addEvent("QUEM010230", "Trade Shows", bookCap5);
////		inters.addEvent("QUEA300230", "Trade Shows", bookCap7);
////		inters.addEvent("QUEM300230", "Trade Shows", bookCap6);
//		
////		
//		inters.bookEvent("SHEC123","QUEM180230", "Trade Shows");
//		inters.bookEvent("SHEC122","QUEM180230", "Trade Shows");
////		inters.bookEvent("SHEC123","QUEE140320", "Trade Shows");
		
		String getInfo;
		String sendInfo = null;
		String registryURL;
		int port = 5000;
		

		startRegistry(5000);
        Naming.rebind("rmi://localhost:" + port + "/QUE",inters);
		System.out.println("QUE_Server is on");
		         
        DatagramSocket aSocket = null;
	    try{
				aSocket = new DatagramSocket(6000);
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
		        		sendInfo = inters.getOwnlistEventAvailability(eventType);	
		        	}
		        	else if (getInfo.contains("book"))
		        	{
		        		String[] info = getInfo.split(",");
		        		String customerID = info[0];
		        		String eventID = info[1];
		        		String eventType = info[2];
		        		sendInfo = inters.bookEvent(customerID, eventID, eventType);
		        	}
		        	else if (getInfo.contains("cshashmap"))
		        	{
		        		String[] info = getInfo.split(",");
		        		String customerID = info[0];
		        		sendInfo = inters.getBookingSchedule(customerID);
		        	}
		        	else if (getInfo.contains("cancel"))
		        	{
		        		String[] info = getInfo.split(",");
		        		String customerID = info[0];
		        		String eventID = info[1];
		        		String eventType = info[2];
		        		sendInfo = inters.cancelEvent(customerID, eventID, eventType);
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
