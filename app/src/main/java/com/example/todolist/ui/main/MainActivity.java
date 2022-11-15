package com.example.todolist.ui.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.todolist.adapter.NotesListAdapter;
import com.example.todolist.db.RoomDB;
import com.example.todolist.ui.takeNote.NoteTakenActivity;
import com.example.todolist.ui.takeNote.NotesClickListener;
import com.example.todolist.R;
import com.example.todolist.models.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    RecyclerView recyclerView;
    FloatingActionButton fab_add;
    NotesListAdapter notesListAdapter;
    RoomDB database;
    List<Note> note = new ArrayList<>();
    SearchView searchView_home;
    Note selectedNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_add);
        searchView_home = findViewById(R.id.searchView_home);
        database = RoomDB.getInstance(this);
        note = database.mainDao().getAll();


        updateRecycle(note);
        fab_add.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NoteTakenActivity.class);
            startActivityForResult(intent, 101);
        });

        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });


    }
    private void filter(String newText){
        List<Note> filteredList = new ArrayList<>();
        for(Note singleNote: note){
            if(singleNote.getTitle().toLowerCase().contains(newText.toLowerCase())
                    || singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())){
                filteredList.add(singleNote);
            }
        }
        notesListAdapter.filterList(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101){
            if(resultCode == Activity.RESULT_OK){
                Note new_note = (Note) data.getSerializableExtra("note");
                database.mainDao().insert(new_note);
                note.clear();
                note.addAll(database.mainDao().getAll());
                notesListAdapter.notifyDataSetChanged();
            }
        }
        if(requestCode == 102){
            if(resultCode == Activity.RESULT_OK){
                Note new_note =(Note) data.getSerializableExtra("note");
                database.mainDao().update(new_note.getID(), new_note.getTitle(), new_note.getNotes());
                note.clear();
                note.addAll(database.mainDao().getAll());
                notesListAdapter.notifyDataSetChanged();
            }

        }
    }

    private void updateRecycle(List<Note> note) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, note, notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Note note) {
            Intent intent = new Intent(MainActivity.this, NoteTakenActivity.class);
            intent.putExtra("old_note", note);
            startActivityForResult(intent, 102);

        }

        @Override
        public void onLongClick(Note note, CardView cardView) {
            selectedNote = new Note();
            selectedNote = note;
            showPopup(cardView);

        }
    };

    private void showPopup(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.pin:
                if(selectedNote.isPinned()){
                    database.mainDao().pin(selectedNote.getID(), false);
                    Toast.makeText(MainActivity.this, "Unpinned", Toast.LENGTH_SHORT).show();
                }else{
                    database.mainDao().pin(selectedNote.getID(), true);
                    Toast.makeText(MainActivity.this, "Pinned", Toast.LENGTH_SHORT).show();
                }
                note.clear();
                note.addAll(database.mainDao().getAll());
                notesListAdapter.notifyDataSetChanged();
                return true;


            case R.id.delete:
                database.mainDao().delete(selectedNote);
                note.remove(selectedNote);
                Toast.makeText(MainActivity.this, "Note removed", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }

    }
}