package com.tantonj.flynnmobilepracticaltest;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by tantonj on 9/11/2017.
 */

public class AddDialogFragment extends DialogFragment {

    public static int ADD_ALBUM = 0;
    public static int ADD_PHOTO = 1;

    public static AddDialogFragment newInstance(int title, int mode) { //constructs the add Dialog, two modes; Album and Photo
        AddDialogFragment frag = new AddDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("mode", mode);
        frag.setArguments(args);
        return frag;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        final AlertDialog dialog;
        final int title = getArguments().getInt("title");
        final int mode = getArguments().getInt("mode");
        float scale = getActivity().getResources().getDisplayMetrics().density;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        int layoutId = R.layout.add_album_layout; //gets appropriete layout depending on Mode
        if(mode == AddDialogFragment.ADD_PHOTO)
                layoutId = R.layout.add_photo_layout;
        final View view = inflater.inflate(layoutId, null);

        TextView textTitle = new TextView(getActivity()); //sets title based on argument
        textTitle.setText(getText(title));
        textTitle.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
        textTitle.setPadding(10,10,10,10);
        textTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setCustomTitle(textTitle);

        if(mode == AddDialogFragment.ADD_ALBUM) { //if adding an album
            ImageButton addAlbumBut = (ImageButton) view.findViewById(R.id.addAlbumButD);
            addAlbumBut.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EditText titleText = (EditText) view.findViewById(R.id.addAlbumTitle);
                    if (titleText.getText().toString().equals("")) {
                        Toast.makeText(getActivity(), "Albums must have names", Toast.LENGTH_LONG).show();
                    } else {
                        ((AlbumActivity) getActivity()).addAlbum(titleText.getText().toString()); //execute the method addAlbum from AlbumActivity
                        getDialog().dismiss();
                    }
                }
            });
        }else if(mode == AddDialogFragment.ADD_PHOTO) { //if adding a photo
            ImageButton addPhotoBut = (ImageButton) view.findViewById(R.id.addPhotoButD);
            addPhotoBut.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EditText urlText = (EditText) view.findViewById(R.id.addImgUrl);
                    EditText titleText = (EditText) view.findViewById(R.id.addImgTitle);
                    if (titleText.getText().toString().equals("") || urlText.getText().toString().equals("")) {
                        Toast.makeText(getActivity(), "Photos must have a source and title", Toast.LENGTH_LONG).show();
                    } else {
                        ((PhotoActivity) getActivity()).addPhoto(urlText.getText().toString(), titleText.getText().toString()); //executes the method addPhoto from PhotoActivity
                        getDialog().dismiss();
                    }
                }
            });
        }

        builder.setView(view);
        dialog = builder.create();
        return dialog;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }
}
