package com.sobhy.notesapplication;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewHolder> {

    private final ClickListener clickListener;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, ClickListener clickListener) {
        super(options);
        this.clickListener= clickListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note model) {
        holder.noteTitle.setText(model.getTitle());
        holder.noteContent.setText(model.getContent());
        String time= new SimpleDateFormat("dd/MM/YYYY").format(model.getTimestamp().toDate());
        holder.noteTime.setText(time);

        holder.itemView.setOnClickListener(v -> {
            String noteId = this.getSnapshots().getSnapshot(position).getId();
            String title = model.getTitle();
            String content = model.getContent();
            if (clickListener != null){
                Log.d("click listener", "onBindViewHolder: clicked");
                clickListener.onPositionClicked(noteId, title, content);
            }


//            Navigation.findNavController(v).navigate(R.id.action_notesFragment_to_noteDetailsFragment);
        });
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle, noteContent, noteTime;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle= itemView.findViewById(R.id.item_note_title);
            noteContent= itemView.findViewById(R.id.item_note_content);
            noteTime= itemView.findViewById(R.id.item_note_time);
        }
    }
}
