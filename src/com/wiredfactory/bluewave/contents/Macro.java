package com.wiredfactory.bluewave.contents;

public class Macro {
	
	// Constants
	public static final int MACRO_DISTANCE_CLOSE = 1;	// Within a few centimeters
	public static final int MACRO_DISTANCE_NEAR = 2;	// Within a couple of meters
	public static final int MACRO_DISTANCE_FAR = 3;		// Greater than 10 meters away
	public static final int MACRO_DISTANCE_NONE = 0;	// Cannot calculate proximity
	
	
	public static final int MACRO_WORKS_NONE = 0;
	
	public static final int MACRO_WORKS_SEND_EMAIL = 1;		// Messaging group
	public static final int MACRO_WORKS_SEND_SMS = 2;
	public static final int MACRO_WORKS_SEND_MESSAGE = 3;
	
	public static final int MACRO_WORKS_VIBRATION = 4;		// Alarm group
	public static final int MACRO_WORKS_SOUND = 5;
	public static final int MACRO_WORKS_NOTIFICATION = 6;
	
	public static final int MACRO_WORKS_SET_BELL_MODE = 7;	// Change settings group
	public static final int MACRO_WORKS_SET_VIBRATION_MODE = 8;
	public static final int MACRO_WORKS_SET_SILENT_MODE = 9;
	public static final int MACRO_WORKS_TURN_ON_WIFI = 10;
	public static final int MACRO_WORKS_TURN_OFF_WIFI = 11;
	
	public static final int MACRO_WORKS_LAUNCH_BROWSER = 12;		// Launch app group
	
	public static final int MACRO_WORKS_LAST = 12;
	
	
	// Macro management
	public int id = -1;
	public String title;
	public boolean isEnabled = true;	// This macro is enabled or not
	public int count = -1;		// Disabled: How many times do I have to do this macro?
	
	
	// Device, Category
	public int major = -1;		// major group (-1 means no restriction)
	public int minor = -1;		// minor group (-1 means no restriction)
	public String uuid;
	
	// Conditions
	public int distance = -1;
	
	// Works
	public int works = MACRO_WORKS_NONE;
	public int repeats = 0;		// Disabled: 0: infinite, 1~n: do n times 
	
	public String destination;	// email, sms: Target number or email address
	public int duration = 0;	// Disabled: 
	
	// User defined message
	public String userMessage;
	
	
	
	public Macro() {
	}
	
	public void copyFromMacro(Macro macro) {
		this.id = macro.id;
		this.title = new String(macro.title);
		this.isEnabled = macro.isEnabled;
		this.count = macro.count;
		this.major = macro.major;
		this.minor = macro.minor;
		this.uuid = new String(macro.uuid);
		this.distance = macro.distance;
		this.works = macro.works;
		this.repeats = macro.repeats;
		this.destination = new String(macro.destination);
		this.duration = macro.duration;
		this.userMessage = new String(macro.userMessage);
	}
	
	
	
}
