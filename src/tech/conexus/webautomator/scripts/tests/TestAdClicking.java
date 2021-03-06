package tech.conexus.webautomator.scripts.tests;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.events.FailLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.FrameLoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.LoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadListener;
import com.teamdev.jxbrowser.chromium.events.ProvisionalLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.StartLoadingEvent;

import tech.conexus.webautomator.Utils;
import tech.conexus.webautomator.script.Script;
import tech.conexus.webautomator.script.ScriptContext;
import tech.conexus.webautomator.script.ScriptMethods;
import tech.conexus.webautomator.script.ScriptMethods.ClickType;
import tech.conexus.webautomator.script.dom.ElementBounds;
import tech.conexus.webautomator.shared.data.library.ScriptManifest;
import tech.conexus.webautomator.shared.tabs.Tab;
import tech.conexus.webautomator.shared.tabs.TabControlAdapter;
import tech.conexus.webautomator.shared.tabs.view.PaintListener;

public class TestAdClicking extends Script implements PaintListener, LoadListener{
	private ScriptMethods methods;
	private Browser browser;
	private int successCode = -1;
	private Tab currentTab;
	private int status = STATE_RUNNING;
	
	private boolean startExec = false;
	
	public TestAdClicking(ScriptManifest manifest, ScriptContext context) {
		super(manifest, context);
		//this.browser.loadURL("naht.tk");
		//this.browser.loadURL("http://ceehu.tk/random");
		//this.browser.loadURL("https://business.twitter.com/help/how-twitter-ads-work");
		//this.browser.loadURL("http://stackoverflow.com/questions/596467/how-do-i-convert-a-float-number-to-a-whole-number-in-javascript");
		//this.browser.loadURL("http://www.holidayautos.co.uk/?_$ja=cid:1510255|cgid:119904326|tsid:70217|crid:63895368&clientID=581725");
		//this.browser.loadURL("https://www.google.com/intx/en_uk/work/apps/business/products/gmail/index.html?utm_source=gdn&utm_medium=display&utm_campaign=emea-gb-en-gmail-rmkt-all-trial-120077397&utm_content=puppyscrubber");
		
		//openTab("naht.tk/random");//
		
	}

	@Override
	public int tick() {
		if (startExec) {
			System.out.println("Finished loading main frame");
			
			//methods.getRandomLink(false);//
			/*ElementBounds rects = methods.getRandomClickable(false);
			
			System.out.println("finished");
			
			//methods.moveMouse(300, 300);
			if (rects != null) {
				System.out.println("Found");
				Point p = rects.getRandomPointFromCentre(0.5, 0.5);
				
				r = rects;//iframe;
				
				
				//break;
				methods.mouse(p, ClickType.NO_CLICK);
				//methods.mouse(p, ClickType.LCLICK);
				//methods.type("hi Gerry");
				
				return STATE_EXIT_SUCCESS;
			}
			/*r = methods.getElements("$('iframe');")[1];
			
			Point p = r.getRandomPointInRect();
			
			methods.mouse(p, ClickType.NO_CLICK);*/
			
			//System.out.println("offset "+methods.getPageYOffset()+", height "+methods.getPageHeight());
			//System.out.println("Waiting");
			Utils.wait(3000);
			
			methods.scrollMouse(false, 3);
			System.out.println(methods.getPageYOffset());
			
			//System.out.println("On Facebook page!!!!");
			
			
			
			/*ScriptMethods fbMethods = new ScriptMethods(currentTab);
			
			ElementBounds fbFoto = fbMethods.getRandomElement("$('.UFICommentPhotoIcon')");// fbMethods.getRandomElement("$('.UFIReplyActorPhotoWrapper');");
			
			if (fbFoto != null) {
				Point p = fbFoto.getRandomPointFromCentre(0.5, 0.5);
				
				r = fbFoto;
				
				System.out.println("Found Facebook photo "+p.y);
				
				fbMethods.scrollTo(p.y, 40, 20);
				
				p.x -= 150;
				
				fbMethods.mouse(p, ClickType.LCLICK);
				Utils.wait(500);
				fbMethods.mouse(p, ClickType.LCLICK);
				Utils.wait(500);
				System.out.println("Writing signature of Testing");
				
				fbMethods.type("Testing");
			}//*/
			//startExec = false;
		}
		
		return super.tick();
	}
	
