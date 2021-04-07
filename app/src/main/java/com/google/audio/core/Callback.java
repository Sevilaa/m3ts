package com.google.audio.core;

/**
 * Java FFT implementation by Google
 * https://code.google.com/archive/p/android-spectrum-analyzer/
 * GNU GPL v3
 */
public interface Callback {
    void onBufferAvailable(byte[] buffer);
}