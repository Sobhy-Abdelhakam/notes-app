package com.sobhy.notesapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton addNoteFab;
    ImageButton menuBtn;
    RecyclerView notesRecyclerView;
    NoteAdapter noteAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addNoteFab= findViewById(R.id.main_fab_add_note);
        menuBtn= findViewById(R.id.main_btn_menu);
        notesRecyclerView= findViewById(R.id.main_rv_notes);

        addNoteFab.setOnClickListener(v -> startActivity(new Intent(getBaseContext(), NoteDetailsActivity.class)));
        menuBtn.setOnClickListener(v -> showMenu());
        setupRecyclerView();
    }
    private void showMenu() {
        PopupMenu popupMenu= new PopupMenu(this, menuBtn);
        popupMenu.getMenu().add(R.string.logout);
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            if(item.getTitle()==getString(R.string.logout)){
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
    private void setupRecyclerView() {
        // get data for current user:-
        FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference reference= FirebaseFirestore.getInstance()
                .collection("notes").document(currentUser.getUid())
                .collection("my_notes");
        // get data by descending order:-
        Query query= reference.orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> options= new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class).build();
        noteAdapter= new NoteAdapter(options);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(noteAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        noteAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteAdapter.notifyDataSetChanged();
    }
}