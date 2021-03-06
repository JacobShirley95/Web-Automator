package tech.conexus.webautomator.script;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.JSObject;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.events.StatusEvent;
import com.teamdev.jxbrowser.chromium.events.StatusListener;

import tech.conexus.webautomator.ResourceLoaderStatic;
import tech.conexus.webautomator.Utils;
import tech.conexus.webautomator.script.callbacks.JSCallback;
import tech.conexus.webautomator.script.dom.ElementBounds;
import tech.conexus.webautomator.script.input.Keyboard;
import tech.conexus.webautomator.script.input.Mouse;
import tech.conexus.webautomator.shared.tabs.Tab;
import tech.conexus.webautomator.shared.tabs.TabCallback;
import tech.conexus.webautomator.shared.tabs.view.TabView;

public class ScriptMethods {
	public static final int DEFAULT_MOUSE_SPEED = 25;
	public static final String SHIFT_KEYS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ�!\"�$%^&*()_+{}:@~<>?|�";
	public static final int DEFAULT_KEY_TIME = 100;
	
	public static final String LINK_JQUERY = "$(document).findVisibles('a[href^=\"http\"], a[href^=\"/\"], a[href^=\"clkn\"], a[href^=\"clkg\"]');";
	
	public enum ClickType {
		LCLICK, RCLICK, NO_CLICK
	}
	
	private String status = "";
	private int mouseSpeed = DEFAULT_MOUSE_SPEED;

	private Mouse mouse;
	private Keyboard keyboard;
	
	protected Tab target;

	public ScriptMethods(Tab target) {
		this.target = target;

		this.mouse = new Mouse(this.target);
		this.keyboard = new Keyboard(this.target);
		
		this.target.getTabView().addPaintListener(this.mouse);
		
		this.target.getBrowserInstance().addStatusListener(new StatusListener() {
			@Override
			public void onStatusChange(StatusEvent event) {
				status = event.getText();
			}
		});
	}
	
	public Tab getTarget() {
		return target;
	}

