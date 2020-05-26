package Servers;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

public class ShutDownWork extends Thread 
{
	@Override
	public void run() 
	{
		try 
		{
			System.out.println("Unbind");
			Naming.unbind("rmi://localhost:" + 5001 + "/MTL");
		} catch (IOException | NotBoundException ex) 
		{
			System.err.println("exception at ShutDownWork#run()...");
		}
	}
}