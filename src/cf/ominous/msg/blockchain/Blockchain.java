package cf.ominous.msg.blockchain;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import cf.ominous.msg.blockchain.transaction.Transaction;

public class Blockchain {
    private List<Block> mainChain;
    private List<Block> orphans;
    private Map<String, Transaction> transactions; // make this more efficient <hash, obj>
    private Map<String, Transaction> uncomfirmedTransactions; // mempool
    //store the block that aren't on the main chain and delete them if not needed
    //unspentoutputs

    public Blockchain() {
        this.mainChain = new ArrayList<>();
        this.transactions = new HashMap<>();
        this.uncomfirmedTransactions = new HashMap<>();
    }

    public void addTransaction(Transaction transaction) {
        // TODO: validate
        // TODO: remove from unspentoutputs
        this.transactions.put(transaction.toString(), transaction);
    }
    
    //remove transaction and add to unspent

    public void addBlock(Block block) {
        // TODO: validate
        // TODO: check pow
        // TODO: check orphans
        if (this.mainChain.isEmpty() || this.getLastBlockHash() == block.getPreviousBlockHash()) {
            this.mainChain.add(block);
            for (Transaction transaction : block.getTransactions()) {
                this.addTransaction(transaction);
            }
        } else {
            //orphan
        }
    }
    
    public Block getLastBlock() {
        return this.mainChain.get(this.getBlockHeight());
    }
    
    public String getLastBlockHash() {
        return Utils.byteArrayToString(this.getLastBlock().getHash());
    }
    
    public int getBlockHeight() {
        return this.mainChain.size() - 1;
    }
    
    public int calculateTotalWorkStartingFromBlock(Block block) {
        return 0;
    }
    
    //remove block and transactions
    
    //get total pow

    public Transaction findTransaction(String hash) {
        return this.transactions.get(hash);
    }

    // getTransaction(id)
    // getBlock(id)
    // getDifficulty()
    // getPOW()
}
