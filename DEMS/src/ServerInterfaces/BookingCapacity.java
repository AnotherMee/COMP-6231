package ServerInterfaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BookingCapacity implements Serializable
{

	private int bookcap;
	List <String> customerIDs = new ArrayList<String>();
	
	
	public BookingCapacity(int bookcap)
	{
		this.bookcap = bookcap;
	}
	
	public int getBookcap() 
	{
        return this.bookcap;
    }
	
	public void setBookcap(int bookcap) 
	{
        this.bookcap = bookcap - 1;
    }
	
	public void addCustomerID(String id)
	{
		customerIDs.add(id);
	}
	public List<String> getCustomerID()
	{
		return this.customerIDs;
		
	}
	

}
