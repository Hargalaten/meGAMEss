package sfs2x.extensions.projectsasha.game.entities;

import java.util.Iterator;
import java.util.List;

import sfs2x.extensions.projectsasha.game.GameConsts;
import sfs2x.extensions.projectsasha.game.entities.gateways.Gateway;
import sfs2x.extensions.projectsasha.game.entities.software.Software;


import com.smartfoxserver.v2.entities.User;
//import sfs2x.extensions.projectsasha.game.User;
import com.smartfoxserver.v2.entities.data.SFSObject;

// Player class representing an individual soldier in the world simulation
public class Player 
{
	private User sfsUser; // SFS user that corresponds to this player
	private String name;
	private int money;
	private boolean canHack;
	private Software[] inventory;
	private List<Quests> current;
	
	
	public Player(User sfsUser) 
	{
		this.sfsUser = sfsUser;
		this.name = sfsUser.getName();
		this.money = 0;
		this.canHack = true;
		this.inventory = new Software[GameConsts.INVENTORY_SIZE];
	}


	public SFSObject toSFSObject()
	{
		SFSObject playerData = new SFSObject();

		playerData.putInt("id", sfsUser.getId());
		playerData.putUtfString("name", this.name);
		playerData.putInt("money", this.money);
		
		return playerData;
	}
	
	public SFSObject toInfosSFSObject()
	{
		SFSObject playerData = new SFSObject();
		
		playerData.putInt("id", sfsUser.getId());
		playerData.putUtfString("name", this.name);
		
		return playerData;
	}
	
	public String getName()
	{
		String ownerName ="none";
		if(this!=null)
			ownerName = this.name;
		return ownerName;
	}
	
	public String getUserName()
	{
		return this.name;
	}
	
	public List<Quests> getQuest()
	{
		return this.current;
	}
	public void addMoney(int value)
	{
			this.money += value;
	}
	
	public Software[] getInventory()
	{
		return inventory;
	}
	
	public boolean hasItems(String type)
	{
		for(Software s : this.inventory)
			if(s!=null && type == s.getType())
				return true;
		return false;
	}
	
	public void removeMoney(int value)
	{
		this.money -= value;
	}
	
	public int getMoney()
	{
		return this.money;
	}
	
	public void setCanHack(boolean value)
	{
		this.canHack = value;
	}
	
	public int getNextInventorySlotAvailable()
	{
		int slot = 0;
		for(Software s : inventory)
			if(s != null)
				slot++;
		
		return slot;
	}
	
	public void setInventory(Software s)
	{
		inventory[this.getNextInventorySlotAvailable()] = s;
	}
	
	public boolean canHack()
	{
		return this.canHack;
	}
	
	public Gateway[] getAllConqueredGateway(GameWorld world)
	{
		Iterator<String> itr = world.gateways.keySet().iterator();
		Gateway currentGW;
		Gateway[] ret = new Gateway[getConqueredGateway(world)];
		String str;
		int counter = 0;

		while (itr.hasNext()) 
		{
			str = itr.next();
			currentGW = world.gateways.get(str);
			
			if(currentGW.getOwner() == this)
			{
				ret[counter] = currentGW;
				counter++;
			}
		}
		
		
		return ret;
		
	}
	public int getConqueredGateway(GameWorld world)
	{
		Iterator<String> itr = world.gateways.keySet().iterator();
		Gateway currentGW;
		String str;
		int counter = 0;
		
		while (itr.hasNext()) 
		{
			str = itr.next();
			currentGW = world.gateways.get(str);
			
			if(currentGW.getOwner() == this)
				counter++;
		}
		
		return counter;
		
	}
	
	public int getConqueredGateway(GameWorld world, String type)
	{
		Iterator<String> itr = world.gateways.keySet().iterator();
		Gateway currentGW;
		String str;
		int counter = 0;
		
		while (itr.hasNext()) 
		{
			str = itr.next();
			currentGW = world.gateways.get(str);
			
			if(currentGW.getClass().getSimpleName() != type)
				continue;
			
			if(currentGW.getOwner() == this)
				counter++;
		}
		
		return counter;
	}
	
	public boolean questComplete(Quests q, Gateway g)
	{
		if(q.getGoal().equals(g.getName()) && this.getName().equals(g.getOwner().getName()))
			return true;
		return false;
	}
	
	@Override 
	public String toString(){
		String returnString = "/*****************************************\\\n";
		returnString += " * Name:\t "+this.name+"\n";
		returnString += " * Money:\t "+this.money+"\n";
		returnString += "\\*****************************************/";
		return returnString;
		
	}
}
