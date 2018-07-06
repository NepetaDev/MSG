package cf.ominous.msg.blockchain.transaction;

import java.util.ArrayList;
import java.util.List;

import com.dampcake.bencode.Bencode;

public class Output {
    private String destination;
    private long amount;

    public Output(String destination, long amount) {
        this.destination = destination;
        this.amount = amount;
    }

    public String getDestination() {
        return this.destination;
    }

    public long getAmount() {
        return this.amount;
    }

    public byte[] serialize() {
        Bencode bencode = new Bencode();
        List<Object> list = new ArrayList<Object>();
        list.add(this.destination);
        list.add(this.amount);
        return bencode.encode(list);
    }
    
    public static Output deserialize(byte[] data) {
        try {
            Bencode bencode = new Bencode();
            List<Object> list = bencode.decode(data, com.dampcake.bencode.Type.LIST);
            Output output = new Output(list.get(0).toString(), Long.parseLong(list.get(1).toString()));
            return output;
        } catch (Exception ex) {
            return null;
        }
    }

    public String toString() {
        return new String(this.serialize());
    }
}
