package mainPackage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;

public class Generator {
//    private static ArrayList<int[]> notesToPlay = new ArrayList();
    private static ArrayList<Integer> prevNotes = new ArrayList();
    private static ArrayList<Long> prevTimes = new ArrayList();

    public static void generateSong(int length) {
        Long prevTime = 0L;
        ArrayList<Integer> notes = new ArrayList<>();
        ArrayList<Long> time = new ArrayList<>();
        for (MidiEvent event: Markov.ogPrevEvents) {
            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                int key = sm.getData1();
                long diff = Markov.getTime(prevTimes);

                prevNotes.add(key);
                prevTimes.add(diff);
                notes.add(key);
                time.add(diff);
            }
        }
        for (int i = 0; i < length; i++) {
            int newNote = Markov.getNote(prevNotes);
            Long newTime = Markov.getTime(prevTimes);

            notes.add(newNote);
            time.add(newTime);
            prevNotes.remove(0);
            prevNotes.add(newNote);
            prevTimes.remove(0);
            prevTimes.add(newTime);
        }

        musicApp.playNotes(notes, time, 0);
    }
}
