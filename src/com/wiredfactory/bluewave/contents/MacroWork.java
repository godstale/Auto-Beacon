package com.wiredfactory.bluewave.contents;

public class MacroWork {
	// Management
	public int workType;	// What to do
	public int duration;	// how long (time in milli-second)
	public int interval;	// repeat after n-sec (time in milli-second)
	public int count;		// how many times
	public long lastExecTime;	// last executed time
	public boolean isFirst = true;
	
	// Information
	public String title;	// Title of this macro
	public String destination;	// Email/Message/SMS - recipient
	public String message;		// Email/Message/SMS - message
	
	
	public MacroWork(int work_type) {
		workType = work_type;
		isFirst = true;
	}
	
	public MacroWork(int _work_type, int _duration, int _interval, int _count) {
		workType = _work_type;
		duration = _duration;
		interval = _interval;
		count = _count;
		isFirst = true;
	}
}
