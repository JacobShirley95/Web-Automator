package org.auriferous.bot.scripts;

import javax.swing.JMenu;

import org.auriferous.bot.gui.swing.script.JGuiListener;
import org.auriferous.bot.gui.tabs.TabView;
import org.auriferous.bot.script.Script;
import org.auriferous.bot.script.ScriptContext;
import org.auriferous.bot.script.ScriptMethods;
import org.auriferous.bot.script.library.ScriptManifest;
import org.auriferous.bot.tabs.Tab;

import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;

public class Googler extends Script implements JGuiListener{
	private ScriptMethods methods;

	public Googler(ScriptManifest manifest, ScriptContext context) {
		super(manifest, context);
	}

	@Override
	public void onStart() {
		final Tab googleTab = openTab("www.google.co.uk");
		
		googleTab.getBrowserWindow().addLoadListener(new LoadAdapter() {
			@Override
			public void onFinishLoadingFrame(FinishLoadingEvent event) {
				methods = new ScriptMethods(googleTab);
				methods.type("I like to Google stuff.");
				status = STATE_EXIT_SUCCESS;
			}
		});
	}

	@Override
	public void onPause() {
	}

	@Override
	public void onTerminate() {
	}

	@Override
	public void onJMenuCreated(JMenu menu) {
	}

	@Override
	public void onTabViewCreated(Tab tab, TabView tabView) {
		// TODO Auto-generated method stub
		
	}
}
