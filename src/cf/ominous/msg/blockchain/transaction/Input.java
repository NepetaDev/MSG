package cf.ominous.msg.blockchain.transaction;

import java.util.ArrayList;
import java.util.List;

import com.dampcake.bencode.Bencode;

public class Input {
    private String source;
    private long amount;
    private int outputIndex;

    public Input(String source, long amount, int outputIndex) {
        this.source = source;
        this.amount = amount;
        this.outputIndex = outputIndex;
    }

    public String getSource() {
        return this.source;
    }

    public long getAmount() {
        return this.amount;
    }

    public int getOutputIndex() {
        return this.outputIndex;
    }

    public byte[] serialize() {
        Bencode bencode = new Bencode();
        List<Object> list = new ArrayList<Object>();
        list.add(this.source);
        list.add(this.amount);
        list.add(this.outputIndex);
        return bencode.encode(list);
    }
    
    public static Input deserialize(byte[] data) {
        try {
            Bencode bencode = new Bencode();
            List<Object> list = bencode.decode(data, com.dampcake.bencode.Type.LIST);
            Input input = new Input(list.get(0).toString(), Long.parseLong(list.get(1).toString()), Integer.parseInt(list.get(2).toString()));
            return input;
        } catch (Exception ex) {
            return null;
        }
    }

    public String toString() {
        return new String(this.serialize());
    }
}
