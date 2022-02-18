
import java.util.LinkedList;

import mcgui.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class Caster extends Multicaster {

    boolean hasToken = false;
    int tokenSeq = 0;
    int latestPushedSeq = 0;
    int nextHost;

    /**
     * sequencer
     * No initializations needed for this simple one
     */
    public void init() {
        mcui.debug("The network has " + hosts + " hosts and you are node " + id + "!");
        if (id == 0) {
            hasToken = true;
            bcom.basicsend(0, new SeqMessage(id, "", true, 1));
        }
        nextHost = id + 1;
        if (nextHost == hosts) {
            nextHost = 0;
        }

    }

    LinkedList<String> messageQueue = new LinkedList<>();

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        messageQueue.add(messagetext);
    }

    public void send() {
        String message = messageQueue.pop();
        for (int i = 0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if (i != id) {
                bcom.basicsend(i, new SeqMessage(id, message, false, tokenSeq));
            }
        }

        deliver(new SeqMessage(id, message, false, tokenSeq));
    }

    LinkedList<SeqMessage> messageStore = new LinkedList<>();

    public void deliver(SeqMessage message) {
        if (message.getSeq() == latestPushedSeq + 1) {
            latestPushedSeq++;
            mcui.debug("Pushed message with sequence: " + message.getSeq());
            mcui.deliver(message.getSender(), message.getText(), message.getSeq() + "");
            pushStore();
        } else {
            mcui.debug("Inconsitent sequence, pushed to store " + message.getSeq());
            messageStore.add(message);
        }
    }

    public void pushStore() {
        int searchSeq = this.latestPushedSeq + 1;
        boolean found = false;
        for (int i = 0; i < messageStore.size(); i++) {
            if (messageStore.get(i).getSeq() == searchSeq) {
                mcui.deliver(messageStore.get(i).getSender(), messageStore.get(i).text,
                        messageStore.get(i).getSeq() + "");
                mcui.debug("Pushed message from store with seq: " + messageStore.get(i).getSeq());
                this.latestPushedSeq++;
                messageStore.remove(i);
                found = true;
                break;
            }
        }
        if (found) {
            pushStore();
        }
    }

    /**
     * Receive a basic message
     * 
     * @param message The message received
     */
    public void basicreceive(int peer, Message message) {
        SeqMessage incMessage = (SeqMessage) message;

        if (incMessage.tokenMessage) {
            if (messageQueue.size() != 0) {
                tokenSeq = incMessage.tokenSeq;
                send();
                bcom.basicsend(nextHost, new SeqMessage(id, "", true, ++tokenSeq));
            } else {
                tokenSeq = incMessage.tokenSeq;

                bcom.basicsend(nextHost, new SeqMessage(id, "", true, tokenSeq));
            }

        } else {
            deliver(incMessage);
        }
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
        // If the node is the next host in the token ring
        if (peer == nextHost) {
            nextHost = (nextHost + 1) % hosts;
            bcom.basicsend(nextHost, new SeqMessage(id, "", true, tokenSeq));
            mcui.debug("new next host is: " + nextHost);
        }

    }
}
