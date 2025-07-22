package com.universalyoga.adminapp.utils;

import android.widget.EditText;

public class ValidationUtils {
    public static boolean empty(EditText e){return e.getText().toString().trim().isEmpty();}
    public static boolean number(EditText e){try{Double.parseDouble(e.getText().toString());return true;}catch(Exception ex){return false;}}
    public static boolean isEmpty(EditText e){return empty(e);}
    public static boolean isValidNumber(EditText e){return number(e);}
}