package sfs2x.extensions.projectsasha.game.entities.software;

import sfs2x.extensions.projectsasha.game.entities.gateways.Gateway;

public abstract class Software 
{
	protected int version;	//Software version
	protected String name;	//Software name (ex: firewall, proxy...)
	protected int slot;
	protected boolean cumulative;
	protected String type;
	protected boolean hasTriggeredAction;
	protected String description;
	
	
	//CONSTRUCTORS
	
	public Software(String name, int version)
	{
		this.name = name;
		this.version = version;
		this.cumulative = false;
		this.type = "SOFTWARE";
		this.hasTriggeredAction = false;
		this.description = "";
	}
	
	
	//GETTERS
	
	public int getDefenceLevel()	//The defensive level (if not overridden returns 0)
	{
		return 0;
	}
	
	public int getAttackLevel()		//The offensive level (if not overridden returns 0)
	{
		return 0;
	}
	
	public int getVersion()
	{
		return version;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean getTriggeredAction()
	{
		return this.hasTriggeredAction;
	}
	
	public int getSlot()
	{
		return slot;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public boolean isCumulative()
	{
		return this.cumulative;
	}
	
	//SETTERS
	
	public void setVersion(int version)
	{
		
		this.version = version;
	}	
	
	public void setSlot(int theSlot)
	{
		this.slot = theSlot;
	}
	
	public void setCumulative(boolean cumulative)
	{
		this.cumulative = cumulative;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public void setTriggeredAction(boolean hasTriggeredAction)
	{
		this.hasTriggeredAction = hasTriggeredAction;
	}
	

	//OTHERS
	
	public void upgrade()
	{
		this.version += 1;
	}	
	
	public void downgrade()
	{
		if(this.version == 1)
			return;

		this.version -= 1;
	}	
	
	public void startTimedEvent()
	{
		return;
	}
	
	public void startTimedEvent(Gateway from, Gateway to)
	{
		//Add delayed event here
		return;
	}
	
	public void runTriggeredAction(Gateway from, Gateway to)
	{
		return;
	}

	public void runTriggeredAction() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String toString(){
		return this.getName() + " V"+this.getVersion();
	}
	
}
