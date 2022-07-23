package com.vishal.secure_note.adapter;

import static com.vishal.secure_note.util.Constant.CUSTOM_PASSWORD;
import static com.vishal.secure_note.util.Constant.IMPORTANT_LABEL;
import static com.vishal.secure_note.util.Constant.NEEDED_LABEL;
import static com.vishal.secure_note.util.Constant.NORMAL_LABEL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vishal.secure_note.R;
import com.vishal.secure_note.activities.NoteViewerActivity;
import com.vishal.secure_note.databinding.NoteSingleRowPrivateBinding;
import com.vishal.secure_note.databinding.NoteSingleRowPublicBinding;
import com.vishal.secure_note.room.database.NoteDatabase;
import com.vishal.secure_note.room.entities.NoteEntity;
import com.vishal.secure_note.security.Auth;
import com.vishal.secure_note.util.Common;

import org.commonmark.node.Node;

import java.util.List;
import java.util.Random;

import io.noties.markwon.Markwon;

/**
 * Created by Vishal on 12, Jul, 2022
 */
public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int PRIVATE_NOTE = 1, PUBLIC_NOTE = 2;
    private final Context context;
    private final List<NoteEntity> noteList;

    public NoteAdapter(Context context, List<NoteEntity> noteList) {
        this.context = context;
        this.noteList = noteList;
    }

    @Override
    public int getItemViewType(int position) {
        if (noteList.get(position).isPrivate()) return PRIVATE_NOTE;
        return PUBLIC_NOTE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // getting needed layout in this View noteView
        View noteView = LayoutInflater.from(context).inflate(R.layout.note_single_row_public, parent, false);
        if (viewType == PRIVATE_NOTE) {
            noteView = LayoutInflater.from(context).inflate(R.layout.note_single_row_private, parent, false);
            return new PrivateNoteViewHolder(noteView);
        }
        return new PublicNoteViewHolder(noteView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case PRIVATE_NOTE:
                PrivateNoteViewHolder privateHolder = (PrivateNoteViewHolder) holder;
                privateHolder.setPrivateNote();
                break;
            case PUBLIC_NOTE:
                PublicNoteViewHolder publicHolder = (PublicNoteViewHolder) holder;
                publicHolder.setPublicNote();
                break;
        }
    }


    @Override
    public int getItemCount() {
        return noteList == null ? 0 : noteList.size();
    }

    public class PrivateNoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final NoteSingleRowPrivateBinding privateBinding;
        private boolean isPasswordVisible = false;


        public PrivateNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            privateBinding = NoteSingleRowPrivateBinding.bind(itemView);
            privateBinding.noteCardView.setOnClickListener(this);
            privateBinding.noteCardView.setOnLongClickListener(this);
        }

        void setPrivateNote() {
            int label = noteList.get(getAdapterPosition()).getLabel();
            manageLabel(label, privateBinding.labelImage);
            privateBinding.noteBg.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(getRandomHexColor())));
        }

        @Override
        public boolean onLongClick(View view) {
            Toast.makeText(context, "Unable to delete private note!", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onClick(View view) {
            NoteEntity noteEntity = noteList.get(getAdapterPosition());
            int passwordType = noteEntity.getPasswordType();
            openNoteFromPasswordType(passwordType);
        }

        private void openNoteFromPasswordType(int passwordType) {
            Dialog dialog = getPrivateNoteDialog();
            handleToggleEyePassword(dialog);
            handlePasswordType(dialog, passwordType);
            dialog.show();
        }

        private void handleToggleEyePassword(@NonNull Dialog dialog) {
            ImageView showPasswordNoteBtn = dialog.findViewById(R.id.showPasswordNoteBtn);
            EditText passwordBox = dialog.findViewById(R.id.passwordNoteEtBox);
            showPasswordNoteBtn.setOnClickListener(view -> {
                isPasswordVisible = !isPasswordVisible;
                toggleEyePassword(showPasswordNoteBtn, passwordBox);
            });
        }

        private void toggleEyePassword(ImageView showPasswordNoteBtn, EditText editText) {
            if (isPasswordVisible) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showPasswordNoteBtn.setImageResource(R.drawable.ic_eye_open);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showPasswordNoteBtn.setImageResource(R.drawable.ic_eye_close);
            }
            editText.setSelection(editText.getText().length());
        }

        private void handlePasswordType(Dialog dialog, int passwordType) {
            if (dialog == null) return;

            EditText passwordNoteEtBox = dialog.findViewById(R.id.passwordNoteEtBox);
            Button openNoteBtn = dialog.findViewById(R.id.openNoteBtn);

            passwordNoteEtBox.setOnEditorActionListener((textView, i, keyEvent) -> {
                if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_GO) {
                    if (passwordNoteEtBox.getText().toString().isEmpty()) {
                        setErrorToEditText(passwordNoteEtBox, "Password is required!");
                        return false;
                    }

                    if (passwordType == CUSTOM_PASSWORD) {
                        viewNoteFromCustomPassword(passwordNoteEtBox, dialog);
                    } else
                        viewNoteFromAppPassword(passwordNoteEtBox, dialog);

                    return true;
                }
                return false;
            });

            openNoteBtn.setOnClickListener(view -> {
                if (passwordNoteEtBox.getText().toString().isEmpty())
                    setErrorToEditText(passwordNoteEtBox, "Password is required!");
                else {
                    if (passwordType == CUSTOM_PASSWORD) {
                        viewNoteFromCustomPassword(passwordNoteEtBox, dialog);
                    } else
                        viewNoteFromAppPassword(passwordNoteEtBox, dialog);
                }
            });

        }

        private void viewNoteFromAppPassword(@NonNull EditText passwordNoteEtBox, Dialog dialog) {
            Auth auth = new Auth(context);
            boolean isAppPasswordCorrect = auth.isValidAppPassword(passwordNoteEtBox.getText().toString());

            if (isAppPasswordCorrect) {
                dialog.dismiss();
                viewNote();
            } else setErrorToEditText(passwordNoteEtBox, "App password is wrong!");
        }

        private void viewNoteFromCustomPassword(@NonNull EditText passwordNoteEtBox, Dialog dialog) {
            NoteEntity noteEntity = noteList.get(getAdapterPosition());
            if (noteEntity == null) return;
            String customPassword = noteEntity.getPassword();
            if (customPassword.equalsIgnoreCase(passwordNoteEtBox.getText().toString())) {
                dialog.dismiss();
                viewNote();
            } else setErrorToEditText(passwordNoteEtBox, "Custom password is wrong!");
        }

        private void setErrorToEditText(@NonNull EditText editText, String msg) {
            editText.setError(msg);
            editText.requestFocus();
        }

        public void viewNote() {
            NoteEntity noteEntity = noteList.get(getAdapterPosition());
            Intent intent = new Intent(context, NoteViewerActivity.class);
            intent.putExtra("noteId", noteEntity.getUid());
            context.startActivity(intent);
        }

        @NonNull
        private Dialog getPrivateNoteDialog() {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.layout_open_private_note_dialog);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(true);
            return dialog;
        }

    }

    class PublicNoteViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private final NoteSingleRowPublicBinding publicBinding;

        public PublicNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            publicBinding = NoteSingleRowPublicBinding.bind(itemView);
            publicBinding.noteCardView.setOnClickListener(this);
            publicBinding.noteCardView.setOnLongClickListener(this);
        }

        void setPublicNote() {
            NoteEntity note = noteList.get(getAdapterPosition());
            publicBinding.noteBg.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(getRandomHexColor())));
            manageLabel(note.getLabel(), publicBinding.labelImage);

            final Markwon markwon = Markwon.create(context);
            final Node node = markwon.parse(note.getNoteDescription());
            final Spanned markdown = markwon.render(node);
            markwon.setParsedMarkdown(publicBinding.noteDesc, markdown);
            publicBinding.noteTitle.setText(note.getNoteTitle());
            publicBinding.noteDate.setText(Common.convertMilliInStringDate(note.getNoteDate()));

            publicBinding.noteTitle.setOnClickListener(this);
            publicBinding.noteTitle.setOnLongClickListener(this);
            publicBinding.noteDesc.setOnClickListener(this);
            publicBinding.noteDesc.setOnLongClickListener(this);
        }

        void deleteNote() {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Note")
                    .setMessage("Are you really want to delete note number :  " + (getAdapterPosition() + 1))
                    .setPositiveButton("Yes", (dialogInterface, i) -> new Thread(() -> {
                        NoteDatabase.getInstance(context)
                                .noteDao()
                                .deleteNote(noteList.get(getAdapterPosition()));
                        ((Activity) context).runOnUiThread(() -> {
                            Toast.makeText(context, "note deleted!", Toast.LENGTH_SHORT).show();
                            notifyItemRemoved(getAdapterPosition());
                        });
                    }).start()).setNegativeButton("No", null)
                    .setIcon(android.R.drawable.ic_delete)
                    .show();
        }


        @Override
        public void onClick(View view) {
            NoteEntity noteEntity = noteList.get(getAdapterPosition());
            Intent intent = new Intent(context, NoteViewerActivity.class);
            intent.putExtra("noteId", noteEntity.getUid());
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view) {
            deleteNote();
            return false;
        }
    }


    private void manageLabel(int label, ImageView labelImg) {
        switch (label) {
            case NEEDED_LABEL:
                labelImg.setImageTintList(ContextCompat.getColorStateList(context, R.color.green));
                break;
            case IMPORTANT_LABEL:
                labelImg.setImageTintList(ContextCompat.getColorStateList(context, R.color.red));
                break;
            case NORMAL_LABEL:
            default:
                labelImg.setImageTintList(ContextCompat.getColorStateList(context, android.R.color.transparent));
                break;
        }
    }

    private String getRandomHexColor() {
        final String[] hexLightColors = new String[]{"#FFA5A5", "#FFE8A5", "#BAF3FF", "#BFFFBA", "#FFD099"};
        Random random = new Random();
        int randomColorNum = random.nextInt(hexLightColors.length);
        return hexLightColors[randomColorNum];
    }
}
