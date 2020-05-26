package ServerInterfaces;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.io.File;
import java.io.FileWriter;




public class QUEimplement extends UnicastRemoteObject implements Interfaces
{
	public QUEimplement que;
	public BookingCapacity queCapAndList;
	public HashMap<String, HashMap<String , BookingCapacity>> eventRecords = new HashMap<>();
	public HashMap<String, List<String>> customerHM = new HashMap<>();


	public QUEimplement() throws RemoteException 
	{
		super();
	}
	
	@Override
	public String addEvent(String eventID, String eventType, BookingCapacity bookCap) throws RemoteException 
	{
		
		if (eventRecords.containsKey(eventType))
		{
			HashMap<String, BookingCapacity> events = eventRecords.get(eventType);
			
			if (events.containsKey(eventID))
			{
				
				events.put(eventID,bookCap);
				eventRecords.put(eventType, events);
				return "Add an event successful";
				
			}
			
			else
			{
				
				events.put(eventID,bookCap);
				eventRecords.put(eventType, events);
				return "Add an event successful";
			}
			
		}
		else
		{
			
			HashMap<String, BookingCapacity> events = new HashMap<>();
			events.put(eventID,bookCap);
			eventRecords.put(eventType, events);
			return "Add an event successful";
		}
	}
	
	
	@Override
	public synchronized String removeEvent(String eventID, String eventType) throws RemoteException
	{	
		String check=null;
		if (eventRecords.containsKey(eventType))
		{			
			HashMap<String, BookingCapacity> events = eventRecords.get(eventType);
			if (events.containsKey(eventID))
			{
				queCapAndList = events.get(eventID);
				List<String> idlist = queCapAndList.getCustomerID();
				List<String> list = new LinkedList(events.keySet());
				Collections.sort(list, new Comparator<String>()
				{
					public int compare(String o1, String o2)
					{						 
						int year1 = Integer.parseInt(o1.substring(8, 10));
		                int year2 = Integer.parseInt(o2.substring(8, 10));
		                if (year1<year2) return -1;
		                else if (year1>year2) return 1;
		                else
		                {
		                	int month1 = Integer.parseInt(o1.substring(6, 8));
		                	int month2 = Integer.parseInt(o2.substring(6, 8));
		                	if (month1<month2) return -1;
		                	else if (month1>month2) return 1;
		                	else
		                	{
		                		int date1 = Integer.parseInt(o1.substring(4,6));  
		                		int date2 = Integer.parseInt(o2.substring(4,6)); 
		                		if (date1<date2) return -1;
		                		else if (date1>date2) return 1;
		                		else 
		                		{
		                			char t1 = o1.charAt(3);
		                			char t2 = o2.charAt(3);
		                			
		                			if (t1==t2) return 0;
		                			else
		                			{
		                				int t11 = -1; int t22 = -1;
		                				if (t1=='M') t11 = 0;
		                				else if (t1=='A') t11 = 1;
		                				else t11 = 2;
		                				
		                				if (t2=='M') t22 = 0;
		                				else if (t2=='A') t22 = 1;
		                				else t22 = 2;
		                				
		                				return t11-t22;
		                			}
		                		}				 
		                	}
		                }
					}
				});
								
				int index = list.indexOf(eventID);
				for (int j = index + 1; j <list.size(); j++)
				{
					queCapAndList = events.get(list.get(j));
					System.out.println("ID" + list.get(j));
					int cap = queCapAndList.getBookcap();
					while (cap > 0 && idlist.size() > 0)
					{
						bookEvent(idlist.remove(0), list.get(j), eventType);
						cap--;
					}
					if (idlist.size()<=0)
					{
						break;
					}
		
				}
				events.remove(eventID);
			}
			check = "Remove an event and rebook another one successful";
		}
		else
		{
			check =  "Cannot find event Remove unsuccessful";
		}
		return check;
	}

