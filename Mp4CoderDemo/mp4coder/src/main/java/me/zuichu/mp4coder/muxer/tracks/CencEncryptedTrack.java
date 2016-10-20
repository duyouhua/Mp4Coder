package me.zuichu.mp4coder.muxer.tracks;

import me.zuichu.mp4coder.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import me.zuichu.mp4coder.muxer.Track;

import java.util.List;
import java.util.UUID;

/**
 * Track encrypted with common (CENC). ISO/IEC 23001-7.
 */
public interface CencEncryptedTrack extends Track {
    List<CencSampleAuxiliaryDataFormat> getSampleEncryptionEntries();

    UUID getDefaultKeyId();

    boolean hasSubSampleEncryption();
}
