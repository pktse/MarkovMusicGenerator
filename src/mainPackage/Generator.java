package mainPackage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;

public class Generator {
    private static ArrayList<Long> prevNotes = new ArrayList();
    private static ArrayList<Long> prevTimes = new ArrayList();

    public static void generateSong(int length) {
        System.out.println("---------------time: \n" + Markov.timeTransitionModel);
        System.out.println("---------------notes: \n" + Markov.notesTransitionModel);
        ArrayList<Integer> notes = new ArrayList<>();
        ArrayList<Long> time = new ArrayList<>();
        for (midiEventWrapper event: Markov.ogPrevEvents) {
            MidiMessage message = event.getEvent().getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                int key = sm.getData1();
                long diff = event.getLength();

                prevNotes.add((long) key);
                prevTimes.add(diff);
                notes.add(key);
                time.add(diff);
            }
        }

//        int j = 0;
//        while (prevNotes.size() < 3) {
//            midiEventWrapper event = Markov.ogPrevEvents.get(j);
//            MidiMessage message = event.getEvent().getMessage();
//            if (message instanceof ShortMessage) {
//                ShortMessage sm = (ShortMessage) message;
//                int key = sm.getData1();
//                long diff = event.getLength();
//
//                prevNotes.add((long) key);
//                prevTimes.add(diff);
//                notes.add(key);
//                time.add(diff);
//            }
//            j++;
//        }
        for (int i = 0; i < length; i++) {
            long newNote = Markov.notesTransitionModel.getPrediction(prevNotes);
            Long newTime = Markov.timeTransitionModel.getPrediction(prevTimes);

            notes.add((int) newNote);
            time.add(newTime);
            prevNotes.remove(0);
            prevNotes.add(newNote);
            prevTimes.remove(0);
            prevTimes.add(newTime);
        }

        time.remove(0);
        notes.remove(notes.size() - 1);
        musicApp.playNotes(notes, time, 0);
    }
}
