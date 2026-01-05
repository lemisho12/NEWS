package com.client;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class TextToSpeechService {
    private Voice voice;
    private boolean isInitialized = false;

    public TextToSpeechService() {
        initializeTTS();
    }

    private void initializeTTS() {
        try {
            // Set the freetts properties to use the kevin voice
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            
            // Get the VoiceManager and get the voice by name
            VoiceManager voiceManager = VoiceManager.getInstance();
            voice = voiceManager.getVoice("kevin16");
            
            if (voice == null) {
                System.err.println("Cannot find a voice named kevin16. Please make sure the FreeTTS libraries are correctly installed.");
                return;
            }
            
            // Allocate the voice
            voice.allocate();

            // --- Custom Voice Settings ---
            // Pitch: Lowered to 85 Hz (creating a deeper tone)
            voice.setPitch(85.0f);
            
            // Rate: Slowed down to 130 wpm (for a more deliberate, authoritative speaking style)
            voice.setRate(130.0f);
            
            // Standard pitch range
            voice.setPitchRange(11.0f);

            isInitialized = true;
            System.out.println("TTS Service initialized successfully with voice: " + voice.getName() + " (Custom settings: 85Hz, 130wpm)");
            
        } catch (Exception e) {
            System.err.println("Error initializing TTS: " + e.getMessage());
            isInitialized = false;
        }
    }

    public void speak(String text) {
        if (!isInitialized || voice == null) {
            System.err.println("TTS not initialized");
            return;
        }
        
        try {
            // Speak the text
            voice.speak(text);
        } catch (Exception e) {
            System.err.println("Error speaking text: " + e.getMessage());
        }
    }

    public void stop() {
        if (voice != null) {
            voice.deallocate();
        }
    }

    public void cleanup() {
        if (voice != null) {
            voice.deallocate();
        }
    }

    public boolean isAvailable() {
        return isInitialized;
    }
}
