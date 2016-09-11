package com.allen.joseph.NexusUpgrader.Utils;

import java.util.ArrayList;
import java.util.List;

import com.google.common.eventbus.EventBus;

import javafx.application.Platform;

/**
 * 
 * @author Joseph Allen
 *
 */
public class EventBusUtil {
	private static EventBus eventBus = new EventBus();
	private static List<Object> eventListenerList = new ArrayList<Object>();
	
	private EventBusUtil() {}

	public static void register(Object o) {
		if(!eventListenerList.contains(o)){
			eventBus.register(o);
			eventListenerList.add(o);
		}
	}

	public static void unregister(Object o) {
		try {
			if (eventListenerList.contains(o)) {
				eventBus.unregister(o);
				eventListenerList.remove(o);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isRegistered(Object o) {
		return eventListenerList.contains(o);
	}

	public static void publish(final String event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					eventBus.post(event);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}
}
