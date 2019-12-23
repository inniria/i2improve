package net.inniria.wurm.i2improve;


import com.wurmonline.client.renderer.PickableUnit;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.wurmonline.client.game.inventory.InventoryMetaItem;


public class WorldItem {
	Pattern fltPattern = Pattern.compile("([0-9]+[.][0-9]+)");
	long itemId;
	String nextTool;
	boolean damaged;
	
	public WorldItem(PickableUnit item) {
		this.itemId = item.getId();
		this.nextTool = null;
		this.damaged = false;
	}
	
	public String getNextTool() {
		return this.nextTool;
	}
	
	public boolean equals(PickableUnit item) {
		return (item.getId() == this.itemId);
	}
	
	public boolean getDamaged() {
		return this.damaged;
	}
	
	public void setDamaged(boolean value) {
		this.damaged = value;
	}
	
	public boolean validTool(InventoryMetaItem tool) {
		if(this.nextTool == null) return false;
		return tool.getBaseName().contains(this.nextTool);
	}
	
	public void parseMessage(String message) {
		// Parse improving messages and update item state accordingly
		if(message.contains("damage the")) {
			this.damaged = true;
		}else if(message.contains("before you try to improve")){
			this.damaged = true;
		}else if(message.contains("with a log") || message.contains("more log")){
    		this.nextTool = "log";
		}else if(message.contains("with a rock shards") || message.contains("more rock shards")){
			this.nextTool = "rock shards";
		}else if(message.contains("with a string") || message.contains("more string")){
			this.nextTool = "string";
    	}else if(message.contains("use a mallet")){
    		this.nextTool = "mallet";
    	}else if(message.contains("use a file")){
    		this.nextTool = "file";
    	}else if(message.contains("want to polish")){
    		this.nextTool = "pelt";
    	}else if(message.contains("carve away")){
    		this.nextTool = "carving knife";
    	}else if(message.contains("with a stone chisel")) {
    		this.nextTool = "stone chisel";
    	}else if(message.contains("some stains")) {
    		this.nextTool = "water";
    	}else if(message.contains("must be backstitched") || message.contains("by slipstitching")){
    		this.nextTool = "needle";
    	}else if(message.contains("be cut away")){
    		this.nextTool = "scissors";
    	}
		
		// Parse damage value of item descriptions
		String[] msg_parts = message.split(", Dam: ");
		if(msg_parts.length > 1) {
			Matcher m = fltPattern.matcher(msg_parts[1]);
			if(m.find()) this.damaged = (Float.parseFloat(m.group()) > 0);
		}
	}
}
