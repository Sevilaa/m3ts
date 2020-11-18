package com.android.grafika.initialize;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Random;

import cz.fmo.R;

public class InitializeCreateMatchRoom extends android.app.Fragment implements Button.OnClickListener, TextWatcher {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private EditText editMatchId;
    private String matchID;
    protected int layout;

    public InitializeCreateMatchRoom() {
        // Required empty public constructor
        this.layout = R.layout.fragment_init_create_match_room;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(this.layout, container, false);
        Button btnCreateRoom = view.findViewById(R.id.init_createRoomBtn);
        btnCreateRoom.setOnClickListener(this);
        this.editMatchId = view.findViewById(R.id.init_editMatchID);
        this.editMatchId.addTextChangedListener(this);
        updateViews();
        return view;
    }

    public void updateViews() {
        this.matchID = generateRandomAlphabeticString(8);
        this.editMatchId.setText(this.matchID);
    }

    @Override
    public void onClick(View view) {
        // create Room Button has been clicked
        ((InitializeActivity) getActivity()).onMatchIDSelected(this.matchID.toLowerCase());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((InitializeActivity)getActivity()).startBackgroundAnimation();
    }

    private String generateRandomAlphabeticString(int length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // ignore
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        this.matchID = charSequence.toString();
    }

    @Override
    public void afterTextChanged(Editable editable) {
        // ignore
    }
}