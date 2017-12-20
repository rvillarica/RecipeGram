package com.example.romvillarica.recipegram;

/**
 * Created by Rom Villarica on 11/11/2017.
 */

import java.util.List;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.v4.content.ContextCompat.startActivity;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<ListItem> values;
    String username;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        @BindView(R.id.user)
        TextView textUser;
        @BindView(R.id.date)
        TextView textDate;
        @BindView(R.id.description)
        TextView textDesc;
        @BindView(R.id.picture)
        ImageView picture;
        @BindView(R.id.view_recipe_button)
        Button viewRecipeButton;
        @BindView(R.id.rating_bar)
        RatingBar ratingBar;
        @BindView(R.id.delete_button)
        Button deleteButton;

        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            ButterKnife.bind(this, v);
        }
    }

    public void add(int position, ListItem item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    public void delete(int position) {
        final ListItem l = values.get(position);
        final int x = position;
        String url1 = l.getImage1Url();
        url1 = url1.replace("http://ec2-54-210-25-34.compute-1.amazonaws.com/879a6589b75ce798f587e68ab76c8237a9/GET/","");
        //remove the image
        RedisService.getService().deletePost(url1).enqueue(new Callback<RedisService.DelResponse>() {
            @Override
            public void onResponse(Call<RedisService.DelResponse> call, Response<RedisService.DelResponse> response) {
            }
            @Override
            public void onFailure(Call<RedisService.DelResponse> call, Throwable t) {
            }
        });
        String url2 = l.getImage2Url();
        url2 = url2.replace("http://ec2-54-210-25-34.compute-1.amazonaws.com/879a6589b75ce798f587e68ab76c8237a9/GET/","");
        //remove the image
        RedisService.getService().deletePost(url2).enqueue(new Callback<RedisService.DelResponse>() {
            @Override
            public void onResponse(Call<RedisService.DelResponse> call, Response<RedisService.DelResponse> response) {
            }
            @Override
            public void onFailure(Call<RedisService.DelResponse> call, Throwable t) {
            }
        });
        String idToRemove = l.getUniqueId();
        RedisService.getService().deletePost(idToRemove).enqueue(new Callback<RedisService.DelResponse>() {
            @Override
            public void onResponse(Call<RedisService.DelResponse> call, Response<RedisService.DelResponse> response) {
                values.remove(x);
                notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call<RedisService.DelResponse> call, Throwable t) {
            }
        });


    }

    public MyAdapter(List<ListItem> myDataset, String username) {
        values = myDataset;
        this.username = username;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_item_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final ListItem lItem = values.get(position);
        String userStr = lItem.getNameOfPoster();
        if (!userStr.equals(username)) {
            holder.deleteButton.setVisibility(View.GONE);
        }
        String dateStr = lItem.getDateInReadableForm();
        String descStr = lItem.getDescription();
        String urlStr = lItem.getImage2Url();
        float rating = (float)lItem.getRating();
        holder.viewRecipeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),RecipeActivity.class);
                i.putExtra("listitemuniqueid", lItem.getUniqueId());
                i.putExtra("username",username);
                startActivity(v.getContext(),i,null);
            }
        });
        holder.deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(position);
            }
        });
        Picasso.with(holder.picture.getContext()).load(urlStr).into(holder.picture);
        holder.textUser.setText("Posted by " + userStr);
        holder.textDate.setText("on " + dateStr);
        holder.textDesc.setText(descStr);
        holder.ratingBar.setRating(rating);
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
}
