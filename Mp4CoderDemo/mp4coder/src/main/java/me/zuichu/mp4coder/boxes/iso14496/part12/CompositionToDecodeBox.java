package me.zuichu.mp4coder.boxes.iso14496.part12;

import me.zuichu.mp4coder.support.AbstractFullBox;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <p>
 * The optional composition shift least greatest atom summarizes the calculated
 * minimum and maximum offsets between decode and composition time, as well as
 * the start and end times, for all samples. This allows a reader to determine
 * the minimum required time for decode to obtain proper presentation order without
 * needing to scan the sample table for the range of offsets. The type of the
 * composition shift least greatest atom is ‘cslg’.</p>
 */
public class CompositionToDecodeBox extends AbstractFullBox {
    public static final String TYPE = "cslg";
    // A 32-bit unsigned integer that specifies the calculated value.
    int compositionOffsetToDisplayOffsetShift;
    // A 32-bit signed integer that specifies the calculated value.
    int leastDisplayOffset;
    // A 32-bit signed integer that specifies the calculated value.
    int greatestDisplayOffset;
    //A 32-bit signed integer that specifies the calculated value.
    int displayStartTime;
    //A 32-bit signed integer that specifies the calculated value.
    int displayEndTime;

    public CompositionToDecodeBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return 24;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        compositionOffsetToDisplayOffsetShift = content.getInt();
        leastDisplayOffset = content.getInt();
        greatestDisplayOffset = content.getInt();
        displayStartTime = content.getInt();
        displayEndTime = content.getInt();
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.putInt(compositionOffsetToDisplayOffsetShift);
        byteBuffer.putInt(leastDisplayOffset);
        byteBuffer.putInt(greatestDisplayOffset);
        byteBuffer.putInt(displayStartTime);
        byteBuffer.putInt(displayEndTime);
    }


    public int getCompositionOffsetToDisplayOffsetShift() {
        return compositionOffsetToDisplayOffsetShift;
    }

    public void setCompositionOffsetToDisplayOffsetShift(int compositionOffsetToDisplayOffsetShift) {
        this.compositionOffsetToDisplayOffsetShift = compositionOffsetToDisplayOffsetShift;
    }

    public int getLeastDisplayOffset() {
        return leastDisplayOffset;
    }

    public void setLeastDisplayOffset(int leastDisplayOffset) {
        this.leastDisplayOffset = leastDisplayOffset;
    }

    public int getGreatestDisplayOffset() {
        return greatestDisplayOffset;
    }

    public void setGreatestDisplayOffset(int greatestDisplayOffset) {
        this.greatestDisplayOffset = greatestDisplayOffset;
    }

    public int getDisplayStartTime() {
        return displayStartTime;
    }

    public void setDisplayStartTime(int displayStartTime) {
        this.displayStartTime = displayStartTime;
    }

    public int getDisplayEndTime() {
        return displayEndTime;
    }

    public void setDisplayEndTime(int displayEndTime) {
        this.displayEndTime = displayEndTime;
    }
}
