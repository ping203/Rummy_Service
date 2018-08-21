package com.athena.services.vo;

import java.io.Serializable;
import java.util.Date;

public class ConfigUpdate implements Serializable{
	
	public ConfigUpdate(){
		TimeUpdate = (new Date()).getTime();
		ConfigValue = 0;
		SourceConfig = 9 ;
	}
	
	private long TimeUpdate;
	private int ConfigValue;
	private int SourceConfig ;
	
	public long getTimeUpdate() {
		return TimeUpdate;
	}
	public int getConfigValue() {
		return ConfigValue;
	}
	public void setConfigValue(int configValue) {
		ConfigValue = configValue;
	}
	public int getSourceConfig() {
		return SourceConfig;
	}
	public void setSourceConfig(int sourceConfig) {
		SourceConfig = sourceConfig;
	}
	public void setTimeUpdate(long timeUpdate) {
		TimeUpdate = timeUpdate;
	}
	
}
