package mainPackage;

import java.io.File;
import java.util.ArrayList;
import javax.sound.midi.*;

public class musicApp {
    public static Sequence seqInput;

    /**
     *
     * @param args
     * takes in midi file path into args, analyzes input, generates melody from markov model
     * .*/
    public static void main(String[] args) {
        /* 48 is middle C
         * octave is 12 (13?)
         */
        assert args.length > 1;
        try {
            process(args);
        } catch (Exception ex) {
            System.out.print("failed to parse input: " + ex);
        }
        Generator.generateSong(100);
        System.out.println("finished generating");
//        notes.add(new int[]{48,10});
//        notes.add(new int[]{50,5});
//        notes.add(new int[]{52,10});
//        notes.add(new int[]{48,5});
//        notes.add(new int[]{52,10});
//        notes.add(new int[]{48,10});
//        notes.add(new int[]{52,5});
//        chords.add(new String[]{"major", "10"});
//        chords.add(new String[]{"sus2", "10"});
//        chords.add(new String[]{"sus4", "10"});
//        chords.add(new String[]{"major", "10"});
//        playNotes(notes, 0);
//        playChords(chords, 0, 48);
    }

    /**
     *
     * @param args
     * @throws Exception
     * takes in args[1] as filepath and analyzes input
     */
    public static void process(String... args) throws Exception {
        Sequence input = MidiSystem.getSequence(new File(args[0]));
        seqInput = input;
        Sequencer sequencer = MidiSystem.getSequencer();
        sequencer.open();
        sequencer.setSequence(input);
//        sequencer.start();
        Markov.analyze(input);
        Track[] temp = input.getTracks();
        // Start playing
    }

    /**
     *
     * @param notes list of notes to play
     * @param i
     */
    public static void playNotes(ArrayList<int[]> notes, int i) {
        try {
            if (i >= notes.size()) {
                return;
            }
            int note = notes.get(i)[0];
            int velocity = notes.get(i)[1];
            play(note, (long) velocity);
            Thread.sleep(velocity * 100);
            playNotes(notes, i + 1);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     *
     * @param notes
     * @param time
     * @param i
     * plays list of notes with associated time and velocity i
     */
    public static void playNotes(ArrayList<Integer> notes, ArrayList<Long> time, int i) {
        try {
            if (i >= notes.size()) {
                return;
            }
            int note = notes.get(i);
            Long velocity = time.get(i);
            System.out.println("PLAY: @" + velocity + " key = " + note);
            play(note, velocity*3);
            Thread.sleep(velocity*3);
            playNotes(notes, time, i + 1);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     *
     * @param note
     * @param velocity
     * creates and plays note @ pitch note and velocity
     */
    public static void play(int note, Long velocity) {
        /* Notes about midi player:
         *  setMessage(command, channel, data1, data2)
         *       command: 144 = on, 128 = off
         *           192 = change instrument:
         *               channel: current channel
         *               data1: instrument to change to
         *       channel: which instrument playing
         *       data1: note to play
         *       data2: velocity
         *   midiEvent(message, tick):
         *       message: message
         *       tick: time
         * */
        try {
            Sequencer player = MidiSystem.getSequencer();
            player.open();
            Sequence seq = new Sequence(seqInput.getDivisionType(), seqInput.getResolution());
            Track track = seq.createTrack();

            ShortMessage a = new ShortMessage();
            a.setMessage(144, 1, note, 100);
            MidiEvent noteOn = new MidiEvent(a, 0);
            track.add(noteOn);

            ShortMessage b = new ShortMessage();
            b.setMessage(128, 1, note, 100);
            MidiEvent noteOff = new MidiEvent(b, velocity);
            track.add(noteOff);

            player.setSequence(seq);
            player.start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}