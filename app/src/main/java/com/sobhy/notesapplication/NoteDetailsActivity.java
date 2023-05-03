package com.sobhy.notesapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NoteDetailsActivity extends AppCompatActivity {
    EditText titleEditText, contentEditText;
    ImageButton saveNoteBtn;
    TextView activityTitle, deleteNote;
    String title, content, noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        titleEditText = findViewById(R.id.details_et_title);
        contentEditText = findViewById(R.id.details_et_content);
        saveNoteBtn = findViewById(R.id.details_btn_save);
        activityTitle = findViewById(R.id.details_title);
        deleteNote = findViewById(R.id.note_tv_delete);

        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        noteId = getIntent().getStringExtra("noteId");

        if (noteId != null && !noteId.isEmpty()) {
            activityTitle.setText(R.string.edit_your_note);
            titleEditText.setText(title);
            contentEditText.setText(content);
            deleteNote.setVisibility(View.VISIBLE);
        }

        saveNoteBtn.setOnClickListener(v -> saveNote());
        deleteNote.setOnClickListener(v -> deleteNoteFromFirebase());
    }


    private void saveNote() {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        if (title.isEmpty()) {
            titleEditText.setError(getString(R.string.title_is_required));
            return;
        }
        Note note = new Note(title, content, Timestamp.now());
        saveNoteToFirebase(note);
    }

    private void saveNoteToFirebase(Note note) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("notes").document(currentUser.getUid())
                .collection("my_notes");
        DocumentReference documentReference;
        if (noteId != null && !noteId.isEmpty()) {
            // update note
            documentReference = collectionReference.document(noteId);
        } else {
            // add new note
            documentReference = collectionReference.document();
        }
        documentReference.set(note).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.note_added_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteNoteFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("notes").document(currentUser.getUid())
                .collection("my_notes");
        DocumentReference documentReference = collectionReference.document(noteId);
        documentReference.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, R.string.note_deleted_successfully, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.note_deleted_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}