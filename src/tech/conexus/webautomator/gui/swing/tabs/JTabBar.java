package tech.conexus.webautomator.gui.swing.tabs;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import tech.conexus.webautomator.gui.swing.JOverlayComponent;
import tech.conexus.webautomator.shared.tabs.Tab;
import tech.conexus.webautomator.shared.tabs.TabControlListener;
import tech.conexus.webautomator.shared.tabs.Tabs;

public class JTabBar extends JTabbedPane implements TabControlListener, ChangeListener {
	private static final long serialVersionUID = 1L;
	private List<Tabs> scriptTabs;
	private JOverlayComponent paintableComponent;

	public JTabBar(JOverlayComponent paintComp, Tabs... scriptTabs) {
		this.paintableComponent = paintComp;
		this.scriptTabs = new ArrayList(Arrays.asList(scriptTabs));
		
		for (Tabs tabs : scriptTabs) {
			addTabs(tabs);
			//tabs.addTabControlListener(this);
		}
		
		addChangeListener(this);
	}
	
	public void addTabs(Tabs tabs) {
		this.scriptTabs.add(tabs);
		
		for (Tab tab : tabs.getTabList()) {
			addTab(new JTab(paintableComponent, this, tab));
		}
		
		tabs.addTabControlListener(this);
	}
	
	public void removeTabs(Tabs tabs) {
		this.scriptTabs.remove(tabs);
		
		for (Tab tab : tabs.getTabList()) {
			remove(getBarIndexByTab(tab));
		}
		
		tabs.removeTabControlListener(this);
	}

	@Override
	public void onTabAdded(Tab tab) {
		addTab("New Tab", new JTab(paintableComponent, this, tab));
	}

	@Override
	public void onTabClosed(Tab tab) {
		int id = getBarIndexByTab(tab);
		super.remove(id);
	}

	@Override
	public void onTabUpdate(Tab tab) {
		
	}

	@Override
	public void onTabChange(Tab tab) {
		setSelectedIndex(getBarIndexByTab(tab));
	}
	
	public void addTab(JTab tab) {
		addTab(tab.getTabInstance().getTitle(), tab);
	}
	
	public void addTab(String title, JTab tab) {
		int index = this.getTabCount();
		
		super.addTab(title, tab);
		
		setTabComponentAt(index, tab.getTabComponent());
		setSelectedIndex(index);
	}
	
	@Override
	public void remove(int index) {
		Component comp = getComponentAt(index);
		if (comp instanceof JTab) {
			Tab tab = ((JTab)comp).getTabInstance();
			for (Tabs tabs : scriptTabs) {
				if (tabs.containsTab(tab)) {
					tabs.closeTab(tab);
					break;
				}
			}
		}
	}
	
	public int getBarIndexByTab(Tab tab) {
		for (int i = 0; i < getTabCount(); i++) {
			Component comp = getComponentAt(i);
			if (comp instanceof JTab) {
				Tab tab2 = ((JTab)comp).getTabInstance();
				if (tab2.equals(tab))
					return i;
			}
		}
		return -1;
	}
	
	public Tab getTabByBarIndex(int index) {
		if (getTabCount() > 0) {
			Component comp = getComponentAt(index);
			if (comp instanceof JTab) {
				Tab tab = ((JTab)comp).getTabInstance();
				return tab;
			}
		}
		return null;
	}
	
	public Tab getCurrentTab() {
		return getTabByBarIndex(getSelectedIndex());
	}
	
	@Override
	public void removeAll() {
		super.removeAll();
		
		for (Tabs tabs : scriptTabs) {
			tabs.closeAll();
		}
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		int index = getSelectedIndex();
		
		if (index >= 0) {
			Tab tab = getTabByBarIndex(index);
			for (Tabs tabs : scriptTabs) {
				if (tabs.containsTab(tab)) {
					tabs.setCurrentTab(tab);
				}
			}
		} else {
			for (Tabs tabs : scriptTabs) {
				tabs.setCurrentTab(-1);
			}
		}
	}
}