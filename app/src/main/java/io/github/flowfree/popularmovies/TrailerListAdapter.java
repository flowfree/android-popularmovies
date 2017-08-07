package io.github.flowfree.popularmovies;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.github.flowfree.popularmovies.data.TrailerInfo;

public class TrailerListAdapter extends RecyclerView.Adapter<TrailerListAdapter.TrailerListViewHolder> {

    private List<TrailerInfo> mTrailersData;
    private Context mContext;

    private final TrailerListClickHandler mClickHandler;

    public class TrailerListViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private TextView mTrailerTitle;

        public TrailerListViewHolder(View view) {
            super(view);
            mTrailerTitle = view.findViewById(R.id.tv_trailer_title);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mClickHandler.onClickTrailerItem(position);
        }
    }

    public interface TrailerListClickHandler {
        void onClickTrailerItem(int position);
    }

    public TrailerListAdapter(TrailerListClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public TrailerListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerListViewHolder holder, int position) {
        TrailerInfo trailerInfo = mTrailersData.get(position);
        holder.mTrailerTitle.setText("\u25B6 " + trailerInfo.title);
    }

    @Override
    public int getItemCount() {
        return mTrailersData == null ? 0 : mTrailersData.size();
    }

    public void setTrailersData(List<TrailerInfo> trailersData) {
        mTrailersData = trailersData;
        if (mTrailersData != null) {
            notifyDataSetChanged();
        }
    }
}
