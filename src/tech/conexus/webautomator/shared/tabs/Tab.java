package tech.conexus.webautomator.shared.tabs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.teamdev.jxbrowser.chromium.BeforeRedirectParams;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserContext;
import com.teamdev.jxbrowser.chromium.BrowserContextParams;
import com.teamdev.jxbrowser.chromium.BrowserType;
import com.teamdev.jxbrowser.chromium.JSFunctionCallback;
import com.teamdev.jxbrowser.chromium.JSObject;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.LoadHTMLParams;
import com.teamdev.jxbrowser.chromium.NavigationEntry;
import com.teamdev.jxbrowser.chromium.PopupContainer;
import com.teamdev.jxbrowser.chromium.PopupHandler;
import com.teamdev.jxbrowser.chromium.PopupParams;
import com.teamdev.jxbrowser.chromium.RequestCompletedParams;
import com.teamdev.jxbrowser.chromium.SendHeadersParams;
import com.teamdev.jxbrowser.chromium.events.ConsoleListener;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.LoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadListener;
import com.teamdev.jxbrowser.chromium.events.RenderListener;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;
import com.teamdev.jxbrowser.chromium.events.StatusListener;
import com.teamdev.jxbrowser.chromium.events.TitleEvent;
import com.teamdev.jxbrowser.chromium.events.TitleListener;
import com.teamdev.jxbrowser.chromium.javafx.DefaultNetworkDelegate;

import tech.conexus.webautomator.Bot;
import tech.conexus.webautomator.Utils;
import tech.conexus.webautomator.shared.data.history.HistoryConfig;
import tech.conexus.webautomator.shared.data.history.HistoryEntry;
import tech.conexus.webautomator.shared.tabs.view.TabView;

public class Tab {
	private static final boolean TEST_MODE = false;
	private static final List<Browser> BROWSER_INSTANCES = new ArrayList<Browser>();

	private int id;
	private String originalURL;
	
	private Browser browser;
	
	private List<TabListener> tabListeners = new LinkedList<TabListener>();
	private Stack<TabCallback> tabCallbacks = new Stack<TabCallback>();

	private TabView tabView;
	private Tabs parent;
	
	private HistoryConfig history;
	
	private boolean blockJSMessages = false;
	
	private HistoryEntry lastHistoryEntry = null;
	
	private int lastNavIndex = -1;
	
	private boolean shouldAddToHistory = false;
	
	public Tab(final Tabs parent, String url, final HistoryConfig history) {
		this.id = -1;
		this.parent = parent;
		this.history = history;
		this.originalURL = url;
		
		setBlockJSMessages(true);
		
		BrowserContextParams params = null;
		if (TEST_MODE) {
			params = new BrowserContextParams("test");
		} else {
			params = new BrowserContextParams(Bot.JXBROWSER_CACHE_DIRECTORY);
		}
		
		this.browser = new Browser(BrowserType.LIGHTWEIGHT, new BrowserContext(params));
		this.browser.getPreferences().setLocalStorageEnabled(true);
		
		BROWSER_INSTANCES.add(this.browser);
		
		this.browser.addScriptContextListener(new ScriptContextAdapter() {
		    @Override
		    public void onScriptContextCreated(ScriptContextEvent event) {
		        Browser browser = event.getBrowser();
		        JSValue window = browser.executeJavaScriptAndReturnValue("window");
		        window.asObject().setProperty("println", new JSFunctionCallback() {
					@Override
					public Object invoke(Object... args) {
						if (!blockJSMessages)
							System.out.println("JAVASCRIPT: "+args[0].toString());
						return null;
					}
				});
		        
		        window.asObject().setProperty("tabCallback", new JSFunctionCallback() {
					@Override
					public Object invoke(Object... args) {
						TabCallback callback = tabCallbacks.get(0);
				    	
				    	if (callback != null) {
					    	Object[] oArgs = new Object[args.length];
					    	
					    	/*int c = 0;
					        for (JSValue arg : args) {
					            if (arg.isBoolean())
					            	oArgs[c] = arg.getBoolean();
					            else if (arg.isNumber())
					            	oArgs[c] = arg.getNumber();
					            else if (arg.isString())
					            	oArgs[c] = arg.getString();
					            else if (arg.isNull() || arg.isUndefined())
					            	oArgs[c] = null;
					            else 
					            	oArgs[c] = arg;
					            c++;
					        }*/
					        Object returned = callback.onInvoke(args);
					       
					        /*if (returned instanceof Number)
					        	return JSValue.create(Double.parseDouble(returned.toString()));
					        else if (returned instanceof Boolean)
					        	return JSValue.create((Boolean)returned);
					        else if (returned instanceof String)
					        	return JSValue.create((String)returned);
					        
					        return JSValue.createNull();*/
				    	}

						return null;
					}
				});
		    }
		});
		
		this.browser.setPopupHandler(new PopupHandler() {
		    @Override
			public PopupContainer handlePopup(final PopupParams params) {
		        return new PopupContainer() {
					@Override
					public void insertBrowser(final Browser browser,
							java.awt.Rectangle dimensions) {
						setBrowser(browser);
					}
		        };
		    }
		});
		
		this.browser.addTitleListener(new TitleListener() {
            @Override
			public void onTitleChange(TitleEvent event) {
            	if (history != null) {
	            	if (shouldAddToHistory) {
	            		shouldAddToHistory = false;
	            		final Browser b = event.getBrowser();
	    				final int index = b.getCurrentNavigationEntryIndex();
	    				
	                	if (index > lastNavIndex) {
	                		(new Thread() {
	                			public void run() {
	                				Utils.wait(500);
	
	                				final NavigationEntry entry = b.getNavigationEntryAtIndex(index);
	            					lastHistoryEntry = new HistoryEntry(entry.getTimestamp(), "DEFAULT", b.getTitle(), b.getURL());
	
	            					history.addEntry(lastHistoryEntry);
	                			};
	                		}).start();
	    				}
	            	}
            	}
            	
            	for (TabListener listener : tabListeners) 
					listener.onTitleChange(event.getTitle());
            }
        });
		
		if (history != null) {
			this.browser.addLoadListener(new LoadAdapter() {
				@Override
				public void onDocumentLoadedInMainFrame(LoadEvent arg0) {
					shouldAddToHistory = true;
				}
			});
		}
		
		loadURL(url);
	}
	
