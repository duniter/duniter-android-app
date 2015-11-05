package io.ucoin.app.technical.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import io.ucoin.app.R;

public class ClearableEditText extends RelativeLayout
{
    LayoutInflater inflater = null;
    TypedArray typedArray;
    EditText edit_text;
    Button btn_clear;
    public ClearableEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        typedArray = context.obtainStyledAttributes(attrs,R.styleable.ClearableEditText, 0, 0);
        initViews();
    }
    public ClearableEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        typedArray = context.obtainStyledAttributes(attrs,R.styleable.ClearableEditText, 0, 0);
        initViews();
    }
    public ClearableEditText(Context context)
    {
        super(context);
        typedArray = null;
        initViews();
    }
    void initViews()
    {
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.clearable_edit_text, this, true);
        edit_text = (EditText) findViewById(R.id.clearable_edit);
        btn_clear = (Button) findViewById(R.id.clearable_button_clear);
        btn_clear.setVisibility(RelativeLayout.INVISIBLE);
        if(typedArray!=null){
            edit_text.setHint(typedArray.getString(R.styleable.ClearableEditText_hint));
            typedArray.recycle();
        }
        // voir pour le hint du edit text
        clearText();
        showHideClearButton();
    }
    void clearText()
    {
        btn_clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_text.setText("");
            }
        });
    }
    void showHideClearButton()
    {
        edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) btn_clear.setVisibility(RelativeLayout.VISIBLE);
                else btn_clear.setVisibility(RelativeLayout.INVISIBLE);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    public Editable getText()
    {
        Editable text = edit_text.getText();
        return text;
    }
    public void setText(String text){
        edit_text.setText(text);
    }
    public void setError(String text){
        edit_text.setError(text);
    }
    public void addTextChangedListener(TextWatcher tw){
        edit_text.addTextChangedListener(tw);
    }

    public void setOnEditorActionListener(EditText.OnEditorActionListener listener){
        edit_text.setOnEditorActionListener(listener);
    }

}