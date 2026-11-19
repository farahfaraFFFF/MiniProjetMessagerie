
package com.messagerie.client;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.DataLine;

public class EnregistreurAudio {

    private TargetDataLine microphone;
    private Thread thread;
    private File fichierAudio;
    private volatile boolean enregistrement = false;

    private AudioFormat format = new AudioFormat(
        16000, 16, 1, true, false
    );

    public void demarrer() throws Exception {

        DataLine.Info info = new DataLine.Info(
            TargetDataLine.class, format
        );

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        fichierAudio = File.createTempFile(
            "message_vocal_", ".wav"
        );

        enregistrement = true;

        thread = new Thread(() -> {
            try (AudioInputStream audio = new AudioInputStream(microphone)) {
                AudioSystem.write(
                    audio,
                    AudioFileFormat.Type.WAVE,
                    fichierAudio
                );
            } catch (IOException e) {
                System.out.println("Erreur d'enregistrement : "
                    + e.getMessage());
            }
        });

        thread.setDaemon(true);
        thread.start();

        System.out.println("Enregistrement démarré...");
    }

    public File arreter() {

        if (!enregistrement) {
            return null;
        }

        enregistrement = false;

        microphone.stop();
        microphone.close();

        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Enregistrement terminé.");

        return fichierAudio;
    }
}
