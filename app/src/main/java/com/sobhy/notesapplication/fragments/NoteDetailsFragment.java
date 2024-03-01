package com.sobhy.notesapplication.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.sobhy.notesapplication.Note;
import com.sobhy.notesapplication.R;

public class NoteDetailsFragment extends Fragment {
    EditText titleEditText, contentEditText;
    ImageButton saveNoteBtn;
    TextView activityTitle, deleteNote;
    NoteDetailsFragmentArgs args;
    String title, content, noteId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note_details, container, false);
        titleEditText = view.findViewById(R.id.details_et_title);
        contentEditText = view.findViewById(R.id.details_et_content);
        saveNoteBtn = view.findViewById(R.id.details_btn_save);
        activityTitle = view.findViewById(R.id.details_title);
        deleteNote = view.findViewById(R.id.note_tv_delete);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        args = NoteDetailsFragmentArgs.fromBundle(getArguments());

        if (args != null) {
            if (args.getTitle() != null) {
                title = args.getTitle();
            }

            if (args.getContent() != null) {
                content = args.getContent();
            }

            if (args.getNoteId() != null) {
                noteId = args.getNoteId();
            }
        }


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
        if (currentUser != null) {
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
                    Toast.makeText(requireContext(), R.string.note_added, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(getView()).popBackStack();
                } else {
                    Toast.makeText(requireContext(), R.string.note_added_failed, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(requireContext(), "no user found", Toast.LENGTH_SHORT).show();
        }

    }

    private void deleteNoteFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("notes").document(currentUser.getUid())
                .collection("my_notes");
        DocumentReference documentReference = collectionReference.document(noteId);
        documentReference.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(), R.string.note_deleted_successfully, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(getView()).popBackStack();
            } else {
                Toast.makeText(requireContext(), R.string.note_deleted_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}