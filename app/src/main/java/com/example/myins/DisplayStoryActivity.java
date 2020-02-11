package com.example.myins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myins.Models.Story;
import com.example.myins.Models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DisplayStoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    private ImageView storyPhoto;
    private CircleImageView USrPhoto;
    private TextView StoryUsrName;
    private View skip, reverse;
    private StoriesProgressView storiesProgressView;
    private int counter;
    private long press = 0L;
    private long limit = 500L;
    private List<String> storries;
    private List<String> images;
    private String UserID;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    press = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;

                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now-press;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_story);

        storyPhoto = findViewById(R.id.theStory);
        USrPhoto = findViewById(R.id.storyProfile);
        storiesProgressView = findViewById(R.id.stories);
        StoryUsrName = findViewById(R.id.storyUsrName);
        reverse = findViewById(R.id.reverse);
        skip = findViewById(R.id.skip);

        UserID = getIntent().getStringExtra("user");
        getStories(UserID);
        getUserInfo(UserID);

        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);


        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onNext() {
        if (counter + 1 >= images.size()){
            return;
        }
        Picasso.get().load(images.get(++counter)).into(storyPhoto);

    }

    @Override
    public void onPrev() {
        if (counter - 1 < 0){
            return;
        }
        Picasso.get().load(images.get(++counter)).into(storyPhoto);
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    private void getStories(String user){
        storries = new ArrayList<>();
        images = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(user);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                storries.clear();
                images.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    long current = System.currentTimeMillis();
                    Story story = snapshot.getValue(Story.class);
                    if (current > story.getTimeStart() && current < story.getTimeEnd()){
                        storries.add(story.getStoryId());
                        images.add(story.getImageUri());
                    }
                }
                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(DisplayStoryActivity.this);
                storiesProgressView.startStories(counter);
                Picasso.get().load(images.get(counter)).into(storyPhoto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getUserInfo(String user){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user1 = dataSnapshot.getValue(User.class);
                Picasso.get().load(user1.getImage()).placeholder(R.drawable.profile)
                        .into(USrPhoto);
                StoryUsrName.setText(user1.getUsername());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
