package com.fs.starfarer.api.campaign;

import java.util.List;

public interface GenericPluginManagerAPI {

	public static interface GenericPlugin {
		/**
		 * Negative priority means plugin doesn't want to handle whatever the parameters indicate the
		 * action is.
		 * @param params
		 * @return
		 */
		int getHandlingPriority(Object params);
	}
	
	boolean hasPlugin(Class c);
	void addPlugin(GenericPlugin plugin);
	void addPlugin(GenericPlugin plugin, boolean isTransient);
	void removePlugin(GenericPlugin plugin);
	List<GenericPlugin> getPluginsOfClass(Class c);
	<T>T pickPlugin(Class<T> c, Object params);
}
