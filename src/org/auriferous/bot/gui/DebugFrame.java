package org.auriferous.bot.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import org.auriferous.bot.tabs.Tab;
import org.auriferous.bot.tabs.TabControlListener;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

public class DebugFrame extends JFrame implements TabControlListener{
	private Browser debugger;
	private BrowserView view;
	
	public DebugFrame(JFrame parent) {
		super("Page Debugger");
		
		setSize(900, 600);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setLocationRelativeTo(parent);
		
		debugger = new Browser();
		
		view = new BrowserView(debugger);
		add(view);
	}
	
	public void debug(String remoteDebuggingURL) {
		debugger.loadURL(remoteDebuggingURL);
	}
	
	public void debug(Browser browser) {
		debug(browser.getRemoteDebuggingURL());
	}
	
	public void cleanup() {
		debugger.dispose();
	}

	@Override
	public void onTabAdded(Tab tab) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabClosed(Tab tab) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabUpdate(Tab tab) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabChange(Tab tab) {
		debug(tab.getBrowserWindow());
		
		System.out.println("SDFSDF");
	}
}
