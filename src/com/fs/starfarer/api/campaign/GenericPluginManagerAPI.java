package com.fs.starfarer.api.campaign;

import java.util.List;

public interface GenericPluginManagerAPI {
	
	/**
	 * Lowest priority. Should only be used by core code, a modded plugin with this priority may not end up being used
	 * anywhere, as what gets picked when multiple plugins have the same priority is undefined.
	 */
	public static int CORE_GENERAL = 0;
	
	/**
	 * Should be used by mods for wholesale replacement of campaign features.
	 */
	public static int MOD_GENERAL = 100;
	
	/**
	 * Should only be used by core code.
	 */
	public static int CORE_SUBSET = 200;
	
	/**
	 * For a plugin that handles a set of circumstances. For example "interaction with all jungle worlds".
	 * Overrides any _GENERAL prioritiy implementations (i.e. "interaction with all planets").
	 * Is overriden by _SPECIFIC priority ("interaction with this particular planet").
	 */
	public static int MOD_SUBSET = 300;
	
	/**
	 * Should be used by core code only for specific encounters. For example, a "special" planet or fleet
	 * could have their own dialog, and the priority of this would override a mod that replaces the general interactions
	 * with all planets or fleets.
	 */
	public static int CORE_SPECIFIC = 400;
	
	/**
	 * Should be used by mods for specific encounters, that is, encounters that aren't handled by
	 * any of the _GENERAL and _SET priority plugins. For example, if a specific fleet has a special encounter dialog, it would be
	 * returned using this priority.
	 */
	public static int MOD_SPECIFIC = 500;
	
	/**
	 * Absolute highest priority; shouldn't be used without good reason.
	 * A mod compilation might use this to resolve conflicts introduced by mods it contains.
	 */
	public static int HIGHEST = Integer.MAX_VALUE;
	
	
	

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
