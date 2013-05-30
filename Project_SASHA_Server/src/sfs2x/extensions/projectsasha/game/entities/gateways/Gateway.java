package sfs2x.extensions.projectsasha.game.entities.gateways;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.smartfoxserver.v2.entities.data.SFSObject;

import sfs2x.extensions.projectsasha.game.GameConsts;
import sfs2x.extensions.projectsasha.game.entities.Player;
import sfs2x.extensions.projectsasha.game.ia.Trace;
import sfs2x.extensions.projectsasha.game.entities.software.Software;
import sfs2x.extensions.projectsasha.game.entities.software.SoftwareFactory;

public abstract class Gateway 
{	
	
	private static int lastGatewayID = 1000;
	
	private List<Trace> traces;
	private Software[] installedSoftware;
	private Gateway[] neighboors;
	private Map<String,Integer> distanceFromAttackGateway = new Hashtable<String,Integer>();
	private Player owner;
	private String name, state;
	private int id;
	private Set<Integer> startedAttacks = Collections.synchronizedSet(new HashSet<Integer>());

	public Gateway(Player owner, String name, String state)
	{
		this.owner = owner;
		this.name = name;
		this.state = state;
		this.id = getNewID();
		this.traces = new Vector<Trace>();
		this.installedSoftware = new Software[GameConsts.MAX_SOFTWARE_INSTALLED];
	}	
	
	
	//GETTERS
	
	public int getID()
	{
		return id;
	}

	public Player getOwner()
	{
		return owner;
	}
	
	public void setOwner(Player p)
	{
		this.owner = p;
	}
	
	public Gateway[] getNeighboors()
	{
		return neighboors;
	}
	
