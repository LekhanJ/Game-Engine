package org.kenji.util;

public class Time {
    // Initialized at the start
    public static float timeStarted = System.nanoTime();

    // Gives how much time has passes since the beginning in seconds
    public static float getTime() {
        return (float)((System.nanoTime() - timeStarted * 1E-9));
    }
}
