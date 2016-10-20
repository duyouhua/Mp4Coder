package me.zuichu.mp4coder.boxes.iso14496.part12;


import me.zuichu.mp4coder.support.AbstractBox;
import me.zuichu.mp4coder.tools.IsoTypeReader;
import me.zuichu.mp4coder.tools.IsoTypeWriter;
import me.zuichu.mp4coder.tools.Mp4Arrays;

import java.nio.ByteBuffer;


/**
 * This box provides a reference from the containing track to another track in the presentation. These references
 * are typed. A 'hint' reference links from the containing hint track to the media data that it hints. A content
 * description reference 'cdsc' links a descriptive or metadata track to the content which it describes. The
 * 'hind' dependency indicates that the referenced track(s)may contain media data required for decoding of
 * the track containing the track reference. The referenced tracks shall be hint tracks. The 'hind' dependency
 * can, for example, be used for indicating the dependencies between hint tracks documenting layered IP
 * multicast over RTP.
 * Exactly one Track Reference Box can be contained within the Track Box.
 * If this box is not present, the track is not referencing anyother track in any way. The reference array is sized
 * to fill the reference type box.
 */
public class TrackReferenceTypeBox extends AbstractBox {

    long[] trackIds = new long[0];

    // ‘hint’  the referenced track(s) contain the original media for this hint track
    // ‘cdsc‘  this track describes the referenced track.
    // 'hind'  this track depends on the referenced hint track, i.e., it should only be used if the referenced hint track is used.
    // 'vdep'  this track contains auxiliary depth video information for the referenced video track
    // 'vplx'  this track contains auxiliary parallax video information for the referenced video track

    public TrackReferenceTypeBox(String type) {
        super(type);
    }

    @Override
    protected long getContentSize() {
        return trackIds.length * 4;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        for (long trackId : trackIds) {
            IsoTypeWriter.writeUInt32(byteBuffer, trackId);
        }
    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        while (content.remaining() >= 4) {
            trackIds = Mp4Arrays.copyOfAndAppend(trackIds, new long[]{IsoTypeReader.readUInt32(content)});
        }

    }

    public long[] getTrackIds() {
        return trackIds;
    }

    public void setTrackIds(long[] trackIds) {
        this.trackIds = trackIds;
    }
}
