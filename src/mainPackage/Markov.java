package mainPackage;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Markov {
    public static ArrayList<midiEventWrapper> ogPrevEvents = new ArrayList();
    public static final int DEPTH = 5 ;
    public static final int NOTE_ON = 0x90;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static Long prevTime = 0L;
    private static ArrayList<midiEventWrapper> prevEvents = new ArrayList();
    public static int totalNotes = 0;
    public static transitionModel notesTransitionModel = new transitionModel();
    public static transitionModel timeTransitionModel = new transitionModel();


    public static void analyze(Sequence seq) throws Exception {
        totalNotes = seq.getTracks().length;
        Sequencer player = MidiSystem.getSequencer();
        player.open();
        Sequence seq2 = new Sequence(seq.getDivisionType(), seq.getResolution());
        Track track = seq2.createTrack();
        Track currTrack = seq.getTracks()[0];
        for (int i = 0; i < currTrack.size(); i++){
            // limit size of input
            if (i == 240){
                break;
            }
            MidiEvent event = currTrack.get(i);
            if (prevEvents.size() < DEPTH) {
                // still need to first few notes that don't depend on history
                if (addToMarkov(event)) {
                    track.add(event);
                }
                if (prevEvents.size() == DEPTH) {
                    ogPrevEvents = (ArrayList<midiEventWrapper>) prevEvents.clone();
                }
            } else if (addToMarkov(event)) {
                //pops of first of prev events and appends new note to prev events
                track.add(event);
                prevEvents.remove(0);
            }
        }
    }

    private static boolean addToMarkov(MidiEvent event) {
        MidiMessage message = event.getMessage();
        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            if (sm.getCommand() == NOTE_ON) {
                int key = sm.getData1();
                int octave = (key / 12)-1;
                int note = key % 12;
                String noteName = NOTE_NAMES[note];
                int velocity = sm.getData2();
                if (velocity != 0) {
                    Long diff = event.getTick() - prevTime;
                    System.out.print("@" + diff + " ");
                    prevTime = event.getTick();

                    if (prevEvents.size() > 1) {
                        List<Long> prevNotes = prevEvents.stream().map(
                                (n) -> (long) ((ShortMessage) n.getEvent().getMessage()).getData1())
                                .collect(Collectors.toList());

                        notesTransitionModel.addToTransModel((long) key, prevNotes);

                        List<Long> prevTimes = prevEvents.stream().map(
                                (n) -> n.getLength())
                                .collect(Collectors.toList());
                        timeTransitionModel.addToTransModel(diff, prevTimes);
                    }

                    System.out.println(noteName + octave + " key=" + key);
                    totalNotes += 1;

                    prevEvents.add(new midiEventWrapper(event, diff));
                    return true;
                }
            }
        }
        return false;
    }
}
