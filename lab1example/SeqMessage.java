
import javax.sound.midi.Sequence;

import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class SeqMessage extends Message {

    String text;
    Boolean tokenMessage;
    int tokenSeq;

    // when sending the final message
    public SeqMessage(int sender, String text, Boolean tokenMessage, int tokenSeq) {
        super(sender);
        this.text = text;
        this.tokenMessage = tokenMessage;
        this.tokenSeq = tokenSeq;
    }

    /**
     * Returns the text of the message only. The toString method can
     * be implemented to show additional things useful for debugging
     * purposes.
     */
    public String getText() {
        return text;
    }

    public int getSeq() {
        return tokenSeq;
    }

    public static final long serialVersionUID = 0;
}
