package com.peterlaurence.trekme.core.map.maploader.tasks;

import android.os.AsyncTask;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.peterlaurence.trekme.core.map.Map;
import com.peterlaurence.trekme.core.map.gson.MapGson;
import com.peterlaurence.trekme.core.map.maploader.MapLoader;
import com.peterlaurence.trekme.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Searches for maps on the SD card (json files).
 * Parses the json files to, e.g, process calibration information.
 *
 * @author peterLaurence on 30/04/17.
 */
public class MapUpdateTask extends AsyncTask<File, Void, Void> {
    private MapLoader.MapListUpdateListener mListener;
    private Gson mGson;
    private List<Map> mMapList;

    private List<File> mapFilesFoundList;
    private static final int MAX_RECURSION_DEPTH = 6;
    private static final String TAG = "MapUpdateTask";

    public MapUpdateTask(@Nullable MapLoader.MapListUpdateListener listener,
                         Gson gson,
                         List<Map> mapList) {
        super();
        mListener = listener;
        mGson = gson;
        mMapList = mapList;
        mapFilesFoundList = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(File... dirs) {
        /* Search for json files */
        for (File dir : dirs) {
            findMaps(dir, 1);
        }

        /* Now parse the json files found */
        for (File f : mapFilesFoundList) {
            /* Get json file content as String */
            String jsonString;
            try {
                jsonString = FileUtils.getStringFromFile(f);
            } catch (Exception e) {
                // Error while decoding the json file
                Log.e(TAG, e.getMessage(), e);
                continue;
            }

            try {
                /* json deserialization */
                MapGson mapGson = mGson.fromJson(jsonString, MapGson.class);

                /* Map creation */
                Map map = mapGson.thumbnail == null ? new Map(mapGson, f, null) :
                        new Map(mapGson, f, new File(f.getParent(), mapGson.thumbnail));

                /* Calibration */
                map.calibrate();

                /* Set BitMapProvider */
                map.setBitmapProvider(MapLoader.makeBitmapProvider(map));

                mMapList.add(map);
            } catch (JsonSyntaxException | NullPointerException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        return null;
    }

    private void findMaps(File root, int depth) {
        if (depth > MAX_RECURSION_DEPTH) return;

        /* Don't allow nested maps */
        File rootJsonFile = new File(root, MapLoader.MAP_FILE_NAME);
        if (rootJsonFile.exists() && rootJsonFile.isFile()) {
            mapFilesFoundList.add(rootJsonFile);
            return;
        }

        File[] list = root.listFiles();
        if (list == null) {
            return;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                findMaps(f, depth + 1);
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mListener != null) {
            mListener.onMapListUpdate(mMapList.size() > 0);
        }
    }
}
