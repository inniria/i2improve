package net.inniria.wurm.i2improve;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.ArrayList;

import javassist.ClassPool;
import javassist.CtClass;

import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import com.wurmonline.client.WurmClientBase;
import com.wurmonline.client.renderer.gui.HeadsUpDisplay;
import com.wurmonline.client.renderer.gui.ToolBelt;
import com.wurmonline.client.renderer.PickableUnit;
import com.wurmonline.client.game.World;
import com.wurmonline.client.game.inventory.InventoryMetaItem;
import com.wurmonline.client.game.inventory.InventoryMetaWindowManager;
import com.wurmonline.client.game.inventory.InventoryMetaWindowView;
import com.wurmonline.shared.constants.PlayerAction;


public class I2Improve implements WurmClientMod, Initable, PreInitable {
	public static Logger logger = Logger.getLogger("I2Improve");
	public static ArrayList<WorldItem> worldItems = new ArrayList<WorldItem>();
	public static WorldItem selWorldItem = null;
    public static HeadsUpDisplay hud;
    
    @Override
    public void preInit() {

    }
    
    @Override
    public void init() {
        // Inject console handler
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();
            
            // Add console command handler
	        CtClass ctWurmConsole = classPool.getCtClass("com.wurmonline.client.console.WurmConsole");
	        ctWurmConsole.getMethod("handleDevInput", "(Ljava/lang/String;[Ljava/lang/String;)Z").insertBefore(
	                "if (net.inniria.wurm.i2improve.I2Improve.handleInput($1,$2)) return true;"
	        );
	        
	        // Add chat messages handler
	        CtClass ctWurmChat = classPool.getCtClass("com.wurmonline.client.renderer.gui.ChatPanelComponent");
	        ctWurmChat.getMethod("addText", "(Ljava/lang/String;Ljava/lang/String;FFFZ)V").insertBefore(
	                "net.inniria.wurm.i2improve.I2Improve.handleMessage($1,$2);"
	        );
        }catch(Throwable e){
        	logger.log(Level.SEVERE, "Error loading mod", e.getMessage());
        }
        
