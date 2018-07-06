package cf.ominous.msg.blockchain.transaction;

import java.util.ArrayList;
import java.util.List;

import com.dampcake.bencode.Bencode;

import cf.ominous.msg.blockchain.Utils;

public class Signature {
    private byte[] data;
    private byte[] publicKey;

    public Signature() {
        this(null, null);
    }

    public Signature(byte[] data, byte[] publicKey) {
        this.data = data;
        this.publicKey = publicKey;
    }

    public byte[] getSignedData() {
        return this.data;
    }

    public byte[] getPublicKey() {
        return this.publicKey;
    }

    public String getPublicKeyHash() {
        return Utils.byteArrayToString(Utils.hash(this.publicKey));
    }

    public byte[] serialize() {
        Bencode bencode = new Bencode();
        List<Object> list = new ArrayList<Object>();
        list.add(Utils.byteArrayToString(this.data));
        list.add(Utils.byteArrayToString(this.publicKey));
        return bencode.encode(list);
    }
    
    public static Signature deserialize(byte[] data) {
        try {
            Bencode bencode = new Bencode();
            List<Object> list = bencode.decode(data, com.dampcake.bencode.Type.LIST);
            Signature signature = new Signature(Utils.stringToByteArray(list.get(0).toString()), Utils.stringToByteArray(list.get(1).toString()));
            return signature;
        } catch (Exception ex) {
            return null;
        }
    }

    public String toString() {
        return new String(this.serialize());
    }
}
