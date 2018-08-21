/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.athena.services.vo;

/**
 *
 * @author UserXP
 */
public class JSent {

	public JSent(){
		Bonus=0 ;
	}

	private String Cmd;
    private String evt;
    private int    Bonus;
    private long   Ag = 0;
    
    

	public int getBonus() {
		return Bonus;
	}

	public void setBonus(int bonus) {
		Bonus = bonus;
	}

	/**
     * Get the value of evt
     *
     * @return the value of evt
     */
    public String getEvt() {
        return evt;
    }

    /**
     * Set the value of evt
     *
     * @param evt new value of evt
     */
    public void setEvt(String evt) {
        this.evt = evt;
    }

    /**
     * Get the value of Cmd
     *
     * @return the value of Cmd
     */
    public String getCmd() {
        return Cmd;
    }

    /**
     * Set the value of Cmd
     *
     * @param Cmd new value of Cmd
     */
    public void setCmd(String Cmd) {
        this.Cmd = Cmd;
    }

	public long getAg() {
		return Ag;
	}

	public void setAg(long ag) {
		Ag = ag;
	}

}