	@Override
	public void onDocumentLoadedInMainFrame(LoadEvent event) {
	}
	
	ElementBounds r2 = null;
	private long mainFrame;
	@Override
	public void onDocumentLoadedInFrame(FrameLoadEvent event) {
		
	}
	
	private ElementBounds findAds(String... jqueryStrings) {
		ElementBounds[] adsbygoogle = methods.getElements("$('.adsbygoogle')");
		
		if (adsbygoogle != null) {
			System.out.println("Found basic ad");
			
			ElementBounds bounds = adsbygoogle[0];
			ElementBounds[] iframe1 = methods.getElements("$('#google_ads_frame1')");
			if (iframe1 != null) {
				bounds.add(iframe1[0]);
				
				ElementBounds[] result = null;
				for (String s : jqueryStrings) {
					System.out.println("Trying "+s);
					result = methods.getElements(s);
					if (result != null) {
						System.out.println("Found "+s);
						
						bounds.add(result[0]);
						bounds.width = result[0].width;
						bounds.height = result[0].height;
						break;
					}
				}
			}
			return bounds;
		}
		
		return null;
	}
	
	@Override
	public void onFinishLoadingFrame(FinishLoadingEvent event) {
		//super.onFinishLoadingFrame(event);
		
		long frame = event.getFrameId();
		
		if (event.isMainFrame()) {
			
			System.out.println("loaded");
			mainFrame = event.getFrameId();
			startExec = true;
		}
	}

	private ElementBounds r = null;
	
	@Override
	public void onPaint(Graphics g) {
		if (r != null) {
			g.setColor(Color.green);
			g.drawRect((int)(r.x-methods.getPageXOffset()), (int)(r.y-methods.getPageYOffset()), r.width, r.height);
		}
	}

	@Override
	public void onStart() {
		System.out.println("Starting");
		//openTab("naht.tk/random");//
		// openTab("http://www.bomgar.com/sem/pam?wm_lpID=121979522&wm_ctID=377&wm_kwID=74414766&wm_mtID=145&wm_kw=728x90%5FPAM%5FLearnMore%5FPAM+RT%5FGoogle+Display+Network&utm_source=google+display+network&utm_medium=display&utm_term=728x90%5Fpam%5Flearnmore%5Fpam+rt%5Fgoogle+display+network&utm_campaign=bomgar+retargeting+uk+pam&wm_sd=1&wm_clid=6357823082402855862125610816-635782308240285586&wm_v=google+display+network&wm_camp=bomgar+retargeting+uk+pam&wm_ag=banners%5Fgoogle+display+network%5F728x90%5Fpam+rt");
		currentTab = openTab("https://www.facebook.com/groups/381168852071038/permalink/466387946882461/");//openTab("https://m.audibene.com/hearing-aids-consultation-siemens/?utm_source=google&utm_medium=cpc&utm_campaign=UK_GDN_INT&gclid=CMKUuITtnscCFWoJwwodyh0KBw");//openTab("http://ceehu.tk/random");// openTab("http://trippins.tk/random");//openTab("http://ceehu.tk/random");//openTab("http://www.w3schools.com/html/tryit.asp?filename=tryhtml_input");
		
		//currentTab.getTabView().addTabPaintListener(this);
		getTabs().addTabControlListener(new TabControlAdapter() {
			@Override
			public void onTabClosed(Tab tab) {
				super.onTabClosed(tab);
				
				if (tab.equals(currentTab)) {
					status = STATE_EXIT_SUCCESS;
				}
			}
		});
		
		methods = new ScriptMethods(currentTab);
		browser = currentTab.getBrowserInstance();
		browser.addLoadListener(this);
		
		currentTab.getTabView().addPaintListener(this);
	}

	@Override
	public void onFailLoadingFrame(FailLoadingEvent event) {
	}

	@Override
	public void onProvisionalLoadingFrame(ProvisionalLoadingEvent event) {
	}

	@Override
	public void onStartLoadingFrame(StartLoadingEvent event) {
	}
}