	public void injectJQuery(long frameID) {
		try {
			if (!isJQueryInjected(frameID)) {
				target.getBrowserInstance().executeJavaScript(frameID, ResourceLoaderStatic.loadResourceAsString("resources/js/jquery.min.js", true));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void injectCode(long frameID) {
		try {
			if (!isCodeInjected(frameID)) {
				target.getBrowserInstance().executeJavaScript(frameID,  ResourceLoaderStatic.loadResourceAsString("resources/js/inject.js", true));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void execJS(String code) {
		Browser browser = target.getBrowserInstance();
		for (long id : browser.getFramesIds()) {
			injectJQuery(id);
			injectCode(id);
			try {
				browser.executeJavaScriptAndReturnValue(id, code);
			} catch (Exception e) {}
		}
	}
	
	public void execJS(String code, JSCallback callback) {
		Browser browser = target.getBrowserInstance();
		for (long id : browser.getFramesIds()) {
			injectJQuery(id);
			injectCode(id);
			try {
				JSValue val = browser.executeJavaScriptAndReturnValue(id, code);
				if (callback.onResult(val))
					break;
			} catch (Exception e) {}
		}
	}
	
	private boolean isJQueryInjected(long frameID) {
		return target.getBrowserInstance().executeJavaScriptAndReturnValue(frameID, "window.jQuery != undefined").asBoolean().getBooleanValue();
	}
	
	private boolean isCodeInjected(long frameID) {
		return target.getBrowserInstance().executeJavaScriptAndReturnValue(frameID, "window.injectionLoaded != undefined").asBoolean().getBooleanValue();
	}
	
	public String getStatus() {
		return status;
	}
	
	public ElementBounds[] getElements(String jqueryString) {
		List<ElementBounds> elems = new ArrayList<ElementBounds>();
		
		List<Long> ids = target.getBrowserInstance().getFramesIds();
		
		if (ids.size() == 0) {
			ElementBounds[] elems2 = getElements(-1, jqueryString);
			elems.addAll(Arrays.asList(elems2));
		} else {
			for (Long frame : ids) {
				ElementBounds[] elems2 = getElements(frame, jqueryString);
				elems.addAll(Arrays.asList(elems2));
			}
		}
		
		ElementBounds[] result = new ElementBounds[elems.size()];
		elems.toArray(result);
		
		return result;
	}
	
	/*public ElementBounds[] getElements(String jqueryString) {
		ElementBounds testEl = null;
		ElementBounds[] foundEls = null;
		
		for (Long frame : target.getBrowserInstance().getFramesIds()) {
			ElementBounds[] elems = getElements(frame, jqueryString);
			if (elems.length > 0) {
				if (testEl == null) {
					foundEls = elems;
					testEl = elems[0];
				} else {
					if (elems[0].x > testEl.x && elems[0].y > testEl.y) {
						foundEls = elems;
						testEl = elems[0];
					}
				}
			}
		}
		
		return foundEls;
	}*/
	
	public ElementBounds[] getElementsInIFrames(String jqueryString) {
		String mainHref = target.getBrowserInstance().executeJavaScriptAndReturnValue("window.location.href;").asString().getStringValue();
		String href = "";
		
		ElementBounds testEl = null;
		ElementBounds[] foundEls = null;
		
		for (Long frame : target.getBrowserInstance().getFramesIds()) {
			ElementBounds[] elems = getElements(frame, jqueryString);
			if (elems.length > 0) {
				//System.out.println("Testing "+jqueryString);
				//System.out.println(target.getBrowserInstance().getHTML(frame));
				if (testEl == null) {
					href = target.getBrowserInstance().executeJavaScriptAndReturnValue(frame, "window.location.href;").asString().getStringValue();

					foundEls = elems;
					testEl = elems[0];
				} else {
					if (elems[0].x > testEl.x && elems[0].y > testEl.y) {
						//System.out.println("Getting highest offset");
						href = target.getBrowserInstance().executeJavaScriptAndReturnValue(frame, "window.location.href;").asString().getStringValue();
						
						foundEls = elems;
						testEl = elems[0];
					}
				}
			}
		}
		
		if (foundEls != null) {
			if (href.equals(mainHref)) {
				return foundEls;
			} else {
				//System.out.println("Searching iframes");
				for (Long frame : target.getBrowserInstance().getFramesIds()) {
					ElementBounds[] iframes = getElements(frame, "$(\"iframe[src='"+href+"']\");");
					
					if (iframes.length > 0) {
						ElementBounds iframe = iframes[0];
						
						//System.out.println("Found iframe "+iframes.length + " with src "+href);
						
						for (ElementBounds rect : foundEls) {
							rect.setIframe(iframe);
							rect.x += iframe.x;
							rect.y += iframe.y;
						}
						
						return foundEls;
					} else {
						//System.out.println("not found");
					}
				}
			}
		}

		return null;
	}
	
	public ElementBounds[] getElements(long frameID, String jqueryString) {
		final List<ElementBounds> rects = new ArrayList<ElementBounds>();
		injectJQuery(frameID);
    	injectCode(frameID);

    	try {
    		if (jqueryString.endsWith(";"))
    			jqueryString = jqueryString.substring(0, jqueryString.length()-1);
    			
    		String search = jqueryString;

    		TabCallback c = new TabCallback() {
    			@Override
    			public Object onInvoke(Object... args) {
    				double x = Double.parseDouble(args[1].toString());
    				double y = Double.parseDouble(args[2].toString());
    				double width = Double.parseDouble(args[3].toString());
    				double height = Double.parseDouble(args[4].toString());
    				
    				JSObject object = (JSObject)args[5];
    				
    				rects.add(new ElementBounds(object, (int)x, (int)y, (int)width, (int)height));

    				return null;
    			}
    		};
    		
	    	target.pushCallback(c);

	    	//System.out.println("Executing javascript");
	    	//System.out.println("getting stuff "+jqueryString+" frame id "+frameID);
	    	if (frameID == -1) {
	    		target.getBrowserInstance().executeJavaScriptAndReturnValue("sendBackResults("+search+")");
	    	} else {
	    		target.getBrowserInstance().executeJavaScriptAndReturnValue(frameID, "sendBackResults("+search+")");
	    	}
			//System.out.println("finished "+rects.size());
			//System.out.println("Finished executing javascript");
			
			target.popCallback();
    	} catch (Exception e) {
    		//e.printStackTrace();
    	}
    	
		ElementBounds[] results = new ElementBounds[rects.size()];
		rects.toArray(results);
		return results;
	}
	
	public ElementBounds[] getElements2(long frameID, String jqueryString) {
		final List<ElementBounds> rects = new ArrayList<ElementBounds>();
	

		injectJQuery(frameID);
    	injectCode(frameID);
    	
    	Browser browser = target.getBrowserInstance();

    	try {
    		System.out.println(frameID+": "+jqueryString);
			/*browser.executeJavaScript(frameID, "elems = "+ jqueryString + (jqueryString.endsWith(";") ? "" : ";"));
			
			int len = (int) browser.executeJavaScriptAndReturnValue(frameID, "elems.length;").getNumberValue();
			
			for (int i = 0; i < len; i++) {
				browser.executeJavaScript(frameID, "el = elems.eq("+i+"); off = el.offset2();");
				
				int x = (int) browser.executeJavaScriptAndReturnValue(
						frameID, "off.left;").getNumberValue();
				int y = (int) browser.executeJavaScriptAndReturnValue(frameID, "off.top;").getNumberValue();
				
				int width = (int) browser.executeJavaScriptAndReturnValue(frameID, "getElementWidth(el);").getNumberValue();
				int height = (int) browser.executeJavaScriptAndReturnValue(frameID, "getElementHeight(el);").getNumberValue();
				
				rects.add(new ElementBounds(null, x, y, width, height));
			}
			*/
    		//browser.exec
    		
    		System.out.println("starting");
    		JSValue val = browser.executeJavaScriptAndReturnValue(jqueryString);
    		System.out.println("FInishing");
    		System.out.println(val.asArray().get(0));
			
			System.out.println("finished");
    	} catch (Exception e) {
    		//e.printStackTrace();
    	}

		ElementBounds[] results = new ElementBounds[rects.size()];
		rects.toArray(results);
		return results;
	}
	
	public double getPageXOffset() {
		return target.getBrowserInstance().executeJavaScriptAndReturnValue("window.pageXOffset").getNumberValue();
	}
	
	public double getPageYOffset() {
		return target.getBrowserInstance().executeJavaScriptAndReturnValue("window.pageYOffset").getNumberValue();
	}
	
	public double getPageWidth() {
		return target.getBrowserInstance().executeJavaScriptAndReturnValue("$(document).width()").getNumberValue();
	}
	
	public double getPageHeight() {
		return target.getBrowserInstance().executeJavaScriptAndReturnValue("$(document).height()").getNumberValue();
	}
	
	public double getWindowWidth() {
		return target.getBrowserInstance().executeJavaScriptAndReturnValue("$(window).width()").getNumberValue();
	}
	
	public double getWindowHeight() {
		return target.getBrowserInstance().executeJavaScriptAndReturnValue("$(window).height()").getNumberValue();
	}
	
	public ElementBounds getRandomElement(String... selector) {
		List<ElementBounds> elemsList = new ArrayList<ElementBounds>();
		for (String s : selector) {
			ElementBounds[] elems = getElements(s);
	
			if (elems == null)
				continue;
			
			elemsList.addAll(Arrays.asList(elems));
		}
		
		if (elemsList.isEmpty())
			return null;
	
		int random = (int) Math.floor(Math.random()*elemsList.size());
		System.out.println("got random "+random + " out of "+elemsList.size());
		return elemsList.get(random);
	}
	
	public ElementBounds getRandomElement(long frameID, String... selector) {
		List<ElementBounds> elemsList = new ArrayList<ElementBounds>();
		
		for (String s : selector) {
			ElementBounds[] elems = getElements(frameID, s);
	
			if (elems == null)
				continue;
			
			elemsList.addAll(Arrays.asList(elems));
		}
		
		if (elemsList.isEmpty())
			return null;
	
		int random = (int) Math.floor(Math.random()*elemsList.size());
		System.out.println("got random "+random + " out of "+elemsList.size());
		return elemsList.get(random);
	}
	
	public ElementBounds getRandomTextField(long frameID) {
		return getRandomElement(frameID, "$(\"input[type='text']\")");
	}
	
	public ElementBounds getRandomClickable(boolean includeButtons) {
		ElementBounds el = null;
		if (includeButtons)
			el = getRandomElement(LINK_JQUERY, "$(document).findVisibles(\"button, input[type='button'], input[type='submit']\");", "getJSClickables();");
		else {
			el = getRandomElement(LINK_JQUERY, "getJSClickables();");
		}
		return el;
	}

	public ElementBounds getRandomClickable(long frameID, boolean includeButtons) {
		ElementBounds el = null;
		if (includeButtons)
			el = getRandomElement(frameID, LINK_JQUERY, "$(document).findVisibles(\"button, input[type='button'], input[type='submit']\");", "getJSClickables();");
		else {
			el = getRandomElement(frameID, LINK_JQUERY, "getJSClickables();");
		}
		return el;
	}

	public void clickElement(ElementBounds element) {
		Point p = element.getRandomPoint();
		
		//p.x -= getPageXOffset();
		//p.y -= getPageYOffset();
		
		mouse(p);
	}

	public void hoverElement(ElementBounds element) {
		Point p = element.getRandomPoint();
		
		p.x -= getPageXOffset();
		p.y -= getPageYOffset();
		
		moveMouse(p);
	}
	
	public void setMouseSpeed(int mouseSpeed) {
		this.mouseSpeed = mouseSpeed;
	}
	
	public int getMouseSpeed() {
		return mouseSpeed;
	}
	
	public void scrollTo(int y, int timeWait, int randomTime) {
		double lastOffset = getPageYOffset();
		double curOffset = getPageYOffset();
		while (y - curOffset > target.getTabView().getHeight()) {
			Utils.wait(timeWait+Utils.random(randomTime));
			scrollMouse(false, 3);
			
			curOffset = getPageYOffset();
			if (lastOffset == curOffset)
				break;
		}
		
		lastOffset = getPageYOffset();
		curOffset = getPageYOffset();
		while (y - curOffset < 0) {
			Utils.wait(timeWait+Utils.random(randomTime));
			scrollMouse(true, 3);
			
			curOffset = getPageYOffset();
			if (lastOffset == curOffset)
				break;
		}
	}
	
	public void scrollTo(int y) {
		scrollTo(y, 100, 20);
	}
	
	public void scrollToRandom(boolean minOffPage) {
		int height = target.getTabView().getHeight();
		int pageHeight = (int) getPageHeight();
		if (pageHeight > height) {
			int y = (int)Math.round(Utils.random(height, pageHeight));
			
			scrollTo(y);
		}
	}
	
	public void moveMouseRandom(boolean onScreen) {
		int randomX = 0;
		int randomY = 0;
		if (onScreen) {
			TabView view = target.getTabView();
			
			randomX = (int)Math.round(Math.random()*view.getWidth());
			randomY = (int)Math.round(Math.random()*view.getHeight());
		} else {
			randomX = (int)Math.round(Math.random()*getWindowWidth());
			randomY = (int)Math.round(Math.random()*getPageHeight());
		}

		mouse(randomX, randomY, ClickType.NO_CLICK);
	}
	
	public void mouse(int x, int y, ClickType clickType) {
		scrollTo(y);
		
		//if (y > getWindowHeight()) {
			x -= getPageXOffset();
			y -= getPageYOffset();
		//}
		
		if (x <= target.getTabView().getWidth()) {
			double x1 = mouse.getMouseX();
			double y1 = mouse.getMouseY();
			
			double randSpeed = ((Math.random() * mouseSpeed) / 2.0 + mouseSpeed) / 10.0;
			
			humanWindMouse(x1, y1, x, y, 7, 5, 10.0 / randSpeed, 15.0 / randSpeed, 10.0 * randSpeed);
			
			Utils.wait(200);
			
			if (clickType == ClickType.LCLICK) {
				mouse.clickMouse(x, y, MouseEvent.BUTTON1);
			} else if (clickType == ClickType.RCLICK) {
				mouse.clickMouse(x, y, MouseEvent.BUTTON2);
			}
		}
	}
	
	public void mouse(Point p, ClickType clickType) {
		mouse(p.x, p.y, clickType);
	}
	
	public void mouse(int x, int y) {
		mouse(x, y, ClickType.LCLICK);
	}
	
	public void moveMouse(int x, int y) {
		mouse(x, y, ClickType.NO_CLICK);
	}
	
	public void mouse(Point p) {
		mouse(p, ClickType.LCLICK);
	}
	
	public void moveMouse(Point p) {
		moveMouse(p.x, p.y);
	}
	
	public void scrollMouse(boolean up, int notches) {
		this.mouse.scrollMouse(up, notches);
		Utils.wait(50+Utils.random(20));
	}
	
	private double hypot(double dx, double dy) {
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	private void humanWindMouse(double xs, double ys, double xe, double ye, double gravity, double wind, double minWait,
			double maxWait, double targetArea) {
		double veloX = 0;
		double veloY = 0;
		double windX = 0;
		double windY = 0;
		double veloMag = 0;
		double maxStep = 0;
		double D = 0;
		double randomDist = 0;
		int W = 0;
		
		int MSP = mouseSpeed;
		
		double sqrt2 = Math.sqrt(2);
		double sqrt3 = Math.sqrt(3);
		double sqrt5 = Math.sqrt(5);

		double dx = xe - xs;
		double dy = ye - ys;

		double distance = hypot(dx, dy);
		long t = System.currentTimeMillis() + 10000;
		while (true) {
			if (System.currentTimeMillis() > t)
				break;

			double dist = hypot(xs - xe, ys - ye);
			wind = Math.min(wind, dist);
			if (dist < 1)
				dist = 1;

			D = (Math.round((Math.round(distance) * 0.3)) / 7);
			if (D > 25)
				D = 25;
			if (D < 5)
				D = 5;

			double rCnc = Utils.random(6);
			if (rCnc == 1)
				D = Utils.random(2, 3);

			if (D <= Math.round(dist))
				maxStep = D;
			else
				maxStep = Math.round(dist);

			if (dist >= targetArea) {
				windX = windX / sqrt3 + (Utils.random(Math.round(wind) * 2 + 1) - wind) / sqrt5;
				windY = windY / sqrt3 + (Utils.random(Math.round(wind) * 2 + 1) - wind) / sqrt5;
			} else {
				windX = windX / sqrt2;
				windY = windY / sqrt2;
			}

			veloX = veloX + windX;
			veloY = veloY + windY;
			veloX = veloX + gravity * (xe - xs) / dist;
			veloY = veloY + gravity * (ye - ys) / dist;

			if (hypot(veloX, veloY) > maxStep) {
				randomDist = maxStep / 2.0 + Utils.random(Math.round(maxStep) / 2);
				veloMag = Math.sqrt((veloX * veloX) + (veloY * veloY));
				veloX = (veloX / veloMag) * randomDist;
				veloY = (veloY / veloMag) * randomDist;
			}

			int lastX = (int) Math.round(xs);
			int lastY = (int) Math.round(ys);
			xs = xs + veloX;
			ys = ys + veloY;

			if ((lastX != Math.round(xs)) || (lastY != Math.round(ys)))
				mouse.moveMouse((int) Math.round(xs), (int) Math.round(ys));

			W = Utils.random((Math.round(100/MSP)))*6;
			if (W < 5)
				W = 5;
			W = (int) Math.round(W * 0.9);

			Utils.wait(W);
			if (hypot(xs - xe, ys - ye) < 1)
				break;
		}

		if ((Math.round(xe) != Math.round(xs)) || (Math.round(ye) != Math.round(ys)))
			mouse.moveMouse((int) Math.round(xe), (int) Math.round(ye));
	}
	
	public final void type(String message) {
		boolean shiftDown = false;
		int mods = 0;
		for (char c : message.toCharArray()) {
			boolean requiresShiftKey = SHIFT_KEYS.contains(""+c);
			
			if (!shiftDown) {
				if (requiresShiftKey) {
					shiftDown = true;
					keyboard.pressKey(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK);
					mods |= InputEvent.SHIFT_DOWN_MASK;
				} else
					mods = 0;
			} else if (shiftDown && !requiresShiftKey) {
				shiftDown = false;
				keyboard.releaseKey(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK);
				mods = 0;
			}
			keyboard.typeKey(c, DEFAULT_KEY_TIME, mods);
		}
	}
	
	public void type(int c, int time) {
		keyboard.typeKey(c, time, 0);
	}
	
	public final void type(int c) {
		int mods = 0;
		if (SHIFT_KEYS.contains(""+(char)c)) {
			keyboard.pressKey(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK);
			mods |= InputEvent.SHIFT_DOWN_MASK;
		}
		keyboard.typeKey(c, DEFAULT_KEY_TIME, mods);
	}

	public Browser getBrowserInstance() {
		return target.getBrowserInstance();
	}
}