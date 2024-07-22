package com.github.joelgodofwar.sps.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class StrUtils {
	/** StringRight */
	public static String Right(String input, int chars){
		if (input.length() > chars){
			//System.out.println("Right input=" + input);
			return input.substring(input.length() - chars);
		}
		else{
			return input;
		}
	}

	/** StringLeft */
	public static String Left(String input, int chars){
		if (input.length() > chars){
			//System.out.println("Left input=" + input);
			//System.out.println("Left chars=" + chars);
			return input.substring(0, chars);
		}
		else{
			return input;
		}
	}

	public  static boolean stringContains(String string, String string2){
		String[] string3 = string.split(", ");
		for(int i = 0; i < string3.length; i++){
			if(string3[i].equals(string2)){
				return true;
			}
		}
		return false;
	}

	// §x§1§1§1§1§1§1D§x§2§2§2§2§2§2e§x§3§3§3§3§3§3M§x§4§4§4§4§4§4o§x§5§5§5§5§5§5N
	public static String parseRGBNameColors(String string){
		String nickPrefix = "\"}";
		String nickString = ",{\"text\":\"<text>\",\"color\":\"<color>\"}";
		String nickString2 = ",{\"text\":\"<text>\",\"bold\":<bvalue>,\"obfuscated\":<ovalue>,\"italic\":<ivalue>,\"underlined\":<uvalue>,\"strikethrough\":<svalue>,\"color\":\"<color>\"}";
		String nickSuffix = ",{\"text\":\"";
		//String[] string2 = null;
		List<String> string2 = new ArrayList<String>();
		String string3 = "";
		String string4 = "";
		String color;
		String text;       // #FF00FF
		boolean bold,obfus,italic,under,strike;
		//int counter = 0; // SxSFSFS0S0SFSF
		int index;
		if(string.contains("§x")){
			int count = StringUtils.countMatches(string, "§x");
			//System.out.println("count=" + count);
			for(int i = 0; i < count; i++){//while(string.length() > 0){
				index = string.indexOf("§x");
				//System.out.println("i=" + i);
				//System.out.println("index=" + index);
				//System.out.println("index2=" + string.indexOf("§x", index + 1));
				int index2 = string.indexOf("§x", index + 1);
				if(index2 <= 1){
					string2.add(Left(string, string.length()));
				}else{
					string2.add(Left(string, index2)); // §x§1§1§1§1§1§1D
				}
				string = string.replace(string2.get(i), "");
			}
			for(int i = 0; i < count; i++){
				string4 = string2.get(i).replace("§x", "#");
				if(string4.toLowerCase().contains("§l")) {	bold = true;	}else {	bold = false;	}
				if(string4.toLowerCase().contains("§k")) {	obfus = true;	}else {	obfus = false;	}
				if(string4.toLowerCase().contains("§o")) {	italic = true;	}else {	italic = false;	}
				if(string4.toLowerCase().contains("§n")) {	under = true;	}else {	under = false;	}
				if(string4.toLowerCase().contains("§m")) {	strike = true;	}else {	strike = false;	}
				string4 = string4.replace("§l", "").replace("§k", "").replace("§o", "").replace("§n", "").replace("§m", "")
						.replace("§L", "").replace("§K", "").replace("§O", "").replace("§N", "").replace("§M", "");
				string4 = string4.replace("§", "");
				//System.out.println("String4=" + string4);
				color = Left(string4, 7).replace("§x", "#").replace("§", "");
				//System.out.println("color=" + color);
				text = Right(string4, string4.length() - 7);
				//System.out.println("text=" + text);
				if (bold||obfus||italic||under||strike) {
					string3 = string3 + nickString2.replace("<color>", color).replace("<text>", text)
							.replace("<bvalue>", "" + bold).replace("<ovalue>", "" + obfus).replace("<ivalue>", "" + italic)
							.replace("<uvalue>", "" + under).replace("<svalue>", "" + strike);
				}else {
					string3 = string3 + nickString.replace("<color>", color).replace("<text>", text);
				}

			}
			return nickPrefix + string3 + nickSuffix;
		}
		return string;
	}

	public static String stripRGBColors(String string){
		List<String> string2 = new ArrayList<String>();
		String string3 = "";
		String string4 = "";
		//String color;
		String text;       // #FF00FF
		//int counter = 0; // SxSFSFS0S0SFSF
		int index;
		if(string.contains("§x")){
			int count = StringUtils.countMatches(string, "§x");
			//System.out.println("count=" + count);
			for(int i = 0; i < count; i++){//while(string.length() > 0){
				index = string.indexOf("§x");
				//System.out.println("i=" + i);
				//System.out.println("index=" + index);
				//System.out.println("index2=" + string.indexOf("§x", index + 1));
				int index2 = string.indexOf("§x", index + 1);
				if(index2 <= 1){
					string2.add(Left(string, string.length()));
				}else{
					string2.add(Left(string, index2)); // §x§1§1§1§1§1§1D
				}
				string = string.replace(string2.get(i), "");
			}
			for(int i = 0; i < count; i++){
				string4 = string2.get(i).replace("§x", "#");
				string4 = string4.replace("§", "");
				//System.out.println("String4=" + string4);
				//color = Left(string4, 7).replace("§x", "#").replace("§", "");
				//System.out.println("color=" + color);
				text = Right(string4, string4.length() - 7);
				//System.out.println("text=" + text);
				string3 = string3 + text;
			}
			return string3;
		}
		return string;
	}
}
