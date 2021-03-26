package com.fs.starfarer.api.combat.listeners;

import java.util.List;

public interface CombatListenerManagerAPI {

	void addListener(Object listener);
	
	void removeListener(Object listener);
	void removeListenerOfClass(Class<?> c);
	
	boolean hasListener(Object listener);
	boolean hasListenerOfClass(Class<?> c);

	<T> List<T> getListeners(Class<T> c);

}
