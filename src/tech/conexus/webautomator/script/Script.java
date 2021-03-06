package tech.conexus.webautomator.script;

import tech.conexus.webautomator.shared.data.library.ScriptManifest;
import tech.conexus.webautomator.shared.tabs.Tab;
import tech.conexus.webautomator.shared.tabs.Tabs;

public abstract class Script {
	public static final int STATE_RUNNING = 0;
	public static final int STATE_EXIT_SUCCESS = 1;
	public static final int STATE_EXIT_FAILURE = 2;
	
	protected ScriptContext context;
	
	protected int status = STATE_RUNNING;
	
	private Tabs tabs;
	private ScriptManifest manifest;
	
	public Script(ScriptManifest manifest, ScriptContext context) {
		this.context = context;
		this.manifest = manifest;
		this.tabs = new Tabs(context.getHistory());
	}
	
	public ScriptManifest getManifest() {
		return manifest;
	}
	
	public Tabs getTabs() {
		return tabs;
	}
	
	public int tick() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void onStart() {}
	public void onPause() {}
	public void onResume() {}
	public void onTerminate() {}
	public void onFinished() {}
	
	public final Tab openTab() {
		return tabs.openTab();
	}
	
	public final Tab openTab(String url) {
		return tabs.openTab(url);
	}
	
	public final void closeTab(Tab tab) {
		tabs.closeTab(tab);
	}
}
