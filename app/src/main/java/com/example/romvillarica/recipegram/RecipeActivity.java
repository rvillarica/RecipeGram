package com.example.romvillarica.recipegram;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeActivity extends AppCompatActivity {
    @BindView(R.id.back_button)
    Button backButton;
    @BindView(R.id.dislike_button)
    Button dislikeButton;
    @BindView(R.id.like_button)
    Button likeButton;

    @BindView(R.id.date)
    TextView dateTextView;
    @BindView(R.id.rating_bar)
    RatingBar ratingBar;

    @BindView(R.id.thanks)
    TextView thanksTextView;

    @BindView(R.id.picture1_frame)
    ImageView picture1Frame;
    @BindView(R.id.picture2_frame)
    ImageView picture2Frame;
    @BindView(R.id.instructions)
    TextView instructionsTextView;
    @BindView(R.id.description)
    TextView descriptionTextView;

    ListItem listItem;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        ButterKnife.bind(this);

        username = getIntent().getStringExtra("username");
        String key = getIntent().getStringExtra("listitemuniqueid");
        RedisService.getService().getPost(key).enqueue(new Callback<RedisService.GetResponse>() {
            @Override
            public void onResponse(Call<RedisService.GetResponse> call, Response<RedisService.GetResponse> response) {
                UserPost up = response.body().userPost;
                listItem = up.getListItem();
                if (listItem != null) {
                    dateTextView.setText(listItem.getDate());
                    ratingBar.setRating((float)listItem.getRating());
                    Picasso.with(picture1Frame.getContext()).load(listItem.getImage1Url()).into(picture1Frame);
                    Picasso.with(picture2Frame.getContext()).load(listItem.getImage2Url()).into(picture2Frame);
                    instructionsTextView.setText(listItem.getInstructions());
                    descriptionTextView.setText(listItem.getDescription());
                }
            }
            @Override
            public void onFailure(Call<RedisService.GetResponse> call, Throwable t) {

            }
        });

        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listItem.addRating(0);
                ratingBar.setRating((float)listItem.getRating());
                likeButton.setVisibility(View.GONE);
                dislikeButton.setVisibility(View.GONE);
                thanksTextView.setVisibility(View.VISIBLE);
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listItem.addRating(5);
                ratingBar.setRating((float)listItem.getRating());
                likeButton.setVisibility(View.GONE);
                dislikeButton.setVisibility(View.GONE);
                thanksTextView.setVisibility(View.VISIBLE);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View v1 = v;
                //update the ListITem on the server
                UserPost up = new UserPost();
                up.setListItem(listItem);
                RedisService.getService().makePost(listItem.getUniqueId(),up).enqueue(new Callback<RedisService.SetResponse>() {
                    @Override
                    public void onResponse(Call<RedisService.SetResponse> call, retrofit2.Response<RedisService.SetResponse> response) {
                        Intent i = new Intent(v1.getContext(),MainActivity.class);
                        i.putExtra("username",username);
                        startActivity(i);
                    }

                    @Override
                    public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {
                        Toast.makeText(v1.getContext(), t.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }
}
