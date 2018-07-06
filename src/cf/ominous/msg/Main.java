package cf.ominous.msg;

import java.security.KeyPair;
import java.security.PublicKey;

import cf.ominous.msg.blockchain.Block;
import cf.ominous.msg.blockchain.Blockchain;
import cf.ominous.msg.blockchain.Utils;
import cf.ominous.msg.blockchain.transaction.Input;
import cf.ominous.msg.blockchain.transaction.Output;
import cf.ominous.msg.blockchain.transaction.Transaction;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        System.out.println("test");
        String key = "l120:MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEb5ILt2ScWGa42sAJH8ixHzUiglt/nYseKV3GNe67N5pgwNiyfp9kyK0zlPSF6V3lGWeZ4allxxbKlksAMtDpNA==88:MD4CAQAwEAYHKoZIzj0CAQYFK4EEAAoEJzAlAgEBBCC/HItMdWIJrDuLbViOqDBJ/LnJmjfg/tBv7xnGy3+PKw==e";
        try {
            KeyPair pair = Utils.deserializeKeyPair(key.getBytes());
            KeyPair[] pairs = new KeyPair[] { pair };
            PublicKey pub = pair.getPublic();

            // mining test
            Blockchain b = new Blockchain();

            Transaction c = new Transaction(Transaction.Type.REWARD);
            c.addOutput(new Output(Utils.byteArrayToString(Utils.publicKeyHash(pub)), 25));
            Block bl = new Block();
            bl.addTransaction(c);
            bl.setPreviousBlockHash("");
            System.out.println("Mining started...");
            bl.setDifficulty(10);

            bl.setNonce(UUID.randomUUID().toString());
            bl.setTimestamp(Utils.getUnixTimestamp());
            while (!bl.verifyDeclaredDifficulty()) {
                bl.setNonce(UUID.randomUUID().toString());
                bl.setTimestamp(Utils.getUnixTimestamp());
            }
            b.addBlock(bl);
            
            Block bl2 = Block.deserialize(bl.serialize(true));
            System.out.println(bl);
            System.out.println(new String(bl2.serialize(true)));
            System.out.println("Mining done...");

            // transaction signing test
            Transaction t = new Transaction(Transaction.Type.NORMAL);
            t.addInput(new Input(c.toString(), 25, 0));
            t.addOutput(new Output("test", 10));
            t.addOutput(new Output("test2", 15));
            t.sign(pairs);
            System.out.println(t);

            if (t.validate(b)) {
                System.out.println("ok!");
            } else {
                System.out.println("not ok!");
            }
            
            Transaction t2 = Transaction.deserialize(t.serialize(true));
            System.out.println(new String(t2.serialize(true)));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
