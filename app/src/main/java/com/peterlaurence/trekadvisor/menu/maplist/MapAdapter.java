package com.peterlaurence.trekadvisor.menu.maplist;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.peterlaurence.trekadvisor.R;
import com.peterlaurence.trekadvisor.core.map.Map;
import com.peterlaurence.trekadvisor.core.map.maploader.MapLoader;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Adapter to provide access to the data set (here a list of {@link Map}).
 * <p/>
 *
 * @author peterLaurence on 26/12/15.
 */
public class MapAdapter extends RecyclerView.Adapter<MapAdapter.MapViewHolder> implements MapLoader.MapListUpdateListener {
    private List<Map> maps;
    private MapSelectionListener mMapSelectionListener;
    private MapSettingsListener mMapSettingsListener;

    private int selectedMapIndex = -1;
    private int previousSelectedMapIndex = -1;

    private int mColorAccent;
    private int mColorWhiteText;
    private int mColorBlackText;

    @Override
    public void onMapListUpdate(boolean mapsFound) {
        if (mapsFound) {
            maps = MapLoader.getInstance().getMaps();
            notifyDataSetChanged();
        }
    }

    /**
     * When an item gets selected, the {@link MapSelectionListener} is called with the corresponding
     * {@link Map}.
     */
    public interface MapSelectionListener {
        void onMapSelected(Map m);
    }

    /**
     * When the settings button of an item is clicked, the {@link MapSettingsListener} is called
     * with the corresponding {@link Map}.
     */
    public interface MapSettingsListener {
        void onMapSettings(Map m);
    }

    /**
     * The view for each {@link Map}
     */
    public static class MapViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView mapName;
        TextView calibrationStatus;
        ImageView mapImage;
        Button editButton;

        public MapViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cv);
            mapName = (TextView) itemView.findViewById(R.id.map_name);
            calibrationStatus = (TextView) itemView.findViewById(R.id.map_calibration_status);
            mapImage = (ImageView) itemView.findViewById(R.id.map_preview_image);
            editButton = (Button) itemView.findViewById(R.id.map_edit_btn);
        }
    }

    public MapAdapter(@Nullable List<Map> maps, MapSelectionListener mapSelectionListener,
                      MapSettingsListener mapSettingsListener) {
        this.maps = maps;
        mMapSelectionListener = mapSelectionListener;
        mMapSettingsListener = mapSettingsListener;
    }

    /**
     * Simple implementation of a toggle selection. When an item is clicked, we change its
     * background and we remember his index. When another item is clicked, the background of the
     * first item is set to its original state.
     *
     * @param position index of the selected view
     */
    private void updateSelectionColor(int position) {
        selectedMapIndex = position;
        notifyItemChanged(position);
        if (previousSelectedMapIndex != -1) {
            notifyItemChanged(previousSelectedMapIndex);
        }
        previousSelectedMapIndex = position;
    }

    @Override
    public MapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();
        View v = LayoutInflater.from(ctx).inflate(R.layout.map_row, parent, false);
        mColorAccent = ctx.getColor(R.color.colorAccent);
        mColorWhiteText = ctx.getColor(R.color.colorPrimaryTextWhite);
        mColorBlackText = ctx.getColor(R.color.colorPrimaryTextBlack);

        return new MapViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MapViewHolder holder, int position) {
        final Map map = maps.get(position);
        holder.mapImage.setImageBitmap(map.getImage());
        holder.mapName.setText(map.getName());
        holder.calibrationStatus.setText(map.getDescription());

        if (holder.getLayoutPosition() == selectedMapIndex) {
            holder.cardView.setCardBackgroundColor(mColorAccent);
            holder.mapName.setTextColor(mColorWhiteText);
            holder.editButton.setTextColor(mColorWhiteText);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.mapName.setTextColor(mColorBlackText);
            holder.editButton.setTextColor(mColorAccent);
        }
        switch (map.getCalibrationStatus()) {
            case OK:
                holder.calibrationStatus.setText(R.string.calibration_status_ok);
                break;
            case NONE:
                holder.calibrationStatus.setText(R.string.calibration_status_none);
                break;
            case ERROR:
                holder.calibrationStatus.setText(R.string.calibration_status_error);
                break;
        }

        /* Set click listeners */
        holder.itemView.setOnClickListener(new MapViewHolderClickListener(holder, this));
        holder.editButton.setOnClickListener(new SettingsButtonClickListener(holder, this));
    }

    @Override
    public int getItemCount() {
        return maps == null ? 0 : maps.size();
    }

    /**
     * The click listener for a settings button of a {@link MapViewHolder}
     * It has a reference to the {@link MapAdapter} as it needs to access the {@link Map} container.
     */
    private static class SettingsButtonClickListener implements View.OnClickListener {
        WeakReference<MapViewHolder> mMapViewHolderWeakReference;
        WeakReference<MapAdapter> mMapAdapterWeakReference;

        SettingsButtonClickListener(MapViewHolder mapViewHolder, MapAdapter mapAdapter) {
            mMapViewHolderWeakReference = new WeakReference<>(mapViewHolder);
            mMapAdapterWeakReference = new WeakReference<>(mapAdapter);
        }

        @Override
        public void onClick(View v) {
            if (mMapAdapterWeakReference != null && mMapViewHolderWeakReference != null) {
                MapAdapter mapAdapter = mMapAdapterWeakReference.get();
                MapViewHolder mapViewHolder = mMapViewHolderWeakReference.get();

                if (mapViewHolder != null && mapAdapter != null) {
                    Map map = mapAdapter.maps.get(mapViewHolder.getAdapterPosition());
                    if (mapAdapter.mMapSettingsListener != null) {
                        mapAdapter.mMapSettingsListener.onMapSettings(map);
                    }
                }
            }
        }
    }

    /**
     * The click listener for a {@link MapViewHolder}.
     * It has a reference to the {@link MapAdapter} as it needs to access the {@link Map} container
     * and call some methods.
     */
    private static class MapViewHolderClickListener implements View.OnClickListener {
        WeakReference<MapViewHolder> mMapViewHolderWeakReference;
        WeakReference<MapAdapter> mMapAdapterWeakReference;

        MapViewHolderClickListener(MapViewHolder mapViewHolder, MapAdapter mapAdapter) {
            mMapViewHolderWeakReference = new WeakReference<>(mapViewHolder);
            mMapAdapterWeakReference = new WeakReference<>(mapAdapter);
        }

        @Override
        public void onClick(View v) {
            if (mMapAdapterWeakReference != null && mMapViewHolderWeakReference != null) {
                MapAdapter mapAdapter = mMapAdapterWeakReference.get();
                MapViewHolder mapViewHolder = mMapViewHolderWeakReference.get();
                if (mapViewHolder != null && mapAdapter != null) {
                    int position = mapViewHolder.getAdapterPosition();

                    // Toggle background color
                    mapAdapter.updateSelectionColor(position);

                    // Call the listener for Map selection
                    Map map = mapAdapter.maps.get(position);
                    mapAdapter.mMapSelectionListener.onMapSelected(map);
                }
            }
        }
    }
}
