/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.zuichu.mp4coder.muxer.tracks;

import me.zuichu.mp4coder.muxer.TrackMetaData;
import me.zuichu.mp4coder.boxes.iso14496.part12.CompositionTimeToSample;
import me.zuichu.mp4coder.boxes.iso14496.part12.SampleDependencyTypeBox;
import me.zuichu.mp4coder.boxes.iso14496.part12.SampleDescriptionBox;
import me.zuichu.mp4coder.boxes.iso14496.part12.SubSampleInformationBox;
import me.zuichu.mp4coder.boxes.samplegrouping.GroupEntry;
import me.zuichu.mp4coder.muxer.Edit;
import me.zuichu.mp4coder.muxer.Sample;
import me.zuichu.mp4coder.muxer.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Changes the timescale of a track by wrapping the track.
 */
public class MultiplyTimeScaleTrack implements Track {
    Track source;
    private int timeScaleFactor;

    public MultiplyTimeScaleTrack(Track source, int timeScaleFactor) {
        this.source = source;
        this.timeScaleFactor = timeScaleFactor;
    }

    static List<CompositionTimeToSample.Entry> adjustCtts(List<CompositionTimeToSample.Entry> source, int timeScaleFactor) {
        if (source != null) {
            List<CompositionTimeToSample.Entry> entries2 = new ArrayList<CompositionTimeToSample.Entry>(source.size());
            for (CompositionTimeToSample.Entry entry : source) {
                entries2.add(new CompositionTimeToSample.Entry(entry.getCount(), timeScaleFactor * entry.getOffset()));
            }
            return entries2;
        } else {
            return null;
        }
    }

    public void close() throws IOException {
        source.close();
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return source.getSampleDescriptionBox();
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return adjustCtts(source.getCompositionTimeEntries(), timeScaleFactor);
    }

    public long[] getSyncSamples() {
        return source.getSyncSamples();
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return source.getSampleDependencies();
    }

    public TrackMetaData getTrackMetaData() {
        TrackMetaData trackMetaData = (TrackMetaData) source.getTrackMetaData().clone();
        trackMetaData.setTimescale(source.getTrackMetaData().getTimescale() * this.timeScaleFactor);
        return trackMetaData;
    }

    public String getHandler() {
        return source.getHandler();
    }

    public List<Sample> getSamples() {
        return source.getSamples();
    }

    public long[] getSampleDurations() {
        long[] scaled = new long[source.getSampleDurations().length];

        for (int i = 0; i < source.getSampleDurations().length; i++) {
            scaled[i] = source.getSampleDurations()[i] * timeScaleFactor;
        }
        return scaled;
    }

    public SubSampleInformationBox getSubsampleInformationBox() {
        return source.getSubsampleInformationBox();
    }

    public long getDuration() {
        return source.getDuration() * timeScaleFactor;
    }

    @Override
    public String toString() {
        return "MultiplyTimeScaleTrack{" +
                "source=" + source +
                '}';
    }

    public String getName() {
        return "timscale(" + source.getName() + ")";
    }

    public List<Edit> getEdits() {
        return source.getEdits();
    }

    public Map<GroupEntry, long[]> getSampleGroups() {
        return source.getSampleGroups();
    }
}
