package net.inniria.wurm.i2improve;


import com.wurmonline.client.renderer.PickableUnit;
import com.wurmonline.client.game.inventory.InventoryMetaItem;


public class WorldItem {
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
		if(message.contains("damage the")) {
			this.damaged = true;
		}else if(message.contains("before you try to improve")){
			this.damaged = true;
		}else if(message.contains("with a log")){
    		this.nextTool = "log";
		}else if(message.contains("with a rock shards")){
			this.nextTool = "rock shards";
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
    	}
	}
}
