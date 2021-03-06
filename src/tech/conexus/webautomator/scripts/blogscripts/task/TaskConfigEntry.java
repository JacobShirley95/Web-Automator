package tech.conexus.webautomator.scripts.blogscripts.task;

import java.util.ArrayList;
import java.util.List;

import tech.conexus.webautomator.shared.data.DataEntry;

public class TaskConfigEntry extends DataEntry{
	private Task task;

	public TaskConfigEntry(Task t) {
		super("task");
		
		this.task = t;
	}
	
	@Override
	public List<DataEntry> getChildren() {
		List<DataEntry> children = new ArrayList<DataEntry>();
		
		children.add(new DataEntry("url", task.url));
		children.add(new DataEntry("shuffles", task.shuffles));
		children.add(new DataEntry("interval", task.timeInterval));
		children.add(new DataEntry("time-on-ad", task.timeOnAd));
		children.add(new DataEntry("sub-clicks", task.subClicks));
		children.add(new DataEntry("fb-link", task.fbLink));

		children.add(new DataEntry("ad-clicked", task.adClicked));
		children.add(new DataEntry("status", task.status));
		children.add(new DataEntry("info", task.info));
		
		return children;
	}
}