	public List<Trace> getTraces()
	{
		return traces;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getState()
	{
		return state;
	}
	
	public int getWeightByRelevance(int attackRelevance)
	{
		float traceCount=0;
		
		for(Trace t : traces)
			if(t.relevance == attackRelevance)
				traceCount++;
		return (int)((1/(1+traceCount))*100);
	}
	
	public int getWeightByDistance(String playerName)
	{
		return distanceFromAttackGateway.get(playerName);
	}
	
	public void setWeightByDistance(String playerName, int weight)
	{
		distanceFromAttackGateway.put(playerName, weight);
	}
	
	public Set<Integer> getStartedAttacks() 
	{
		return startedAttacks;
	}

	public void addStartedAttack(int startedAttack) 
	{
		this.startedAttacks.add(startedAttack);
	}

	public void removeStartedAttack(int startedAttack) 
	{
		this.startedAttacks.remove(startedAttack);
	}

	public boolean hasStartedAttack(int startedAttack)
	{
		return this.startedAttacks.contains(startedAttack);
	}
	
	synchronized public void confiscate()
	{
		this.traces.clear();
		this.installedSoftware = new Software[GameConsts.MAX_SOFTWARE_INSTALLED];
		this.owner = null;
		this.startedAttacks.clear();	
	}
	
	public void resetWeightByDistance(String playerName)
	{
		setWeightByDistance(playerName, 0);
	}	
	
	synchronized public int getDefenceLevel()
	{
		int dl = getBaseDefenceLevel();
		
		for(Software s : installedSoftware)
		{
			if(s != null)
				dl += s.getDefenceLevel();
		}
		return dl;
	}
	
	synchronized public int getAttackLevel()
	{
		int dl = getBaseAttackLevel();
		
		for(Software s : installedSoftware)
		{
			if(s != null)
				dl += s.getAttackLevel();
		}
		return dl;
	}
	
	synchronized public Software[] getInstalledSoftwares()
	{
		return this.installedSoftware;
	}
	
	synchronized public Software getInstalledSoftware(String type)
	{
		for(Software s : installedSoftware)
		{
			if (s.getType() == type)
				return s;
		}
		return null;
	}
	
	synchronized public Software getInstalledSoftware(int slot)
	{
		return installedSoftware[slot];
	}
	
	//SETTERS
	
	public void setNeighborhoods(Gateway[] neighboors)
	{
		this.neighboors = neighboors;
	}
	
	
	//ABSTRACT METHODS

	protected abstract int getBaseAttackLevel();
	protected abstract int getBaseDefenceLevel();
	public abstract int getPaymentAmount();
	public abstract void onGatewayConquered(Player newOwner, Player oldOwner);
		
	//STATIC METHODS
	
	private static synchronized int getNewID()
	{
		return lastGatewayID++;
	}
	
	public static boolean hack(Gateway from, Gateway to){
		boolean ret = false;
		/*********************/
		/** hack logic here **/
		/*********************/
		int difference = to.getDefenceLevel()-from.getAttackLevel();
		if(difference < 0){
			if(to.hasSoftware(GameConsts.IDS))
			{
				Software s = to.getInstalledSoftware(GameConsts.IDS);
				s.runTriggeredAction(from, to);
			}
			to.setOwner(from.getOwner());
			ret = true;
		}else{
			ret = false;
		}
		return ret;
	}
	
	//NON STATIC METHODS
	synchronized public void installSoftware(String type, Player hacker)
	{
		Software newSoftware = SoftwareFactory.makeSoftware(type);
		if(this.owner!=hacker)
			return;

		
		if(this.hasSoftware(newSoftware.getType()) && !newSoftware.isCumulative())
			return;
		
		int available_slot = this.getNextSoftwareSlotAvailable();
		newSoftware.setVersion(1);
		
		if(available_slot < GameConsts.MAX_SOFTWARE_INSTALLED)
		{
			newSoftware.setSlot(available_slot);
			installedSoftware[available_slot] = newSoftware;
			
		}else
			return;		
	}
	
	synchronized public void uninstallSoftware(String type, Player p)
	{
		this.uninstallSoftware(type, p, 0);
	}
	
	synchronized public void uninstallSoftware(String type, Player hacker, int slot)
	{
		Software newSoftware = SoftwareFactory.makeSoftware(type);
		
		if(this.owner!=hacker)
			return;
		
		
		if(this.hasSoftware(type)) //is installed
		{
			Software theSoftware = this.getInstalledSoftware(type); 
			
			if(!this.getInstalledSoftware(type).isCumulative()) //not cumulative
			{
				theSoftware.setVersion(0);
				installedSoftware[theSoftware.getSlot()] = null;
			}
			else //cumulative
			{
				this.getInstalledSoftware(slot).setVersion(0);
				installedSoftware[slot] = null;
			}
		}
	}
	
	synchronized public boolean hasSoftware(String type){
		for(Software s : installedSoftware)
			if(s != null && type == s.getType())
				return true;
		return false;
	}
	
	synchronized public int getNextSoftwareSlotAvailable(){
		int slot = 0;
		
		for(Software s : installedSoftware)
			if(s != null)
				slot++;
		
		return slot;
	}
	
	synchronized public void upgradeSoftware(int slot, Player hacker){
		Software sw = null;
		try
		{
			sw = this.getInstalledSoftware(slot);
			this.upgradeSoftware(sw.getType(), hacker, slot);
		}
		catch(NullPointerException e)
		{/*SLOT EMPTY*/}
		
	}
	
	synchronized public void upgradeSoftware(String type, Player hacker){
	
		if(this.owner!=hacker)
			return;
		
		if(this.hasSoftware(type))
			this.getInstalledSoftware(type).upgrade();
		else
			return; 
	}
	
	synchronized public void upgradeSoftware(String type, Player hacker,  int slot){
		
		if(this.owner!=hacker)
			return;
		
		if(this.hasSoftware(type))
			this.getInstalledSoftware(slot).upgrade();
		else
			return;
	}
	
	synchronized public void downgradeSoftware(String type, Player hacker){
		
		Software newSoftware = SoftwareFactory.makeSoftware(type);
		
		if(this.owner!=hacker)
			return;
		
		if(this.hasSoftware(type))
			this.getInstalledSoftware(type).downgrade();
		else
			return;
		}
	
	synchronized public void downgradeSoftware(String type, Player hacker, int slot){
		
		Software newSoftware = SoftwareFactory.makeSoftware(type);
		
		if(this.owner!=hacker)
			return;
		
		if(this.hasSoftware(type))
			this.getInstalledSoftware(slot).downgrade();
		else
			return; 
	}
	
	@Override
	synchronized public String toString(){
		String ownerName = this.owner == null?"NEUTRAL":this.owner.getName();
		String returnString = "/*****************************************\\\n";
		returnString += " * Owner:\t "+ownerName+"\n";
		returnString += " * Name:\t "+this.name+"\n";
		returnString += " * State:\t "+this.state+"\n";
		returnString += " * Id:\t\t "+this.id+"\n";
		returnString += " * Attak:\t "+this.getAttackLevel()+"\n";
		returnString += " * Defence:\t "+this.getDefenceLevel()+"\n";
		returnString += " * Software:\t "+Arrays.toString(this.getInstalledSoftwares())+"\n";
		returnString += "\\*****************************************/";
		return returnString;
	}
	
}
