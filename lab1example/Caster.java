
import java.util.LinkedList;

import mcgui.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class Caster extends Multicaster {

    // for the sequencer to keep track
    int sequenceCounter = 1;
    // tracking sequences
    int sequenceTracker = 0;
    int sequencer = 0;

    LinkedList<String> messageQueue = new LinkedList<>();

    /**
     * No initializations needed for this simple one
     */
    public void init() {
        mcui.debug("The network has " + hosts + " hosts and you are node " + id + "!");
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        // enqueue message
        messageQueue.add(messagetext);
        // Send seqnumber request to node 1
        bcom.basicsend(sequencer, new SeqMessage(id));
        mcui.debug("Enqueded: \"" + messagetext + "\"" + "Asked for sequence number");
    }

    /**
     * Receive a basic message
     * 
     * @param message The message received
     */
    public void basicreceive(int peer, Message message) {
        SeqMessage incMessage = (SeqMessage) message;
        if (incMessage.getSeqMsg() && incMessage.getSeq() == 0) {
            mcui.debug("Got request for seq number");
            SeqRespond(incMessage.getSender());
        } else if (incMessage.getSeqMsg()) {
            mcui.debug("Got sequence number");
            send(incMessage.getSeq());
        } else {
            deliver(peer, incMessage);
        }
    }

    /**
     * Responds with a sequence number to the correct node
     * 
     * @param id
     */
    public void SeqRespond(int id) {
        mcui.debug("Sent back sequence number");
        bcom.basicsend(id, new SeqMessage(id, this.sequenceCounter++));
    }

    /**
     * When sequence number has been received from the sequencer we broadcast them
     * to everyone except ourselves
     * in th correct message format
     * 
     * @param sequence
     */
    public void send(int sequence) {
        String messagetext = this.messageQueue.pop();
        for (int i = 0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if (i != id) {
                bcom.basicsend(i, new SeqMessage(id, messagetext, sequence));
            }
        }
        deliver(id, new SeqMessage(id, messagetext, sequence));
        mcui.debug("Sent out \"" + messagetext + "\" with sequence " + sequence);
    }

    LinkedList<SeqMessage> messageStore = new LinkedList<>();

    public void deliver(int peer, SeqMessage incMessage){
        if(this.sequenceTracker + 1 == incMessage.getSeq()){
            mcui.deliver(peer, incMessage.getText(), incMessage.getSeq() + "");
            mcui.debug("printed message with sequence " + incMessage.getSeq());
            this.sequenceTracker = incMessage.getSeq();
            pushStore();
        }else{
            messageStore.add(incMessage);
            mcui.debug("Inconsistency detected, enqueed message with sequence " + incMessage.getSeq());
        }
    }

    public void pushStore(){
        int searchSeq = this.sequenceTracker +1;
        boolean found = false;
        for (int i = 0; i < messageStore.size(); i++){
            if(messageStore.get(i).getSeq() == searchSeq){
                mcui.deliver(messageStore.get(i).getSender(), messageStore.get(i).text, messageStore.get(i).getSeq() + "");
                mcui.debug("Pushed message from store with seq: " + messageStore.get(i).getSeq());
                this.sequenceTracker++;
                messageStore.remove(i);
                found = true;
                break;
            }
        }
        if(found){pushStore();}
    }

    /**
     * Signals that a peer is down and has been down for a while to
     * allow for messages taking different paths from this peer to
     * arrive.
     * 
     * @param peer The dead peer
     */
    public void basicpeerdown(int peer) {
        mcui.debug("Peer " + peer + " has been dead for a while now!");
    }
}
