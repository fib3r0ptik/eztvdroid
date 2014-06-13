package com.hamaksoftware.eztvpal.utils;

import android.content.DialogInterface;

public interface iDialog {
	public abstract void PositiveMethod(DialogInterface dialog, int id);
    public abstract void NegativeMethod(DialogInterface dialog, int id);
}