	@Override
	public synchronized String listEventAvailability(String eventType)throws RemoteException
	{
		String eventIDs = null;
		String caps = null;
		String avaEvents = null;
		if (eventRecords.containsKey(eventType))
		{
			HashMap<String, BookingCapacity> events = eventRecords.get(eventType);
			for (String key: events.keySet())
			{
				eventIDs = key;
				caps = String.valueOf(events.get(key).getBookcap());
				avaEvents = avaEvents + " " + (eventIDs+" "+caps);
			}
		}
		
		try 
		{
			String info = eventType+",list";
			System.out.println(info);
			DatagramSocket aSocket = null;
			aSocket = new DatagramSocket();
			byte [] m = new byte[1000];
			InetAddress aHost = InetAddress.getByName("localhost");
			m = (info).getBytes();
			
			// get events from MTL
			int serverPort = 6001;
			DatagramPacket request = new DatagramPacket(m, info.length(), aHost, serverPort);
			aSocket.send(request);
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			avaEvents = (avaEvents + " " +(new String(reply.getData()))).trim();
			aSocket.close();
			
			// get events from SHE
			aSocket = new DatagramSocket();
			serverPort = 6002;
			request = new DatagramPacket(m, info.length(), aHost, serverPort);
			aSocket.send(request);
			buffer = new byte[1000];
			reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			avaEvents = (avaEvents + " " + (new String(reply.getData()))).trim();
			aSocket.close();
		}
		catch (SocketException e)
		{System.out.println("Socket: " + e.getMessage());}
		catch (IOException e)
		{System.out.println("IO: " + e.getMessage());}
		
		return avaEvents + " list avalible event successful";		
	}
	
