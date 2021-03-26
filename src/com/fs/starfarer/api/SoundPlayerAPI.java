package com.fs.starfarer.api;

import org.lwjgl.util.vector.Vector2f;

/**
 * @author Alex Mosolov
 *
 * Copyright 2013 Fractal Softworks, LLC
 */
public interface SoundPlayerAPI {
	/**
	 * UI sound files should be stereo.
	 * @param id
	 * @param pitch
	 * @param volume
	 */
	SoundAPI playUISound(String id, float pitch, float volume);
	
	/**
	 * Sound file must be *mono* for it to be properly played within the engine.
	 * @param id
	 * @param pitch
	 * @param volume
	 * @param loc
	 * @param vel
	 * @return
	 */
	SoundAPI playSound(String id, float pitch, float volume, Vector2f loc, Vector2f vel);
	
	/**
	 * Loop a sound. Must be called every frame or the loop will fade out. Should be mono.
	 * 
	 * @param id
	 * @param playingEntity Required. Used to help uniquely identify playing loops. Also used to figure out which loops to not play (i.e., same entity playing multiples of the same loop would only play the one.)
	 * @param pitch
	 * @param volume
	 * @param loc
	 * @param vel
	 */
	void playLoop(String id, Object playingEntity, float pitch, float volume, Vector2f loc, Vector2f vel);
	
	
	/**
	 * Thread-safe - can be called from threads other than the main loop thread.
	 */
	void restartCurrentMusic();
	
	/**
	 * Thread-safe - can be called from threads other than the main loop thread.
	 * The music id is the name of the file, including its path. It is NOT the
	 * name of the music set that gets passed in to playMusic, which may consist
	 * of multiple files, one of which will get picked randomly.
	 * 
	 * Returns the string "nothing" when nothing is playing.
	 * 
	 * @return
	 */
	String getCurrentMusicId();
	
	
	/**
	 * Thread-safe - can be called from threads other than the main loop thread.
	 * 
	 * Fade in/out are in seconds and have to be whole numbers.
	 * Can pass in null for musicSetId to stop the current track without starting a new one.
	 * @param fadeOutIfAny
	 * @param fadeIn
	 * @param musicSetId
	 */
	void playCustomMusic(int fadeOutIfAny, int fadeIn, String musicSetId);
	
	/**
	 * Use playCustomMusic instead; has same effect. Deprecation is for naming consistency only.
	 * @param fadeOutIfAny
	 * @param fadeIn
	 * @param musicSetId
	 */
	@Deprecated void playMusic(int fadeOutIfAny, int fadeIn, String musicSetId);
	
	/**
	 * Thread-safe - can be called from threads other than the main loop thread.
	 * 
	 * Won't stop any currently-playing music, but will prevent new tracks from starting playback
	 * automatically when the current one is over. Meant to be used in conjunction with
	 * playMusic() for customized music playback.
	 * 
	 * See also: SettingsAPI.getCurrentGameState()
	 * 
	 * Stops
	 * @param suspendMusicPlayback
	 */
	void setSuspendDefaultMusicPlayback(boolean suspendMusicPlayback);
	
	Vector2f getListenerPos();

	void pauseCustomMusic();
	void resumeCustomMusic();
	void playCustomMusic(int fadeOutIfAny, int fadeIn, String musicSetId, boolean looping);

	void playUILoop(String id, float pitch, float volume);

	void playLoop(String id, Object playingEntity, float pitch, float volume, Vector2f loc, Vector2f vel, float fadeIn, float fadeOut);
}