	public void setBlockJSMessages(boolean blockJSMessages) {
		this.blockJSMessages = blockJSMessages;
	}
	
	public void goBack() {
		this.browser.goBack();
	}
	
	public void goForward() {
		this.browser.goForward();
	}
	
	public void stop() {
		this.browser.stop();
	}
	
	public void alert(String message) {
		//browser.executeJavaScript("alert('"+message+"');");
	}
	
	public void pushCallback(TabCallback callback) {
		tabCallbacks.push(callback);
	}
	
	public void popCallback() {
		tabCallbacks.pop();
	}
	
	public void loadURL(String url) {
		if (!this.browser.isDisposed()) {
			this.originalURL = url;
		
			this.browser.loadURL(url);
		
			for (TabListener listener : tabListeners) 
				listener.onTabUpdating();
		}
	}
	
	public void reload() {
		if (!this.browser.isDisposed()) {
			this.browser.reload();
		
			for (TabListener listener : tabListeners) 
				listener.onTabReloaded();
		}
	}
	
	public void close() {
		parent.closeTab(this);
	}
	
	public void handleClose() {
		for (TabListener listener : tabListeners) 
			listener.onTabClosed();
	}
	
	public String getTitle() {
		if (!browser.isDisposed())
			return browser.getTitle();
		
		return "";
	}
	
	public String getURL() {
		if (!browser.isDisposed())
			return browser.getURL();
		return "";
	}
	
	public String getOriginalURL() {
		return originalURL;
	}
	
	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	private void setBrowser(Browser browser) {
		synchronized (this.browser) {
			for (TitleListener l : this.browser.getTitleListeners())
				browser.addTitleListener(l);
			
			for (LoadListener l : this.browser.getLoadListeners())
				browser.addLoadListener(l);
			
			for (StatusListener l : this.browser.getStatusListeners())
				browser.addStatusListener(l);
			
			for (ConsoleListener l : this.browser.getConsoleListeners())
				browser.addConsoleListener(l);
			
			for (RenderListener l : this.browser.getRenderListeners())
				browser.addRenderListener(l);
	
			//for (String s : this.browser.getBrowserFunctionNames())
				//browser.registerFunction(s, this.browser.getBrowserFunction(s));
			
			browser.setPreferences(this.browser.getPreferences());
			browser.setPopupHandler(this.browser.getPopupHandler());
			browser.setLoadHandler(this.browser.getLoadHandler());
			browser.setDialogHandler(this.browser.getDialogHandler());
			browser.setFullScreenHandler(this.browser.getFullScreenHandler());
			browser.setDownloadHandler(this.browser.getDownloadHandler());
			browser.setContextMenuHandler(this.browser.getContextMenuHandler());
			browser.setPrintHandler(this.browser.getPrintHandler());
			browser.setDialogHandler(this.browser.getDialogHandler());
		
			this.browser.stop();
			this.browser = browser;
			
			BROWSER_INSTANCES.add(this.browser);
			
			for (TabListener listener : tabListeners) 
				listener.onTabBrowserChanged(this.browser);
		}
	}
	
	public Browser getBrowserInstance() {
		return browser;
	}
	
	public void setTabView(TabView tabView) {
		this.tabView = tabView;
	}
	
	public TabView getTabView() {
		return tabView;
	}
	
	public void addTabListener(TabListener tabListener) {
		this.tabListeners.add(tabListener);
	}
	
	public void removeTabListener(TabListener tabListener) {
		this.tabListeners.remove(tabListener);
	}
	
	static {
		Thread t = new Thread(new Runnable() {
		    @Override
			public void run() {
		    	for (Browser browser : BROWSER_INSTANCES) {
		    		System.out.println("Disposing "+browser);
		    		if (!browser.isDisposed())
		    			browser.dispose();
		    	}
		    }
		});
		t.setDaemon(true);
		
		Runtime.getRuntime().addShutdownHook(t);
	}
}
