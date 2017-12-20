package com.example.romvillarica.recipegram;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.TextView;

class DateComparator implements Comparator<ListItem>
{
    public int compare(ListItem c1, ListItem c2)
    {
        return c1.getDate().compareTo(c2.getDate());
    }
}

class RatingComparator implements Comparator<ListItem>
{
    public int compare(ListItem c1, ListItem c2)
    {
        Double c1Rating = c1.getRating();
        Double c2Rating = c2.getRating();
        return c1Rating.compareTo(c2Rating);
    }
}

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.my_fab)
    FloatingActionButton cameraButton;
    @BindView(R.id.welcome_message)
    TextView welcomeMessage;
    @BindView(R.id.sort_selector)
    Spinner sortSelector;

    String username;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        username = getIntent().getStringExtra("username");
        welcomeMessage.setText("Welcome, " + username + "!");

        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),CaptureActivity.class);
                i.putExtra("username",username);
                startActivity(i);
            }
        });

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        final List<ListItem> input = new ArrayList<>();

        refreshAdapter(input);

        sortSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                if (index == 1) {
                    Collections.sort(input,new RatingComparator());
                    Collections.reverse(input);
                    String test = "";
                    for (int i = 0; i < input.size(); i++) {
                        test += input.get(i).getUniqueId() + " , ";
                    }
                    //Toast.makeText(getBaseContext(),"By rating: " + test,Toast.LENGTH_LONG).show();
                    mAdapter.notifyDataSetChanged();
                }
                else {
                    Collections.sort(input,new DateComparator());
                    Collections.reverse(input);
                    String test = "";
                    for (int i = 0; i < input.size(); i++) {
                        test += input.get(i).getUniqueId() + " , ";
                    }
                    //Toast.makeText(getBaseContext(),"By date: " + test,Toast.LENGTH_LONG).show();
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });

        mAdapter = new MyAdapter(input,username);
        recyclerView.setAdapter(mAdapter);
    }
    public void refreshAdapter(final List<ListItem> input) {
        RedisService.getService().allKeys("*").enqueue(new Callback<RedisService.KeysResponse>() {
            @Override
            public void onResponse(Call<RedisService.KeysResponse> call, Response<RedisService.KeysResponse> response) {
                ArrayList<String> key = response.body().keys;
                for (int i = 0; i < key.size(); i++) {
                    /*RedisService.getService().deletePost(key.get(i)).enqueue(new Callback<RedisService.DelResponse>() {
                        @Override
                        public void onResponse(Call<RedisService.DelResponse> call, Response<RedisService.DelResponse> response) {

                        }

                        @Override
                        public void onFailure(Call<RedisService.DelResponse> call, Throwable t) {

                        }
                    });*/
                    RedisService.getService().getPost(key.get(i)).enqueue(new Callback<RedisService.GetResponse>() {
                        @Override
                        public void onResponse(Call<RedisService.GetResponse> call, Response<RedisService.GetResponse> response) {
                            UserPost up = response.body().userPost;
                            ListItem l = up.getListItem();
                            if (l != null) {
                                System.out.println(l.getImage2Url());
                                input.add(l);
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Call<RedisService.GetResponse> call, Throwable t) {

                        }
                    });
                }
                Collections.sort(input,new DateComparator());
                Collections.reverse(input);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<RedisService.KeysResponse> call, Throwable t) {

            }
        });
    }
}
