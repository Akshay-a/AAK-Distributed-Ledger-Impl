import java.util.ArrayList;
import java.util.List;

public class Ledger {
    private List<Block> chain;

    public Ledger() {
        this.chain = new ArrayList<>();
        // Genesis block
        chain.add(new Block("Genesis Block", "0"));
    }

    public void addBlock(Block newBlock) {
        if (isValidNewBlock(newBlock)) {
            chain.add(newBlock);
            System.out.println("Block added: " + newBlock.getHash());
        } else {
            System.out.println("Invalid block rejected");
        }
    }

    public boolean isValidNewBlock(Block newBlock) {
        Block lastBlock = chain.get(chain.size() - 1);
        boolean chainValid = lastBlock.getHash().equals(newBlock.getPreviousHash());
        boolean hashValid = newBlock.getHash().equals(newBlock.calculateHash());
    
        if (!chainValid) {
            System.out.println("Chain validation failed: lastBlock.hash=" + lastBlock.getHash() + 
                              ", newBlock.previousHash=" + newBlock.getPreviousHash());
        }
        if (!hashValid) {
            System.out.println("Hash validation failed: storedHash=" + newBlock.getHash() + 
                              ", computedHash=" + newBlock.calculateHash());
        }
    
        return chainValid && hashValid;
    }

    public void replaceChain(List<Block> newChain) {
        if (newChain.size() <= chain.size()) {
            System.out.println("Received chain is not longer than current chain, ignoring.");
            return;
        }
        // Validate the new chain
        for (int i = 1; i < newChain.size(); i++) {
            Block current = newChain.get(i);
            Block previous = newChain.get(i - 1);
            if (!current.getPreviousHash().equals(previous.getHash()) ||
                !current.getHash().equals(current.calculateHash())) {
                System.out.println("Invalid chain received during sync, rejecting.");
                return;
            }
        }
        this.chain = new ArrayList<>(newChain);
        System.out.println("Chain replaced successfully.");
    }

    public List<Block> getChain() { return chain; }
}