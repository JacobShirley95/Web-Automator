package tech.conexus.webautomator.scripts.googler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;

import tech.conexus.webautomator.Utils;
import tech.conexus.webautomator.gui.swing.script.JScriptGui;
import tech.conexus.webautomator.script.Script;
import tech.conexus.webautomator.script.ScriptContext;
import tech.conexus.webautomator.script.ScriptMethods;
import tech.conexus.webautomator.script.dom.ElementBounds;
import tech.conexus.webautomator.shared.data.library.ScriptManifest;
import tech.conexus.webautomator.shared.tabs.Tab;
import tech.conexus.webautomator.shared.tabs.TabAdapter;
import tech.conexus.webautomator.shared.tabs.TabControlAdapter;
import tech.conexus.webautomator.shared.tabs.TabListener;
import tech.conexus.webautomator.shared.tabs.view.PaintListener;

public class Googler extends Script implements JScriptGui, PaintListener{
	private static final String[] SEARCHES = new String[] {"Network Storage and Data Management","netapp, netapp network storage","cloud computing","virtualisation in netapp","storage efficiency","data storage","information management","nas","what does netapp do","Mulesoft","Mulesoft FAQ","connect applications together with mulesoft","what are developer connections","system integration","message routing software","transaction management software","Mule ESB","Mulesoft INC","integration platform","mulesoft.com"};
	
	private static final int STAGE_GOOGLE = 0;
	private static final int STAGE_CLICK_LINK = 1;
	private static final int STAGE_SAVE_URL = 2;
	private static final int STAGE_CLICK_SUB_LINKS = 3;
	private static final int STAGE_RETURN_TO_LINK = 4;
	private static final int STAGE_NEXT_SEARCH = 5;
	
	private static final int MAX_CLICKS = 6;

	private int stage;
	
	private ScriptMethods methods;
	private boolean exec = false;
	
	private long mainFrame = 0;
	
	private List<String> searches = new LinkedList<String>();
	
	private ElementBounds debugElement = null;

	public Googler(ScriptManifest manifest, ScriptContext context) {
		super(manifest, context);
		
		searches.addAll(Arrays.asList(SEARCHES));
	}

	private Tab googleTab = null;
	
	@Override
	public void onStart() {
		googleTab = openTab("www.google.co.uk");
		googleTab.getTabView().addPaintListener(this);
		
		googleTab.getBrowserInstance().addLoadListener(new LoadAdapter() {
			@Override
			public void onFinishLoadingFrame(FinishLoadingEvent event) {
				exec = true;
				mainFrame = event.getFrameId();
			}
		});
		googleTab.addTabListener(new TabAdapter() {
			@Override
			public void onTabClosed() {
				status = Script.STATE_EXIT_FAILURE;
			}
		});
				
		methods = new ScriptMethods(googleTab);
		
		timer = System.currentTimeMillis();
	}
	
	private boolean tickNextSearch() {
		return false;
	}
	
	private boolean tickGoogle() {
		if (!searches.isEmpty()) {
			Utils.wait(2000);
			
			System.out.println("Started typing "+googleTab.getBrowserInstance().getFramesIds());
			int random = (int)Math.floor(searches.size()*Math.random());
			String search = searches.remove(random);
			
			System.out.println("Looking for input element");
			debugElement = methods.getRandomElement("$('*[maxlength=\"2048\"]')");
			methods.clickElement(debugElement);
			//methods.moveMouse(300, 300);
			System.out.println("Found input element");
			
			methods.type(search);
			methods.type(KeyEvent.VK_ENTER);
			
			Utils.wait(2000);
			
			ElementBounds el = methods.getRandomElement(mainFrame, "$('.g').find('.r').find('a')");
			debugElement = el;
			methods.scrollTo((int)el.getCenterY(), 500, 500);
			methods.mouse((int)el.getCenterX(), (int)el.getCenterY());
			
			stage = STAGE_SAVE_URL;
			
			//System.out.println(methods.getElements2(mainFrame, "$('#lst-ib')").length);
			
			return true;
		} else {
			System.out.println("All finished. Exiting...");
			status = STATE_EXIT_SUCCESS;
			
			return true;
		}
	}
	
	private int subClicks = 0;
	private String saveURL = "";
	private ElementBounds[] elements = null;
	
	private int getWaitTime(ElementBounds[] elements) {
		int time = 6;
		if (elements != null) {
			time = (int) (elements.length*0.5);
			if (time > 16)
				time = 16;
			else if (time < 5)
				time = 5;
		}
		return (time+Utils.random(-5, 5))*1000;
	}
	
	private int getWaitTime() {
		return getWaitTime(methods.getElements(methods.LINK_JQUERY));
	}
	
	private boolean tickSaveURL() {
		subClicks = 0;
		saveURL = googleTab.getURL();
		
		elements = methods.getElements(mainFrame, methods.LINK_JQUERY);
		if (elements == null) {
			Utils.wait(5000);
			elements = methods.getElements(mainFrame, methods.LINK_JQUERY);
		}
		int waitTime = getWaitTime(elements);
		System.out.println("Waiting "+(waitTime/1000)+" seconds");
		Utils.wait(waitTime);
		
		stage = STAGE_CLICK_SUB_LINKS;
		
		return false;
	}
	
	private boolean tickSubClicks() {
		if (subClicks < MAX_CLICKS) {
			subClicks++;
			System.out.println("Started clicking links "+subClicks+"/"+MAX_CLICKS);
			
			ElementBounds clickable = (ElementBounds)Utils.getRandomObject(elements);
			
			System.out.println("Got clickable");
			
			if (clickable != null) {
				debugElement = clickable;
				methods.mouse(clickable.getRandomPointFromCentre(0.7, 0.7));
			} else {
				System.out.println("couldn't find link");
			}
			stage = STAGE_RETURN_TO_LINK;
			
			return true;
		} else {
			System.out.println("Going to google");
			
			stage = STAGE_GOOGLE;
			googleTab.loadURL("www.google.co.uk");
			return true;
		}
	}
	
	private boolean tickReturn() {
		int waitTime = getWaitTime();
		System.out.println("Waiting "+(waitTime/1000)+" seconds");
		Utils.wait(waitTime);
		
		methods.moveMouseRandom(false);
		
		System.out.println("Returning to original link");
		googleTab.loadURL(saveURL);
		
		stage = STAGE_CLICK_SUB_LINKS;
		
		return true;
	}
	
	private long timer = 0;
	
	@Override
	public int tick() {
		if (exec) {
			switch (stage) {
				case STAGE_NEXT_SEARCH: if (tickNextSearch()) break;
				case STAGE_GOOGLE: if (tickGoogle()) break;
				case STAGE_SAVE_URL: if (tickSaveURL()) break;
				case STAGE_CLICK_SUB_LINKS: if (tickSubClicks()) break;
				case STAGE_RETURN_TO_LINK: if (tickReturn()) break;
			}
			timer = System.currentTimeMillis();
			exec = false;
		} else {
			if (System.currentTimeMillis()-timer >= 10000) {
				System.out.println("It's been 10 seconds. Forcing execution.");
				exec = true;
			}
		}

		return super.tick();
	}

	@Override
	public void onJMenuCreated(JMenu menu) {
		menu.add(new JMenuItem("Add Search"));
	}

	@Override
	public void onPaint(Graphics g) {
		if (debugElement != null) {
			g.setColor(Color.green);
			g.drawRect(debugElement.x, debugElement.y, debugElement.width, debugElement.height);
		}
	}

	@Override
	public boolean shouldCreateMenu() {
		return true;
	}
}
