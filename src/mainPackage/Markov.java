package mainPackage;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Markov {
    public static ArrayList<MidiEvent> ogPrevEvents = new ArrayList();
    public static final int DEPTH = 2 ;
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static Long prevTime = 0L;
    private static ArrayList<MidiEvent> prevEvents = new ArrayList();
    public static int totalNotes = 0;
    public static HashMap<List<Integer>, HashMap<Integer, Long>> notesTransitionModel = new HashMap<>(new HashMap<>());
    public static HashMap<List<Long>, HashMap<Long, Long>> timeTransitionModel = new HashMap<>(new HashMap<>());
    public static ArrayList<Integer> possibleNotes = new ArrayList<>();
    public static ArrayList<Long> possibleTimes = new ArrayList<>();


    public static void analyze(Sequence seq) throws Exception {
        totalNotes = seq.getTracks().length;
        Sequencer player = MidiSystem.getSequencer();
        player.open();
        Sequence seq2 = new Sequence(seq.getDivisionType(), seq.getResolution());
        Track track = seq2.createTrack();
        Track currTrack = seq.getTracks()[0];
        for (int i = 0; i < currTrack.size(); i++){
            if (i == 240){
                break;
            }
            MidiEvent event = currTrack.get(i);
            if (prevEvents.size() < DEPTH) {
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == NOTE_ON) {
                        int velocity = sm.getData2();
                        if (velocity != 0) {
                            prevEvents.add(event);
                        }
                    }
                }
                if (prevEvents.size() == DEPTH) {
                    ogPrevEvents = prevEvents;
                }
            } else if (addToMarkov(event)) {
                track.add(event);
                prevEvents.remove(0);
                prevEvents.add(event);
            }
        }
//        track = seq2.createTrack();
//        for (int i = 0; i < seq.getTracks()[1].size(); i++){
//            track.add(seq.getTracks()[1].get(i));
//        }

//        player.setSequence(seq2);
//        player.start();
    }

    private static boolean addToMarkov(MidiEvent event) throws Exception{
        MidiMessage message = event.getMessage();
        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
//            System.out.print("Channel: " + sm.getChannel() + " ");
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

                    List<Integer> prevNotes = prevEvents.stream().map(
                            (n) -> ((ShortMessage)n.getMessage()).getData1())
                            .collect(Collectors.toList());
                    HashMap<Integer, Long> notesHistory = notesTransitionModel.get(prevNotes);
                    if (notesHistory == null) {
                        notesHistory = new HashMap<>();
                    }
                    notesHistory.put(key, notesHistory.getOrDefault(prevEvents, 0L) + 1);
                    notesTransitionModel.put(prevNotes, notesHistory);
                    possibleNotes.add(key);

                    List<Long> prevTimes = prevEvents.stream().map(
                            (n) -> n.getTick())
                            .collect(Collectors.toList());
                    HashMap<Long, Long> timeHistory = timeTransitionModel.get(prevTimes);
                    if (timeHistory == null) {
                        timeHistory = new HashMap<>();
                    }
                    timeHistory.put(diff, timeHistory.getOrDefault(prevEvents, 0L) + 1);
                    timeTransitionModel.put(prevTimes, timeHistory);
                    possibleTimes.add(diff);

                    System.out.println(noteName + octave + " key=" + key);
                    totalNotes += 1;
                    return true;
                }
            } //else if (sm.getCommand() == NOTE_OFF) {
//                int key = sm.getData1();
//                int octave = (key / 12)-1;
//                int note = key % 12;
//                String noteName = NOTE_NAMES[note];
//                int velocity = sm.getData2();
//                System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
//            } else {
//                System.out.println("Command:" + sm.getCommand());
//            }
        } //else {
//            System.out.println("Other message: " + message.getClass());
//        }
        return false;
    }

    public static int getNote(ArrayList<Integer> prevNotes) {
        double chosen = Math.random();
        double total = 0L;
        if (notesTransitionModel.get(prevNotes) == null) {
            return possibleNotes.get((int) Math.floor(Math.random() * possibleNotes.size()));
        } else {
            for (Map.Entry<Integer, Long> entry : notesTransitionModel.get(prevNotes).entrySet()) {
                total += entry.getValue();
            }
            double curr = 0L;
            for (Map.Entry<Integer, Long> entry : notesTransitionModel.get(prevNotes).entrySet()) {
                curr += entry.getValue() / total;
                if (chosen >= curr) {
                    return entry.getKey();
                }
            }
        }
        return possibleNotes.get((int) Math.floor(Math.random() * possibleNotes.size()));
    }

    public static Long getTime(ArrayList<Long> prevTimes) {
        double chosen = Math.random();
        double total = 0L;
        if (notesTransitionModel.get(prevTimes) == null) {
            return possibleTimes.get((int) Math.floor(Math.random() * possibleTimes.size()));
        } else {
            for (Map.Entry<Long, Long> entry : timeTransitionModel.get(prevTimes).entrySet()) {
                total += entry.getValue();
            }
            double curr = 0L;
            for (Map.Entry<Long, Long> entry : timeTransitionModel.get(prevTimes).entrySet()) {
                curr += entry.getValue() / total;
                if (chosen >= curr) {
                    return entry.getKey();
                }
            }
        }
        return possibleTimes.get((int) Math.floor(Math.random() * possibleTimes.size()));
    }
}
