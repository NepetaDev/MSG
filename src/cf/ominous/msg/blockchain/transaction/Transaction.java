package cf.ominous.msg.blockchain.transaction;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.List;

import com.dampcake.bencode.Bencode;

import cf.ominous.msg.blockchain.Blockchain;
import cf.ominous.msg.blockchain.Utils;

public class Transaction {
    public static enum Type {
        NORMAL, REWARD
    }

    List<Input> inputs;
    List<Output> outputs;
    List<Signature> signatures;
    List<String> publicKeyHashes;
    long timestamp;
    Type type;
    boolean signed;

    public Transaction(Type type) {
        this.type = type;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.signatures = new ArrayList<>();
        this.publicKeyHashes = new ArrayList<>();
        this.signed = false;
    }

    public void addInput(Input input) throws TransactionAlreadySignedException {
        if (this.signed)
            throw new TransactionAlreadySignedException();
        this.inputs.add(input);
    }

    public void addOutput(Output output) throws TransactionAlreadySignedException {
        if (this.signed)
            throw new TransactionAlreadySignedException();
        this.outputs.add(output);
    }

    public void addSignature(Signature signature) {
        this.signatures.add(signature);
        this.publicKeyHashes.add(signature.getPublicKeyHash());
    }

    public void sign(KeyPair[] keys) throws TransactionAlreadySignedException {
        if (this.signed)
            throw new TransactionAlreadySignedException();
        this.timestamp = Utils.getUnixTimestamp();

        byte[] hashed = Utils.hash(this.serialize(false));

        for (KeyPair key : keys) {
            this.addSignature(new Signature(Utils.sign(hashed, key.getPrivate()),
                    Utils.serializePublicKey((ECPublicKey) key.getPublic())));
        }

        signed = true;
    }

    public boolean verifySignatures() {
        byte[] hashed = Utils.hash(this.serialize(false));
        if (this.signatures.size() == 0 && this.type != Type.REWARD)
            return false;
        try {
            boolean valid = true;
            for (Signature signature : this.signatures) {
                PublicKey publicKey = Utils.deserializePublicKey(signature.getPublicKey());
                if (publicKey == null || !Utils.verify(hashed, signature.getSignedData(), publicKey)) {
                    valid = false;
                    break;
                }
            }
            return valid;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean verifyInputs(Blockchain blockchain) {
        boolean valid = true;
        for (Input input : this.inputs) {
            Transaction transaction = blockchain.findTransaction(input.getSource());
            if (transaction == null) {
                valid = false;
                break;
            }

            Output output = transaction.getOutputAt(input.getOutputIndex());
            if (output.getAmount() != input.getAmount() || !this.publicKeyHashes.contains(output.getDestination())) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    public Output getOutputAt(int index) {
        if (this.outputs.size() >= index + 1) {
            return this.outputs.get(index);
        } else {
            return null;
        }
    }

    public long getTimestamp() {
        return this.timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean validate(Blockchain blockchain) {
        long inputSum = 0;
        long outputSum = 0;

        for (Input input : this.inputs) {
            if (input.getAmount() <= 0)
                return false;
            inputSum += input.getAmount();
        }

        for (Output output : this.outputs) {
            if (output.getAmount() <= 0)
                return false;
            outputSum += output.getAmount();
        }

        if (inputSum != outputSum)
            return false;

        switch (this.type) {
            case NORMAL:
                if (this.inputs.size() == 0 || this.outputs.size() == 0) {
                    return false;
                }
                break;
            case REWARD:
                //TODO: compare inputSum to rewards from previous block
                if (this.outputs.size() != 1) {
                    return false;
                }
                break;
            default:
                return false;
        }

        if (!this.verifySignatures() || !this.verifyInputs(blockchain)) {
            return false;
        }
        
        return true;
    }

    public Type getType() {
        return this.type;
    }

    public byte[] serialize(boolean includeSignatures) {
        Bencode bencode = new Bencode();
        List<Object> list = new ArrayList<Object>();
        list.add(this.type.ordinal());
        list.add(this.timestamp);
        list.add(this.inputs);
        list.add(this.outputs);
        if (this.type != Type.REWARD && includeSignatures) {
            list.add(this.signatures);
        }
        return bencode.encode(list);
    }

    @SuppressWarnings("unchecked")
    public static Transaction deserialize(byte[] data) {
        try {
            Bencode bencode = new Bencode();
            List<Object> list = bencode.decode(data, com.dampcake.bencode.Type.LIST);
            Type type = Type.values()[Integer.parseInt(list.get(0).toString())];
            Transaction transaction = new Transaction(type);
            
            transaction.setTimestamp(Long.parseLong(list.get(1).toString()));
            
            List<String> inputs = (List<String>)list.get(2);
            for (String input : inputs) {
                transaction.addInput(Input.deserialize(input.getBytes()));
            }
            
            List<String> outputs = (List<String>)list.get(3);
            for (String output : outputs) {
                transaction.addOutput(Output.deserialize(output.getBytes()));
            }
            
            if (list.size() >= 5) {
                List<String> signatures = (List<String>)list.get(4);
                for (String signature : signatures) {
                    transaction.addSignature(Signature.deserialize(signature.getBytes()));
                }
            }
            
            return transaction;
        } catch (Exception ex) {
            return null;
        }
    }

    public byte[] getHash() {
        return Utils.hash(this.serialize(true));
    }

    public String toString() {
        return Utils.byteArrayToString(this.getHash());
    }
}
