package cz.fmo.data;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cz.fmo.Lib;
import cz.fmo.util.Config;

/**
 * Latest detected tracks that are meant to be kept on screen to allow inspection by the user.
 */
public class TrackSet {
    private static final int FRAMES_UNTIL_OLD_TRACK_REMOVAL = 7;
    private static final int NUM_TRACKS = 2;
    private final Object mLock = new Object();
    private final ArrayList<Track> mTracks = new ArrayList<>();
    private Config mConfig = null;
    private SparseArray<Track> mCurrentTrackMap = new SparseArray<>();
    private SparseArray<Track> mPreviousTrackMap = new SparseArray<>();
    private int mWidth = 1;  // width of the source image (not necessarily the screen width)
    private int mHeight = 1; // height of the source image (not necessarily the screen height)

    private TrackSet() {}

    public static TrackSet getInstance() {
        return SingletonHolder.instance;
    }

    public void setConfig(Config config) {
        synchronized (mLock) {
            mConfig = config;
            clear();
        }
    }

    /**
     * Adds detections to the correct tracks. If there is no predecessor for a given detection, a
     * new track is created.
     *
     * @param width  width of the source image (not the screen)
     * @param height height of the source image (not the screen)
     */
    public void addDetections(Lib.Detection[] detections, int width, int height, long detectionTime) {
        synchronized (mLock) {
            if (mConfig == null) return;
            mWidth = width;
            mHeight = height;
            // swap the maps
            {
                SparseArray<Track> temp = mCurrentTrackMap;
                mCurrentTrackMap = mPreviousTrackMap;
                mPreviousTrackMap = temp;
            }

            mCurrentTrackMap.clear();
            this.filterOutOldTracks(detectionTime);
            for (Lib.Detection detection : detections) {
                if (detection.id < 0) {
                    throw new RuntimeException("ID of a detection not specified");
                }

                // get the track of the predecessor
                Track track = mPreviousTrackMap.get(detection.predecessorId);
                if (track == null) {
                    // no predecessor/track not found: make a new track
                    track = new Track(mConfig);

                    // add the track to the list
                    mTracks.add(track);
                }

                detection.predecessor = track.getLatest();
                track.setLatest(detection, detectionTime);
                mCurrentTrackMap.put(detection.id, track);
            }
        }
    }

    public List<Track> getTracks() {return Collections.unmodifiableList(mTracks);}

    public Track getTrackWithLatestDetection() {
        int index = 0;
        long latestDetectionTime = Long.MAX_VALUE;
        for (int i = 0; i<this.getTracks().size(); i++) {
            Track t = this.getTracks().get(i);
            if(t.getLastDetectionTime() < latestDetectionTime) {
                latestDetectionTime = t.getLastDetectionTime();
                index = i;
            }
        }
        return this.getTracks().get(index);
    }

    public void clear() {
        synchronized (mLock) {
            mTracks.clear();
            mPreviousTrackMap.clear();
            mCurrentTrackMap.clear();
        }
    }

    private void filterOutOldTracks(long currentTime) {
        // filter out tracks which were not updated after n Frames (n=FRAMES_UNTIL_OLD_TRACK_REMOVAL)
        if(this.getTracks().size()>1) {
            long maxTimeDeltaForOldestTrack = (long)(FRAMES_UNTIL_OLD_TRACK_REMOVAL/ mConfig.getFrameRate() * Math.pow(1000,3));
            Iterator<Track> it = mTracks.iterator();
            while(it.hasNext()) {
                Track t = it.next();
                // if a track hasn't been updated in n Frames ...
                if (t.getLastDetectionTime()<=currentTime-maxTimeDeltaForOldestTrack) {
                    // delete it
                    it.remove();
                    break;
                }
            }
        }
    }

    private static class SingletonHolder {
        static final TrackSet instance = new TrackSet();
    }
}
