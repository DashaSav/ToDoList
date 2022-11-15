package com.example.todolist.ui.takeNote;

import androidx.cardview.widget.CardView;

import com.example.todolist.models.Note;

public interface NotesClickListener  {
    void onClick (Note note);
    void onLongClick (Note note, CardView cardView);

}
