package com.github.andarb.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.andarb.popularmovies.data.Video;
import com.slackar.popularmovies.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.TrailerViewHolder> {
    private final Context mContext;
    private List<Video> mVideos;

    // Video thumbnail url details
    private static final String THUMBNAIL_BASE_URL = "http://img.youtube.com/vi/";
    private static final String THUMBNAIL_SIZE = "/mqdefault.jpg";

    // Video video url
    private static final String TRAILER_BASE_URL = "https://www.youtube.com/watch?v=";

    public VideoAdapter(Context context) {
        mContext = context;
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.trailer_iv)
        ImageView trailerIV;
        @BindView(R.id.trailer_name_tv)
        TextView trailerNameTV;

        /* Bind trailer Views, and set an OnClickListener on the list item */
        public TrailerViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        /* When one of the trailers is clicked, find an app to play it */
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            String trailerKey = mVideos.get(position).getKey();

            Intent playTrailerIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(TRAILER_BASE_URL + trailerKey));

            if (playTrailerIntent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(playTrailerIntent);
            }
        }
    }

    /* Inflate list item and intialize with it a new viewholder */
    @Override
    public VideoAdapter.TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.trailer_list_item, parent, false);

        return new VideoAdapter.TrailerViewHolder(view);
    }

    /* Download trailer thumbnail, and set other details */
    @Override
    public void onBindViewHolder(VideoAdapter.TrailerViewHolder holder, int position) {
        String trailerUrlKey = mVideos.get(position).getKey();
        String thumbnailUrl = THUMBNAIL_BASE_URL + trailerUrlKey + THUMBNAIL_SIZE;

        Picasso.with(mContext).load(thumbnailUrl).into(holder.trailerIV);
        holder.trailerNameTV.setText(mVideos.get(position).getName());
    }

    /* Number of trailers retrieved for this movie */
    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    /* Sets a list of videos (retrieved and parsed earlier from 'themoviedb')
     to be used by adapter */
    public void setVideos(List<Video> videos) {
        mVideos = videos;
    }
}