        // Hook HUD init to setup our stuff
        HookManager.getInstance().registerHook("com.wurmonline.client.renderer.gui.HeadsUpDisplay", "init", "(II)V", () -> (proxy, method, args) -> {
			method.invoke(proxy, args);
			hud = (HeadsUpDisplay) proxy;
			return null;
		});
    }
    
    public static boolean handleInput(final String cmd, final String[] data) {
    	// Check the command is an i2improve request
    	if(!cmd.toLowerCase().equals("i2improve")) return false;
    	
    	// If extra parameters, raise error
    	if(data.length > 1) {
    		hud.consoleOutput("I2Improve: Unsupported extra arguments passed to I2Improve.");
    		return true;
    	}
    	
    	// Fetch world and client
    	World world = hud.getWorld();
    	WurmClientBase client = world.getClient();
        if (world.getClient().isMouseUnavailable()) return true;
        
        // Apply i2improve to inventory items
        long[] oids = hud.getCommandTargetsFrom(client.getXMouse(), client.getYMouse());
        if(oids != null && oids.length > 0) {
        	I2Improve.improveInventoryItems(oids);
        	return true;
        }
    	
        // Apply i2improve to world items
        PickableUnit curHovered = world.getCurrentHoveredObject();
        if(curHovered != null) {
        	I2Improve.improveWorldItem(curHovered);
        	return true;
        }
        
		return true;
    }
    
    public static void handleMessage(String context, String message) {
    	if(!context.equalsIgnoreCase(":event")) return;
    	
    	// If looking at item description, try to capture world item
    	if(message.contains("the signature of its maker")) {
    		PickableUnit curHovered = hud.getWorld().getCurrentHoveredObject();
    		if(curHovered != null) selWorldItem = I2Improve.getWorldItem(curHovered);
    	}
    	
    	if(selWorldItem != null) selWorldItem.parseMessage(message);
    }
    
    public static void improveInventoryItems(long[] tgtIds) {
    	// Get objects given target IDs
    	if(tgtIds.length == 0) return;
    	InventoryMetaItem[] targets =  I2Improve.getObjectsFromIDs(tgtIds);
    	
    	// Get object with lowest quality
    	float minQuality = 1000;
    	InventoryMetaItem target = null;
    	for(InventoryMetaItem tgt: targets) {
    		if(tgt != null && tgt.getQuality() < minQuality) {
    			minQuality = tgt.getQuality();
    			target = tgt;
    		}
    	}
    	
    	// If target needs repair, send repair action
		long[] ids = { target.getId() };
    	if(target.getDamage() > 0) hud.sendAction(PlayerAction.REPAIR, target.getId());
    	
		// Look for valid tool on tool belt
    	ToolBelt toolBelt = hud.getToolBelt();
    	for(int i=0; i<toolBelt.getSlotCount(); i++) {
    		// Get tool from tool belt
    		InventoryMetaItem tTool = toolBelt.getItemInSlot(i);
    		if(tTool == null) continue;
    		
    		// If tool matches required improve item
    		if(tTool.getType() == target.getImproveIconId()) {
    			hud.getWorld().getServerConnection().sendAction(tTool.getId(), ids, PlayerAction.IMPROVE);
    			return;
    		}
    	}
    	
    	// If no tool match, use active item
    	hud.sendAction(PlayerAction.IMPROVE, ids);
    }
    
    public static void improveWorldItem(PickableUnit item) {
    	// Find corresponding world item
    	selWorldItem = I2Improve.getWorldItem(item);
    	
    	// If item damaged, first repair
    	long[] ids = { item.getId() };
    	if(selWorldItem.getDamaged()) {
    		hud.sendAction(PlayerAction.REPAIR, item.getId());
    		selWorldItem.setDamaged(false);
    	}
    	
    	// Look for valid tool on tool belt
    	ToolBelt toolBelt = hud.getToolBelt();
    	for(int i=0; i<toolBelt.getSlotCount(); i++) {
    		// Get tool from tool belt
    		InventoryMetaItem tTool = toolBelt.getItemInSlot(i);
    		if(tTool == null) continue;
    		
    		// If tool matches required improve item
    		if(selWorldItem.validTool(tTool)) {
    			hud.getWorld().getServerConnection().sendAction(tTool.getId(), ids, PlayerAction.IMPROVE);
    			return;
    		}
    	}
    	
    	// If no tool match, use active item
    	hud.sendAction(PlayerAction.IMPROVE, ids);
    }
    
    public static long[] getTargetIDs() {
    	World world = hud.getWorld();
    	WurmClientBase client = world.getClient();
    	
    	// If mouse not available, return null
        if (world.getClient().isMouseUnavailable()) return null;
        
        // Get targets from mouse coordinates
        PickableUnit curHovered = world.getCurrentHoveredObject();
        long[] oids = hud.getCommandTargetsFrom(client.getXMouse(), client.getYMouse());
        if (oids == null && curHovered != null) oids = new long[] { curHovered.getId() };
        return oids;
    }
    
    public static InventoryMetaItem[] getObjectsFromIDs(long[] ids) {
    	InventoryMetaWindowManager invManager = hud.getWorld().getInventoryManager();
    	InventoryMetaItem[] ret = new InventoryMetaItem[ids.length];
    	
    	// List inventories, starting with player inventory
    	ArrayList<InventoryMetaWindowView> inventories = new ArrayList<>();
		inventories.add(invManager.getPlayerInventory());
		
		// Add other inventories to list
    	try {
    		Map<Long, InventoryMetaWindowView> extraInvs = ReflectionUtil.getPrivateField(invManager, ReflectionUtil.getField(InventoryMetaWindowManager.class, "inventoryWindows"));
    		inventories.addAll(new ArrayList<>(extraInvs.values()));
    	}catch(Exception ex) {
    		hud.consoleOutput("I2Improve: Error accessing extra inventory windows.");
    	}
    	
    	// Look for items in inventories
    	for(InventoryMetaWindowView inventory: inventories) {
    		for(int i=0; i<ids.length; i++) {
        		InventoryMetaItem item = inventory.getItem(ids[i]);
        		if(item != null) ret[i] = item;
        	}
    	}
    	
    	// Return list of items
    	return ret;
    }
    
    public static WorldItem getWorldItem(PickableUnit item) {
    	// Find corresponding world item
    	WorldItem ret = null;
    	for(WorldItem tItem: worldItems) {
    		if(tItem.equals(item)) {
    			ret = tItem;
    			break;
    		}
    	}
    	
    	// If corresponding world item not found, create new one
    	if(ret == null) { 
    		ret = new WorldItem(item);
    		worldItems.add(ret);
    	}
    	
    	return ret;
    }
}
