package com.example.todolist.DataBase;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.todolist.models.Note;

import java.util.List;

@Dao
public interface mainDAO {
    @Insert (onConflict = REPLACE)
    void insert (Note notes);
    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAll();
    @Query("UPDATE notes SET title = :title, notes = :notes WHERE ID = :id")
    void update(int id, String title, String notes);

    @Delete
    void delete (Note note);

    @Query("UPDATE notes SET pinned = :pin WHERE ID = :id")
    void pin(int id, boolean pin);

}
