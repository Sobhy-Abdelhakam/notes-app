package com.sobhy.notesapplication.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sobhy.notesapplication.ClickListener;
import com.sobhy.notesapplication.Note;
import com.sobhy.notesapplication.NoteAdapter;
import com.sobhy.notesapplication.R;

public class NotesFragment extends Fragment implements ClickListener {
    FloatingActionButton addNoteFab;
    ImageButton menuBtn;
    RecyclerView notesRecyclerView;
    NoteAdapter noteAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        addNoteFab = view.findViewById(R.id.main_fab_add_note);
        menuBtn = view.findViewById(R.id.main_btn_menu);
        notesRecyclerView = view.findViewById(R.id.main_rv_notes);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addNoteFab.setOnClickListener(v -> {
                    NotesFragmentDirections.ActionNotesFragmentToNoteDetailsFragment action =
                            NotesFragmentDirections.actionNotesFragmentToNoteDetailsFragment(null, null, null);
                    Navigation.findNavController(requireView()).navigate(action);
                }

        );
        menuBtn.setOnClickListener(v -> showMenu(savedInstanceState));
        setupRecyclerView();
    }

    private void showMenu(Bundle savedInstanceState) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), menuBtn);
        popupMenu.getMenu().add(R.string.logout);
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle() == getString(R.string.logout)) {
                FirebaseAuth.getInstance().signOut();
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.notesFragment, true)
                        .build();

                Navigation.findNavController(getView()).navigate(R.id.action_notesFragment_to_loginFragment, savedInstanceState, navOptions);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        // get data for current user:-
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            CollectionReference reference = FirebaseFirestore.getInstance()
                    .collection("notes").document(currentUser.getUid())
                    .collection("my_notes");
            // get data by descending order:-
            Query query = reference.orderBy("timestamp", Query.Direction.DESCENDING);
            FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                    .setQuery(query, Note.class).build();
            noteAdapter = new NoteAdapter(options, this);
            notesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            notesRecyclerView.setAdapter(noteAdapter);
        }
    }

    @Override
    public void onPositionClicked(String noteId, String noteTitle, String noteContent) {
        Log.d("Navigation", "Item clicked: " + noteId);
        NotesFragmentDirections.ActionNotesFragmentToNoteDetailsFragment action = NotesFragmentDirections.actionNotesFragmentToNoteDetailsFragment(noteId, noteTitle, noteContent);
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        noteAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        noteAdapter.notifyDataSetChanged();
    }
}