package sfs2x.projectsasha.game.entities.software;

import sfs2x.projectsasha.game.GameConsts;
import sfs2x.projectsasha.game.entities.gateways.Gateway;


public class Sniffer extends Software
{
	
	
	public Sniffer() 
	{
		super(GameConsts.SNIFFER_NAME, 1);
		setCumulative(GameConsts.SNIFFER_CUMULATIVE);
		setType(GameConsts.SNIFFER);
		setDescription(GameConsts.SNIFFER_DESCRIPTION);
		setCost(GameConsts.SNIFFER_COST,1);
	}
	
	
	public Sniffer(int version) 
	{
		super(GameConsts.SNIFFER_NAME, version);
		setCumulative(GameConsts.SNIFFER_CUMULATIVE);
		setType(GameConsts.SNIFFER);
		setDescription(GameConsts.SNIFFER_DESCRIPTION);
		setCost(GameConsts.SNIFFER_COST,version);
	}
	
	@Override
	public void upgrade()
	{
		if (this==null)
			return;
		if(this.version < GameConsts.SNIFFER_MAX_LEVEL)
			this.version += 1;
		else
			return;
	}	
	
	public int getDetectedItems()
	{
		return this.version;
	}
	
	@Override
	public void runTriggeredAction(Gateway from, Gateway to)
	{
		for(int i=0; i<this.getVersion();i++)
		{
			// mostrare oggetto nemico
		}
			
	}
}
