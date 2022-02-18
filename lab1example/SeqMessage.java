
import javax.sound.midi.Sequence;

import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class SeqMessage extends Message {
        
    String text;
    int sequenceNumber;
    Boolean sequenceMsg;
        
    // when sending the final message
    public SeqMessage(int sender,String text, int sequence) {
        super(sender);
        this.text = text;
        this.sequenceNumber = sequence;
        this.sequenceMsg = false;
    }
    
    // when requesting a sequence number
    public SeqMessage(int sender) {
        super(sender);
        this.sequenceNumber = 0;
        this.sequenceMsg = true;
    }

    // when responding with a sequence number
    public SeqMessage(int sender, int sequence) {
        super(sender);
        this.sequenceNumber = sequence;
        this.sequenceMsg = true;
    }

    /**
     * Returns the text of the message only. The toString method can
     * be implemented to show additional things useful for debugging
     * purposes.
     */
    public String getText() {
        return text;
    }

    public int getSeq(){
        return sequenceNumber;
    }

    public boolean getSeqMsg(){
        return sequenceMsg;
    }
    
    public static final long serialVersionUID = 0;
}
