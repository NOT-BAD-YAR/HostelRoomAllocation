public class Block {
    private int blockId;
    private String blockName;
    private String description;

    public Block(int blockId, String blockName, String description) {
        this.blockId = blockId;
        this.blockName = blockName;
        this.description = description;
    }

    public int getBlockId() { return blockId; }
    public void setBlockId(int blockId) { this.blockId = blockId; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
