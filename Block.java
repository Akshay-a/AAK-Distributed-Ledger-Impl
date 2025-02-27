import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Date;

public class Block implements Serializable {
    private static final long serialVersionUID = 1L; // For version control of serialization
    private String data;
    private String previousHash;
    private long timestamp;
    private String hash;

    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        try {
            String input=null;
            if(data.equals("Genesis Block")){
                input = previousHash  + data;
            }
            else{
                input = previousHash + Long.toString(timestamp) + data;
            }
             
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    // Getters
    public String getData() { return data; }
    public String getPreviousHash() { return previousHash; }
    public String getHash() { return hash; }
    public long getTimestamp() { return timestamp; }
}