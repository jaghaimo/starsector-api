package com.fs.starfarer.api.campaign.listeners;

import java.util.List;

@SuppressWarnings("unchecked")
public interface ListenerManagerAPI {

	void addListener(Object listener);
	void addListener(Object listener, boolean isTransient);
	
	void removeListener(Object listener);
	void removeListenerOfClass(Class<?> c);
	
	boolean hasListener(Object listener);
	boolean hasListenerOfClass(Class<?> c);

	<T> List<T> getListeners(Class<T> c);

}
