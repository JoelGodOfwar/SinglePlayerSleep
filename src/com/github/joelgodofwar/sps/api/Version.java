package com.github.joelgodofwar.sps.api;

import org.apache.commons.lang.math.NumberUtils;


public class Version{
	private int Major; // 1
	private int Minor; // 16
	private int Patch; // 1
	private int Build; // ?
	private boolean isDev = false;
	public Version(String string){
		String[] string2 = string.split(".");
		this.Major = NumberUtils.toInt(string2[0]);
		this.Minor = NumberUtils.toInt(string2[1]);
		this.Patch = NumberUtils.toInt(string2[2]);
		if(string2[3] != null){
			if(string2[3].toUpperCase().contains("D")){
				isDev =  true;
				string2[3] = string2[3].toUpperCase().replace("D", "");
			}
			this.Build = NumberUtils.toInt(string2[3]);
		}else{
			this.Build = 0;
		}
	}
	public int Major(){
		return Major;
	}
	public int Minor(){
		return Minor;
		
	}
	public int Patch(){
		return Patch;
		
	}
	public int Build(){
		return Build;
		
	}
	public boolean isDev(){
		return isDev;
	}
}
