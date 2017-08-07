package io.github.flowfree.popularmovies;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.github.flowfree.popularmovies.data.ReviewInfo;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewListViewHolder> {

    private List<ReviewInfo> mReviewsData;
    private Context mContext;

    public class ReviewListViewHolder extends RecyclerView.ViewHolder {
        private TextView mReviewAuthor;
        private TextView mReviewContent;

        public ReviewListViewHolder(View view) {
            super(view);
            mReviewAuthor = view.findViewById(R.id.tv_review_author);
            mReviewContent = view.findViewById(R.id.tv_review_content);
        }
    }

    public ReviewListAdapter() {
    }

    @Override
    public ReviewListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.review_list_item, parent, false);
        return new ReviewListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewListViewHolder holder, int position) {
        ReviewInfo reviewInfo = mReviewsData.get(position);
        holder.mReviewAuthor.setText(reviewInfo.author);
        holder.mReviewContent.setText(reviewInfo.content);
    }

    @Override
    public int getItemCount() {
        return mReviewsData == null ? 0 : mReviewsData.size();
    }

    public void setReviewsData(List<ReviewInfo> reviewsData) {
        mReviewsData = reviewsData;
        if (mReviewsData != null) {
            notifyDataSetChanged();
        }
    }
}
