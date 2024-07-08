package com.androidplot.xy;

public class BlockAutoPan{
    private boolean block;
    public BlockAutoPan(boolean block)
    {
        this.block = block;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }
}
