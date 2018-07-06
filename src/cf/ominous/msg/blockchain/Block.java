package cf.ominous.msg.blockchain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.dampcake.bencode.Bencode;

import cf.ominous.msg.blockchain.transaction.Input;
import cf.ominous.msg.blockchain.transaction.Transaction;
import cf.ominous.msg.blockchain.transaction.TransactionAlreadySignedException;

public class Block {
    // protocol version
    // flags
    // additional stuff, coinbase
    private String previousBlockHash;
    private String nonce;
    private double difficulty;
    private long timestamp;
    private List<Transaction> transactions;
    // difficulty

    public Block() {
        this.transactions = new ArrayList<>();
        this.timestamp = 0;
        this.nonce = "";
        this.previousBlockHash = "";
    }
    
    public Transaction createRewardTransactionForTheNextBlock(Blockchain blockchain) {
        try {
            Transaction rewardTransaction = new Transaction(Transaction.Type.REWARD);
            rewardTransaction.addInput(new Input("reward", 25, 0));
            rewardTransaction.addInput(new Input("fees", this.getFees(), 0));
            return rewardTransaction;
        } catch (TransactionAlreadySignedException e) {
            return null;
        }
    }
    
    public long getFees() {
        return 0;
    }

    public byte[] serialize(boolean includeTransactions) {
        Bencode bencode = new Bencode();
        List<Object> list = new ArrayList<Object>();
        list.add(this.previousBlockHash);
        list.add(this.nonce);
        list.add(this.timestamp);
        list.add(this.transactions);
        if (includeTransactions) {
            List<String> transactionList = new ArrayList<String>();
            for (Transaction transaction : this.transactions) {
                transactionList.add(new String(transaction.serialize(true)));
            }
            list.add(transactionList);
        }
        return bencode.encode(list);
    }
    
    @SuppressWarnings("unchecked")
    public static Block deserialize(byte[] data) {
        try {
            Bencode bencode = new Bencode();
            List<Object> list = bencode.decode(data, com.dampcake.bencode.Type.LIST);
            Block block = new Block();

            block.setPreviousBlockHash(list.get(0).toString());
            block.setNonce(list.get(1).toString());
            block.setTimestamp(Long.parseLong(list.get(2).toString()));

            List<String> transactions = (List<String>)list.get(4);
            for (String transaction : transactions) {
                block.addTransaction(Transaction.deserialize(transaction.getBytes()));
            }
            
            return block;
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean validate(Blockchain blockchain) {
        boolean valid = true;
        boolean hasReward = false;
        for (Transaction transaction : this.transactions) {
            if (!transaction.validate(blockchain)) {
                valid = false;
                break;
            }
            if (transaction.getType() == Transaction.Type.REWARD) {
                if (hasReward) {
                    valid = false;
                    break;
                } else {
                    // TODO: get block height, and calculate reward; let's assume 25 is correct atm
                    // (or anything for that matter for now)
                    hasReward = true;
                }
            }
            //TODO: check that hash matches
        }
        return valid;
    }

    public byte[] getHash() {
        return Utils.hash(this.serialize(false));
    }
    
    public double getDifficulty() {
        return this.difficulty;
    }
    
    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public boolean verifyDeclaredDifficulty() {
        BigInteger hash = new BigInteger(1, this.getHash());
        BigDecimal target = new BigDecimal(Utils.getMaxTarget()).divide(BigDecimal.valueOf(Math.pow(2, this.difficulty)), 0, RoundingMode.HALF_UP);
        if (hash.abs().compareTo(target.toBigInteger()) < 0) {
            return true;
        }
        return false;
    }

    public String toString() {
        return Utils.byteArrayToString(this.getHash());
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    public List<Transaction> getTransactions() {
        return this.transactions;
    }

    public void setPreviousBlockHash(String hash) {
        this.previousBlockHash = hash;
    }

    public String getPreviousBlockHash() {
        return this.previousBlockHash;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getNonce() {
        return this.nonce;
    }
}
