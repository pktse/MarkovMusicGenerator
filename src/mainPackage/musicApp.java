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
    public static void main(String[] args) throws Exception{
        /* 48 is middle C
         * octave is 12 (13?)
         */
        assert args.length > 1;
        process(args);

        Generator.generateSong(100);
        System.out.println("finished generating");
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
        System.out.println(seqInput.getDivisionType());
        System.out.println(seqInput.getResolution());
        System.out.println(seqInput.getTickLength());
        Sequencer sequencer = MidiSystem.getSequencer();
        sequencer.open();
        sequencer.setSequence(input);
//        sequencer.start();
        Markov.analyze(input);
        // Start playing
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
            if (i > 50) {
                return;
            }
            int note = notes.get(i);

            int note2 = note % 12;
            String noteName = Markov.NOTE_NAMES[note2];

            System.out.println("PLAY: @" + time.get(i) + " key = " + note + " note = " + noteName);
            Long velocity = time.get(i) * 2;
            play(note, velocity);
            Thread.sleep(velocity);
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
            a.setMessage(144, 1, note, 80);
            MidiEvent noteOn = new MidiEvent(a, 0);
            track.add(noteOn);

            ShortMessage b = new ShortMessage();
            b.setMessage(144, 1, note, 0);
            MidiEvent noteOff = new MidiEvent(b, velocity);
            track.add(noteOff);

            player.setSequence(seq);
            player.start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
