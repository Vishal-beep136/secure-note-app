package com.vishal.secure_note.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.vishal.secure_note.R;
import com.vishal.secure_note.adapter.NoteAdapter;
import com.vishal.secure_note.databinding.ActivityMainBinding;
import com.vishal.secure_note.room.database.NoteDatabase;
import com.vishal.secure_note.room.entities.NoteEntity;
import com.vishal.secure_note.util.Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<NoteEntity> noteEntityList, stableNoteList;
    NoteAdapter noteAdapter;
    private int selectedFilter = -1;
    private final int SORT_BY_LATEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        noteEntityList = new ArrayList<>();
        stableNoteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(MainActivity.this, noteEntityList);

        setNotesOnRecyclerView();

        binding.filterNotesBtn.setOnClickListener(view -> showFilterDialog());

        binding.searchNoteBox.addTextChangedListener(searchTextWatcherListener);

        binding.settingsBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        binding.addNoteBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, AddNoteActivity.class)));
    }

    private void showFilterDialog() {
        AlertDialog.Builder labelDialog = new AlertDialog.Builder(this);

        labelDialog.setIcon(R.drawable.ic_secure_note_logo);
        labelDialog.setTitle("Filter Notes");

        labelDialog.setSingleChoiceItems(R.array.filter_options_array, selectedFilter, (dialogInterface, i) -> {
            selectedFilter = i;
            handleNoteFilter(i);
            dialogInterface.dismiss();
        });

        labelDialog.setNegativeButton("clear", (dialogInterface, i) -> {
            selectedFilter = -1;
            addAllNotesToNoteEntityList();
            dialogInterface.dismiss();
        });

        labelDialog.show();
    }

    private void handleNoteFilter(int selectedIndex) {
        final int SORT_BY_OLDEST = 1;
        switch (selectedIndex) {
            case Constant.FILTER_BY_LATEST_NOTE:
                filterNoteByDate(SORT_BY_LATEST);
                break;
            case Constant.FILTER_BY_IMPORTANT_NOTE:
                filterNotesByLabel(Constant.IMPORTANT_LABEL);
                break;
            case Constant.FILTER_BY_PRIVATE_NOTE:
                filterNotesByVisibility(true);
                break;
            case Constant.FILTER_BY_NEEDED_NOTE:
                filterNotesByLabel(Constant.NEEDED_LABEL);
                break;
            case Constant.FILTER_BY_PUBLIC_NOTE:
                filterNotesByVisibility(false);
                break;
            case Constant.FILTER_BY_OLDEST_NOTE:
                filterNoteByDate(SORT_BY_OLDEST);
                break;
        }
    }


    private void filterNotesByVisibility(boolean isPrivate) {
        showNoteNotFound();
        if (stableNoteList.size() == 0) return;
        List<NoteEntity> notesByVisibilityList = new ArrayList<>();
        for (NoteEntity note : stableNoteList) {
            if (note.isPrivate() == isPrivate)
                notesByVisibilityList.add(note);
        }
        updateNoteList(notesByVisibilityList);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterNotesByLabel(int label) {
        showNoteNotFound();
        if (stableNoteList.size() == 0) return;
        List<NoteEntity> notesByLabelList = new ArrayList<>();
        for (NoteEntity note : stableNoteList) {
            if (note.getLabel() == label)
                notesByLabelList.add(note);
        }
        updateNoteList(notesByLabelList);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterNoteByDate(int filterDateCode) {
        if (filterDateCode == SORT_BY_LATEST) Collections.sort(stableNoteList, latestComparator);
        else Collections.sort(stableNoteList, oldestComparator);

        noteEntityList.clear();
        noteEntityList.addAll(stableNoteList);
        noteAdapter.notifyDataSetChanged();
        showNotificationCircleIcon();
        showNoteNotFound();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateNoteList(List<NoteEntity> updatedList) {
        noteEntityList.clear();
        noteEntityList.addAll(updatedList);
        noteAdapter.notifyDataSetChanged();
        updatedList.clear();
        showNotificationCircleIcon();
        showNoteNotFound();
    }

    private void setNotesOnRecyclerView() {
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.noteRecView.setLayoutManager(staggeredGridLayoutManager);
        binding.noteRecView.setHasFixedSize(true);
        binding.noteRecView.setAdapter(noteAdapter);
        addAllNotesToNoteEntityList();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addAllNotesToNoteEntityList() {
        showNotificationCircleIcon();
        NoteDatabase.getInstance(MainActivity.this)
                .noteDao()
                .getAllNotesLiveList()
                .observe(this, noteEntities -> {
                    noteEntityList.clear();
                    stableNoteList.clear();
                    noteEntityList.addAll(noteEntities);
                    stableNoteList.addAll(noteEntities);
                    noteAdapter.notifyDataSetChanged();
                    showNoteNotFound();
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void searchNote(String searchStr) {
        NoteDatabase.getInstance(this)
                .noteDao()
                .getSearchResult(searchStr)
                .observe(this, noteEntities -> {
                    noteEntityList.clear();
                    noteEntityList.addAll(noteEntities);
                    runOnUiThread(() -> {
                        noteAdapter.notifyDataSetChanged();
                        showNoteNotFound();
                    });
                });
    }

    private void showNotificationCircleIcon() {
        if (selectedFilter != -1)
            binding.notificationCircleIcon.setVisibility(View.VISIBLE);
        else
            binding.notificationCircleIcon.setVisibility(View.GONE);
    }

    Comparator<NoteEntity> latestComparator = (noteA, noteB) -> {
        Date dateA = new Date(noteA.getNoteDate());
        Date dateB = new Date(noteB.getNoteDate());
        return dateB.compareTo(dateA);
    };

    Comparator<NoteEntity> oldestComparator = (noteA, noteB) -> {
        Date dateA = new Date(noteA.getNoteDate());
        Date dateB = new Date(noteB.getNoteDate());
        return dateA.compareTo(dateB);
    };

    private void showNoteNotFound() {
        if (noteEntityList == null || noteEntityList.size() == 0) {
            binding.noteRecView.setVisibility(View.GONE);
            binding.noteNotFoundImg.setVisibility(View.VISIBLE);
            binding.noteNotFoundTv.setVisibility(View.VISIBLE);
        } else {
            binding.noteRecView.setVisibility(View.VISIBLE);
            binding.noteNotFoundImg.setVisibility(View.GONE);
            binding.noteNotFoundTv.setVisibility(View.GONE);
        }
    }


    TextWatcher searchTextWatcherListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable != null) {
                String searchStr = editable.toString();
                if (!searchStr.isEmpty()) {
                    searchNote(searchStr);
                } else addAllNotesToNoteEntityList();
            }
        }
    };
}