package com.example.todolist.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.todolist.R;
import com.example.todolist.adapter.NotesListAdapter;
import com.example.todolist.db.RoomDB;
import com.example.todolist.models.Note;
import com.example.todolist.ui.takeNote.TakeNoteActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private RecyclerView recyclerView;
    private NotesListAdapter notesListAdapter;

    private SearchView searchView;
    private FloatingActionButton fabAdd;

    private FirebaseUser user;
    private RoomDB database;

    private List<Note> note = new ArrayList<>();
    private Note selectedNote;

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Note note) {
            Intent intent = new Intent(MainActivity.this, TakeNoteActivity.class);
            intent.putExtra("old_note", note);
            startActivityForResult(intent, 102);
        }

        @Override
        public void onLongClick(Note note, CardView cardView) {
            selectedNote = note;
            showPopup(cardView);
        }
    };

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> onSignInResult(result)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getViews();

        firebaseAuthSetup();

        database = RoomDB.getInstance(getApplicationContext());
        note = database.mainDao().getAll();

        updateRecycle(note);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, TakeNoteActivity.class);
            startActivityForResult(intent, 101);
        });

        setSearchQuery();
    }

    private void firebaseAuthSetup() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(@NonNull FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            // ...
        } else {
            firebaseAuthSetup();
            Toast.makeText(this, "Incorrect login, please retry", Toast.LENGTH_SHORT).show();
        }
    }

    private void getViews() {
        recyclerView = findViewById(R.id.recycler_home);
        fabAdd = findViewById(R.id.fab_add);
        searchView = findViewById(R.id.searchView_home);
    }

    private void setSearchQuery() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    private void filter(String newText) {
        List<Note> filteredList = new ArrayList<>();
        for (Note singleNote : note) {
            if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase())
                    || singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(singleNote);
            }
        }
        notesListAdapter.filterList(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Note newNote = (Note) data.getSerializableExtra("note");

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 101) {
                if (newNote != null) {
                    database.mainDao().insert(newNote);
                    note.clear();
                    note.addAll(database.mainDao().getAll());
                    notesListAdapter.notifyDataSetChanged();
                }
            } else if (requestCode == 102) {
                if (newNote != null) {
                    database.mainDao().update(newNote.getID(), newNote.getTitle(), newNote.getNotes());
                    note.clear();
                    note.addAll(database.mainDao().getAll());
                    notesListAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    private void updateRecycle(List<Note> note) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, note, notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }

    private void showPopup(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pin:
                if (selectedNote.isPinned()) {
                    database.mainDao().pin(selectedNote.getID(), false);
                    Toast.makeText(MainActivity.this, "Unpinned", Toast.LENGTH_SHORT).show();
                } else {
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