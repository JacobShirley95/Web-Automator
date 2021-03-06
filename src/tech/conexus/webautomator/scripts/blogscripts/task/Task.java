package tech.conexus.webautomator.scripts.blogscripts.task;

import tech.conexus.webautomator.shared.data.DataEntry;

public class Task {
	public static final int STATUS_NO_STATUS = 0;
	public static final int STATUS_COMPLETE = 1;
	public static final int STATUS_RUNNING = 2;
	public static final int STATUS_FAILED = 3;
	
	public String url;
	public int timeOnAd;	
	public int shuffles;
	public int timeInterval;
	public int subClicks;
	public String fbLink;
	
	public String adClicked;
	public int status;
	public String info;
	
	public Task(DataEntry entry) {
		load(entry);
	}
	
	public Task(String url, int shuffles, int timeInterval, int timeOnAd, int subClicks, String fbLink) {
		this(url, shuffles, timeInterval, timeOnAd, subClicks, fbLink, "", STATUS_NO_STATUS, "");
	}
	
	public Task(String url, int shuffles, int timeInterval, int timeOnAd, int subClicks, String fbLink, String adClicked, int status, String reason) {
		this.url = url;
		this.shuffles = shuffles;
		this.timeInterval = timeInterval;
		this.timeOnAd = timeOnAd;
		this.subClicks = subClicks;
		this.fbLink = fbLink;
		
		this.adClicked = adClicked;
		this.status = status;
		this.info = reason;
	}
	
	public void load(DataEntry config) {
		//System.out.println(config.getValue("shuffles", ""));
		
		this.url = ""+config.getValue("url", "");
		this.shuffles = Integer.parseInt(""+config.getValue("shuffles", "0"));
		this.timeInterval = Integer.parseInt(""+config.getValue("interval", "0"));
		this.timeOnAd = Integer.parseInt(""+config.getValue("time-on-ad", "0"));
		this.subClicks = Integer.parseInt(""+config.getValue("sub-clicks", "0"));
		this.fbLink = ""+config.getValue("fb-link", "");
		
		this.adClicked = ""+config.getValue("ad-clicked", "");
		this.status = Integer.parseInt(""+config.getValue("status", ""+STATUS_NO_STATUS));
		this.info = ""+config.getValue("info", "");
	}
	
	public Task copy() {
		return new Task(url, shuffles, timeInterval, timeOnAd, subClicks, fbLink, adClicked, status, info);
	}
}
