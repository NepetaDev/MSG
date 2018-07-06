package cf.ominous.msg.blockchain;

import org.apache.commons.codec.digest.DigestUtils;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public final class Utils {
    private static final BigInteger MAX_TARGET = new BigInteger("1346097011034941016471924199027519497880772611882819598800340923050542647945");
    private Utils() {
    }
    
    public static BigInteger getMaxTarget() {
        return MAX_TARGET;
    }

    public static String byteArrayToString(byte[] data) {
        return new String(Base64.encodeBase64(data));
    }

    public static byte[] stringToByteArray(String data) {
        return Base64.decodeBase64(data);
    }

    public static byte[] publicKeyHash(PublicKey publicKey) {
        return Utils.hash(publicKey.getEncoded());
    }

    public static byte[] serializePublicKey(ECPublicKey publicKey) {
        try {
            return publicKey.getEncoded();
        } catch (Exception ex) {
            return null;
        }
    }

    public static ECPublicKey deserializePublicKey(byte[] data) {
        try {
            return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(data));
        } catch (Exception ex) {
            return null;
        }
    }

    public static byte[] serializePrivateKey(ECPrivateKey privateKey) {
        try {
            return privateKey.getEncoded();
        } catch (Exception ex) {
            return null;
        }
    }

    public static ECPrivateKey deserializePrivateKey(byte[] data) {
        try {
            return (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(data));
        } catch (Exception ex) {
            return null;
        }
    }

    public static String bigIntToString(BigInteger bigInteger) {
        return Utils.byteArrayToString(bigInteger.toByteArray());
    }

    public static BigInteger bigIntFromString(String string) {
        return new BigInteger(Utils.stringToByteArray(string));
    }

    public static byte[] serializeKeyPair(KeyPair keyPair) {
        Bencode bencode = new Bencode();
        List<Object> list = new ArrayList<Object>();

        list.add(Utils.byteArrayToString(Utils.serializePublicKey((ECPublicKey) keyPair.getPublic())));
        list.add(Utils.byteArrayToString(Utils.serializePrivateKey((ECPrivateKey) keyPair.getPrivate())));

        return bencode.encode(list);
    }

    public static KeyPair deserializeKeyPair(byte[] data) {
        try {
            Bencode bencode = new Bencode();
            List<Object> list = bencode.decode(data, Type.LIST);
            return new KeyPair(Utils.deserializePublicKey(Utils.stringToByteArray(list.get(0).toString())),
                    Utils.deserializePrivateKey(Utils.stringToByteArray(list.get(1).toString())));
        } catch (Exception ex) {
            return null;
        }
    }

    public static byte[] hash(byte[] data) {
        return DigestUtils.sha256(DigestUtils.sha256(data));
    }

    public static boolean verify(byte[] data, byte[] signedData, PublicKey publicKey) {
        try {
            Signature dsa = Signature.getInstance("SHA1withECDSA");
            dsa.initVerify(publicKey);
            dsa.update(data);
            return dsa.verify(signedData);
        } catch (Exception ex) {
            return false;
        }
    }

    public static byte[] sign(byte[] data, PrivateKey privateKey) {
        try {
            Signature dsa = Signature.getInstance("SHA1withECDSA");
            dsa.initSign(privateKey);
            dsa.update(data);
            return dsa.sign();
        } catch (Exception ex) {
            return null;
        }
    }

    public static long getUnixTimestamp() {
        return (long) (System.currentTimeMillis() / 1000L);
    }
}
