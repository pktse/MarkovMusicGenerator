package mainPackage;

import lombok.Getter;
import lombok.Setter;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

@Getter
@Setter
public class midiEventWrapper {
    private MidiEvent event;
    private Long length;

    public midiEventWrapper(MidiEvent event, Long length) {
        this.event = event;
        this.length = length;
    }

    @Override
    public String toString() {
        return "midiEventWrapper{" +
                "key=" + ((ShortMessage) event.getMessage()).getData1() +
                ", duration=" + length +
                '}';
    }
}
