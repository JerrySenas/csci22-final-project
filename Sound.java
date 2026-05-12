/**
This class contains the sounds for the game. This includes sound effects and background music.
@author Jerry Señas (255351) and Angelico Soriano (255468)
@version May 12, 2026

I have not discussed the Java language code in my program
with anyone other than my instructor or the teaching assistants
assigned to this course.

I have not used Java language code obtained from another student,
or any other unauthorized source, either modified or unmodified.

If any Java language code or documentation used in my program
was obtained from another source, such as a textbook or website,
that has been clearly noted with a proper citation in the comments
of my program.
*/

import java.io.*;
import javax.sound.sampled.*;

public class Sound {
    /**
     * Loops the background music of the game.
     */
    public static void loopMusic(String filepath) {
        try {
            File music = new File(filepath);
            if (music.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(music);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays a sound effect.
     */
    public static void playSFX(String filepath) {
        try {
            File music = new File(filepath);
            if (music.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(music);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}