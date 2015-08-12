package org.auriferous.bot.tabs;

public interface TabControlListener {
	public void onTabAdded(Tab tab);
	public void onTabClosed(Tab tab);
	public void onTabUpdate(Tab tab);
	public void onTabChange(Tab tab);
}
