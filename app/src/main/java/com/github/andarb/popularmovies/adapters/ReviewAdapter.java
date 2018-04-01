package com.github.andarb.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.andarb.popularmovies.data.Review;
import com.slackar.popularmovies.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private final Context mContext;
    private List<Review> mReviews;

    // Used to collapse and expand a review
    private Boolean mIsReviewCollapsed = true;
    private TextView mExpandedReviewTV;

    public ReviewAdapter(Context context) {
        mContext = context;
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.review_author_tv)
        TextView reviewAuthorTV;
        @BindView(R.id.review_content_tv)
        TextView reviewContentTV;


        /* Bind review TextViews, and set an OnClickListener on the list item */
        public ReviewViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        /* If there is a lot of text, expand or collapse content when the review is clicked */
        @Override
        public void onClick(View view) {
            if (reviewContentTV.getLineCount() < 4) return;

            if (mIsReviewCollapsed) {
                reviewContentTV.setMaxLines(Integer.MAX_VALUE);
                reviewContentTV.setEllipsize(null);
                mExpandedReviewTV = reviewContentTV;
                mIsReviewCollapsed = false;
            } else {
                reviewContentTV.setMaxLines(4);
                reviewContentTV.setEllipsize(TextUtils.TruncateAt.END);
                mIsReviewCollapsed = true;
            }
        }
    }

    /* Inflate list item and intialize with it a new viewholder */
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.review_list_item, parent, false);

        // Automatically collapse an expanded review when scrolling to the next review
        parent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (!mIsReviewCollapsed) {
                            mExpandedReviewTV.setMaxLines(4);
                            mExpandedReviewTV.setEllipsize(TextUtils.TruncateAt.END);
                            mIsReviewCollapsed = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                }

                return false;
            }
        });

        return new ReviewAdapter.ReviewViewHolder(view);
    }

    /* Set review content and author */
    @Override
    public void onBindViewHolder(ReviewAdapter.ReviewViewHolder holder, int position) {
        holder.reviewContentTV.setText(mReviews.get(position).getContent());
        holder.reviewAuthorTV.setText(mReviews.get(position).getAuthor());
    }

    /* Number of reviews retrieved for this movie */
    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    /* Sets a list of reviews (retrieved and parsed earlier from 'themoviedb')
     to be used by adapter */
    public void setReviews(List<Review> reviews) {
        mReviews = reviews;
    }
}
