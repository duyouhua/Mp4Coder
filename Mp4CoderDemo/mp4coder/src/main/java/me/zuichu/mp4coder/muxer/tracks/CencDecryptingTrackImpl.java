package me.zuichu.mp4coder.muxer.tracks;

import me.zuichu.mp4coder.Box;
import me.zuichu.mp4coder.boxes.iso14496.part12.OriginalFormatBox;
import me.zuichu.mp4coder.boxes.iso14496.part12.SchemeTypeBox;
import me.zuichu.mp4coder.boxes.sampleentry.AudioSampleEntry;
import me.zuichu.mp4coder.boxes.samplegrouping.CencSampleEncryptionInformationGroupEntry;
import me.zuichu.mp4coder.muxer.AbstractTrack;
import me.zuichu.mp4coder.muxer.Track;
import me.zuichu.mp4coder.muxer.TrackMetaData;
import me.zuichu.mp4coder.tools.ByteBufferByteChannel;
import me.zuichu.mp4coder.tools.Path;
import me.zuichu.mp4coder.IsoFile;
import me.zuichu.mp4coder.boxes.iso14496.part12.SampleDescriptionBox;
import me.zuichu.mp4coder.boxes.sampleentry.VisualSampleEntry;
import me.zuichu.mp4coder.boxes.samplegrouping.GroupEntry;
import me.zuichu.mp4coder.muxer.Sample;
import me.zuichu.mp4coder.muxer.samples.CencDecryptingSampleList;
import me.zuichu.mp4coder.tools.RangeStartMap;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.*;

public class CencDecryptingTrackImpl extends AbstractTrack {
    CencDecryptingSampleList samples;
    Track original;
    RangeStartMap<Integer, SecretKey> indexToKey = new RangeStartMap<Integer, SecretKey>();

    public CencDecryptingTrackImpl(CencEncryptedTrack original, SecretKey sk) {
        this(original, Collections.singletonMap(original.getDefaultKeyId(), sk));

    }

    public CencDecryptingTrackImpl(CencEncryptedTrack original, Map<UUID, SecretKey> keys) {
        super("dec(" + original.getName() + ")");
        this.original = original;
        SchemeTypeBox schm = Path.getPath(original.getSampleDescriptionBox(), "enc./sinf/schm");
        assert schm != null;
        if (!("cenc".equals(schm.getSchemeType()) ||
                "cbc1".equals(schm.getSchemeType()) ||
                "piff".equals(schm.getSchemeType()))) {
            throw new RuntimeException("You can only use the CencDecryptingTrackImpl with CENC (cenc or cbc1) encrypted tracks");
        }

        List<CencSampleEncryptionInformationGroupEntry> groupEntries = new ArrayList<CencSampleEncryptionInformationGroupEntry>();
        for (Map.Entry<GroupEntry, long[]> groupEntry : original.getSampleGroups().entrySet()) {
            if (groupEntry.getKey() instanceof CencSampleEncryptionInformationGroupEntry) {
                groupEntries.add((CencSampleEncryptionInformationGroupEntry) groupEntry.getKey());
            } else {
                getSampleGroups().put(groupEntry.getKey(), groupEntry.getValue());
            }
        }


        int lastSampleGroupDescriptionIndex = -1;
        for (int i = 0; i < original.getSamples().size(); i++) {
            int index = 0;
            for (int j = 0; j < groupEntries.size(); j++) {
                GroupEntry groupEntry = groupEntries.get(j);
                long[] sampleNums = original.getSampleGroups().get(groupEntry);
                if (Arrays.binarySearch(sampleNums, i) >= 0) {
                    index = j + 1;
                }
            }
            if (lastSampleGroupDescriptionIndex != index) {
                if (index == 0) {
                    // if default_encrypted == false then keys.get(original.getDefaultKeyId()) == null
                    indexToKey.put(i, keys.get(original.getDefaultKeyId()));
                } else {
                    if (groupEntries.get(index - 1).isEncrypted()) {
                        SecretKey sk = keys.get(groupEntries.get(index - 1).getKid());
                        if (sk == null) {
                            throw new RuntimeException("Key " + groupEntries.get(index - 1).getKid() + " was not supplied for decryption");
                        }
                        indexToKey.put(i, sk);
                    } else {
                        indexToKey.put(i, null);
                    }
                }
                lastSampleGroupDescriptionIndex = index;
            }
        }


        samples = new CencDecryptingSampleList(indexToKey, original.getSamples(), original.getSampleEncryptionEntries(), schm.getSchemeType());
    }

    public void close() throws IOException {
        original.close();
    }

    public long[] getSyncSamples() {
        return original.getSyncSamples();
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        OriginalFormatBox frma = Path.getPath(original.getSampleDescriptionBox(), "enc./sinf/frma");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SampleDescriptionBox stsd;
        try {
            original.getSampleDescriptionBox().getBox(Channels.newChannel(baos));
            stsd = (SampleDescriptionBox) new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray()))).getBoxes().get(0);
        } catch (IOException e) {
            throw new RuntimeException("Dumping stsd to memory failed");
        }

        if (stsd.getSampleEntry() instanceof AudioSampleEntry) {
            ((AudioSampleEntry) stsd.getSampleEntry()).setType(frma.getDataFormat());
        } else if (stsd.getSampleEntry() instanceof VisualSampleEntry) {
            ((VisualSampleEntry) stsd.getSampleEntry()).setType(frma.getDataFormat());
        } else {
            throw new RuntimeException("I don't know " + stsd.getSampleEntry().getType());
        }
        List<Box> nuBoxes = new LinkedList<Box>();
        for (Box box : stsd.getSampleEntry().getBoxes()) {
            if (!box.getType().equals("sinf")) {
                nuBoxes.add(box);
            }
        }
        stsd.getSampleEntry().setBoxes(nuBoxes);
        return stsd;
    }


    public long[] getSampleDurations() {
        return original.getSampleDurations();
    }

    public TrackMetaData getTrackMetaData() {
        return original.getTrackMetaData();
    }

    public String getHandler() {
        return original.getHandler();
    }

    public List<Sample> getSamples() {
        return samples;
    }

}