	@Override
	public String getOwnlistEventAvailability(String eventType) throws RemoteException 
	{
		String eventIDs = null;
		String caps = null;
		String avaEvents = null;
		if (eventRecords.containsKey(eventType))
		{
			HashMap<String, BookingCapacity> events = eventRecords.get(eventType);
			for (String key: events.keySet())
			{
				eventIDs = key;
				caps = String.valueOf(events.get(key).getBookcap());
				avaEvents = avaEvents + " " + (eventIDs+" "+caps);
			}
		}
		return avaEvents;
	}
	@Override
	public synchronized String bookEvent(String customerID, String eventID, String eventType) throws RemoteException 
	{
		String reply = null;
		String server = eventID.substring(0,3);
		if (server.equals("QUE"))
		{
			if (eventRecords.containsKey(eventType))
			{
				HashMap<String, BookingCapacity> events = eventRecords.get(eventType);
				
				if (events.containsKey(eventID))
				{
					queCapAndList = events.get(eventID);
					if (queCapAndList.getBookcap() != 0)
					{
						queCapAndList.addCustomerID(customerID);
						queCapAndList.setBookcap(queCapAndList.getBookcap());
						if (customerHM.containsKey(customerID))
						{
							List <String> eventIDs = new ArrayList();
							eventIDs = customerHM.get(customerID);
							eventIDs.add(eventID);
							customerHM.put(customerID, eventIDs);
							
						}
						else
						{
							List <String> eventIDs = new ArrayList();
							eventIDs.add(eventID);
							customerHM.put(customerID, eventIDs);
						}
						reply = "Booked the event successful";
						
					}
					else
					{
						reply = "Capbility is 0 booking unsuccessful";
					}
					
				}
				else {reply = "booking unsuccessful";}
				
			}
			else {reply = "booking unsuccessful";}
		}
		else
		{
			String cusHM = getBookingSchedule(customerID);
			if (cusHM.contains("QUE") == false && cusHM.contains("MTL") == false && cusHM.contains("SHE") == false)
			{
				reply = bookOtherServer(customerID, eventID, eventType);
			}
			else
			{								
				String[] eventIDs = cusHM.split(" ");
				List <Date> list = new ArrayList();
				for (int i=0; i<eventIDs.length; i++)
				{
					if (eventIDs[i].contains("MTL")|| eventIDs[i].contains("SHE"))
					{
						String year = "20"+ eventIDs[i].substring(8,10);
						String month = eventIDs[i].substring(6,8);						
						String day = eventIDs[i].substring(4,6);						
						String date = year + "-" + month + "-" + day;
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
						Date date1 = null;
						try {
							date1 = format.parse(date);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						list.add(date1);
					}
				}
				
				if (list.size() < 3)
				{
					reply = bookOtherServer(customerID, eventID, eventType);
				}
				else 
				{
					String year = "20"+ eventID.substring(8,10);
					String month = eventID.substring(6,8);						
					String day = eventID.substring(4,6);						
					String cdate = year + "-" + month + "-" + day;
					System.out.println(cdate);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
					Date cmdate = null;
					
					try {
						cmdate = format.parse(cdate);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					int count = 0;
					String sameWeek = null;
					for (int i=0; i<list.size(); i++)
					{
						try {
							sameWeek = sameWeek(list.get(i),cmdate);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if (sameWeek.equals("yes"))
						{
							count++;
						}
					}	
					
					if (count == 3 || count > 3)
					{
						return "Max_3_events_in_one_week_booking_unsuccessful";
					}
					else
					{
						reply = bookOtherServer(customerID, eventID, eventType);
					}
								
				}
			}
		}
		return reply;
	}
	
	public synchronized String bookOtherServer (String customerID, String eventID, String eventType)
	{
		String reply = null;
		String server = eventID.substring(0,3);
		try 
		{
			String info = customerID+"," + eventID + "," + eventType + "," + "book";
			DatagramSocket aSocket = null;
			aSocket = new DatagramSocket();
			byte [] m = new byte[1000];
			InetAddress aHost = InetAddress.getByName("localhost");
			m = (info).getBytes();	
			
			// get events from MTL
			if (server.equals("MTL"))
			{
				System.out.println("reach MTL");
				int serverPort = 6001;
				DatagramPacket request = new DatagramPacket(m, info.length(), aHost, serverPort);
				aSocket.send(request);
				byte[] buffer = new byte[1000];
				DatagramPacket book_reply = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(book_reply);
				reply = (new String(book_reply.getData())).trim();
				System.out.println(reply);
				aSocket.close();
			}
			
			else if (server.equals("SHE"))
			{
				int serverPort = 6002;
				DatagramPacket request = new DatagramPacket(m, info.length(), aHost, serverPort);
				aSocket.send(request);
				byte[] buffer = new byte[1000];
				DatagramPacket book_reply = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(book_reply);
				reply = (new String(book_reply.getData())).trim();
				aSocket.close();
			}
		}
		catch (SocketException e)
		{System.out.println("Socket: " + e.getMessage());}
		catch (IOException e)
		{System.out.println("IO: " + e.getMessage());}
		
		return reply;
		
	}
	
	public String sameWeek(Date date1, Date date2) throws ParseException
	{

		Calendar day_1 = Calendar.getInstance();
		Calendar day_2 = Calendar.getInstance();
		day_1.setTime(date1);
		day_2.setTime(date2);
		int year = day_1.get(Calendar.YEAR)-day_2.get(Calendar.YEAR);
		if(year == 0)
		  {
		  if(day_1.get(Calendar.WEEK_OF_YEAR) == day_2.get(Calendar.WEEK_OF_YEAR))
		    return "yes";
		  }
		else if(year==-1 && day_1.get(Calendar.MONTH)==11)
		  {
		  if(day_1.get(Calendar.WEEK_OF_YEAR) == day_2.get(Calendar.WEEK_OF_YEAR))
		    return "yes";
		   
		  }
		else if(year==1 && day_2.get(Calendar.MONTH)==11)
		  {
		  if(day_1.get(Calendar.WEEK_OF_YEAR) == day_2.get(Calendar.WEEK_OF_YEAR))
		    return "yes";
		  }
	
		return "no";
		
	}
	@Override
	public String getBookingSchedule(String customerID) throws RemoteException 
	{	
		String server = customerID.substring(0,3);
		String cusHM = null;
		if (customerHM.containsKey(customerID))
		{
			List <String> eventIDs = new ArrayList();
			eventIDs = customerHM.get(customerID);
			cusHM = String.join(" ",eventIDs);

		}
		if (server.equals("QUE"))
		{
			try 
			{
				String info = customerID +",cshashmap";
				DatagramSocket aSocket = null;
				aSocket = new DatagramSocket();
				byte [] m = new byte[1000];
				InetAddress aHost = InetAddress.getByName("localhost");
				m = (info).getBytes();
				
				// get events from MTL
				int serverPort = 6001;
				DatagramPacket request = new DatagramPacket(m, info.length(), aHost, serverPort);
				aSocket.send(request);
				byte[] buffer = new byte[1000];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(reply);
				cusHM = (cusHM + " " +(new String(reply.getData()))).trim();
				System.out.println(cusHM);
				aSocket.close();
				
				// get events from SHE
				aSocket = new DatagramSocket();
				serverPort = 6002;
				request = new DatagramPacket(m, info.length(), aHost, serverPort);
				aSocket.send(request);
				buffer = new byte[1000];
				reply = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(reply);
				cusHM = (cusHM + " " + (new String(reply.getData()))).trim();
				System.out.println(cusHM);
				aSocket.close();
			}
			catch (SocketException e)
			{System.out.println("Socket: " + e.getMessage());}
			catch (IOException e)
			{System.out.println("IO: " + e.getMessage());}
		}
		return cusHM + " get_schedule_successful";
	}

	@Override
	public synchronized String cancelEvent(String customerID, String eventID, String eventType) throws RemoteException 
	{
		String server = eventID.substring(0,3);
		if(server.equals("QUE"))
		{
			if (eventRecords.containsKey(eventType))
			{
				HashMap<String, BookingCapacity> events = eventRecords.get(eventType);
				
				if (events.containsKey(eventID))
				{
					List<String> customerIDs = events.get(eventID).getCustomerID();
					for (int i = 0; i < customerIDs.size(); i++)
					{
						if (customerIDs.get(i).equals(customerID))
						{
							events.remove(eventID);
							return "Cancel event successful";
						}
					}	
				}
				else {return "Cancel event unsuccessful";}
			}
			else {return "Cancel event unsuccessful";}
		}
		else if (server.equals("MTL"))
		{
			String reply = null;
			try 
			{
				String info = customerID+"," + eventID + "," + eventType + ",cancel";
				DatagramSocket aSocket = null;
				aSocket = new DatagramSocket();
				byte [] m = new byte[1000];
				InetAddress aHost = InetAddress.getByName("localhost");
				m = (info).getBytes();	
				
				int serverPort = 6001;
				DatagramPacket request = new DatagramPacket(m, info.length(), aHost, serverPort);
				aSocket.send(request);
				byte[] buffer = new byte[1000];
				DatagramPacket book_reply = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(book_reply);
				reply = (new String(book_reply.getData())).trim();
				aSocket.close();
			}
			catch (SocketException e)
			{System.out.println("Socket: " + e.getMessage());}
			catch (IOException e)
			{System.out.println("IO: " + e.getMessage());}
			return reply;
		}
					
		else if (server.equals("SHE"))
		{
			String reply = null;
			try 
			{
				String info = customerID+"," + eventID + "," + eventType + ",cancel";
				DatagramSocket aSocket = null;
				aSocket = new DatagramSocket();
				byte [] m = new byte[1000];
				InetAddress aHost = InetAddress.getByName("localhost");
				m = (info).getBytes();	
				
				int serverPort = 6002;
				DatagramPacket request = new DatagramPacket(m, info.length(), aHost, serverPort);
				aSocket.send(request);
				byte[] buffer = new byte[1000];
				DatagramPacket book_reply = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(book_reply);
				reply = (new String(book_reply.getData())).trim();
				aSocket.close();
			}
			catch (SocketException e)
			{System.out.println("Socket: " + e.getMessage());}
			catch (IOException e)
			{System.out.println("IO: " + e.getMessage());}
			return reply;
		}
		else
		{
			return "Cancel event unsuccessful";
		}
		return "Cancel event unsuccessful";
	}
			
}
