/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.gdi.android.news.preference.color;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import fr.gdi.android.news.R;

public class ColorPickerDialog extends AlertDialog implements ColorPickerView.OnColorChangedListener, TextWatcher
{
    
    public interface OnColorChangedListener 
    {
        public void onColorChanged(int color);
    }
    
    private boolean supportsAlpha;
    
    private ColorPickerView mColorPicker;
    
    private ColorPanelView mOldColor;
    private ColorPanelView mNewColor;
    
    private OnColorChangedListener mListener;
    
    
    private EditText hexText;
    
    private boolean propagating; 
    
    public ColorPickerDialog(Context context, int initialColor)
    {
        super(context);
        
        init(initialColor);
    }
    
    private void init(int color)
    {
        // To fight color branding.
        getWindow().setFormat(PixelFormat.RGBA_8888);
        
        setUp(color);
        
    }
    
    private void setUp(int color)
    {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_color_picker, null);
        
        setView(layout);
        
        setTitle(R.string.color_picker_title);
        // setIcon(android.R.drawable.ic_dialog_info);
        
        mColorPicker = (ColorPickerView) layout.findViewById(R.id.color_picker_view);
        mOldColor = (ColorPanelView) layout.findViewById(R.id.old_color_panel);
        mNewColor = (ColorPanelView) layout.findViewById(R.id.new_color_panel);
        
        hexText = (EditText) layout.findViewById(R.id.hex_color_text);
        
        ((LinearLayout) mOldColor.getParent()).setPadding(Math.round(mColorPicker.getDrawingOffset()), 0, Math.round(mColorPicker.getDrawingOffset()), 0);
        
        mColorPicker.setOnColorChangedListener(this);
        
        hexText.addTextChangedListener(this);
        
        setColor(color);
    }
    
    private void setColor(int color) 
    {
        mOldColor.setColor(color);
        mColorPicker.setColor(color, true);
    }
    
    @Override
    public void onColorChanged(int color)
    {
        mNewColor.setColor(color);
        
        if ( !propagating ) hexText.setText(colorToRgbHexString(color));
        
        if (mListener != null)
        {
            mListener.onColorChanged(color);
        }
        
    }
    
    public String colorToRgbHexString(int color)
    {
        String strg = Integer.toHexString(color);
        if (!supportsAlpha && strg.length() == 8)
        {
            strg = strg.substring(2);
        }
        String val = "#" + strg; //$NON-NLS-1$
        return val;
    }
    
    public void setAlphaSliderVisible(boolean visible)
    {
        this.supportsAlpha = visible;
        mColorPicker.setAlphaSliderVisible(visible);
    }
    
    public int getColor()
    {
        return mColorPicker.getColor();
    }

    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
    }
    
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
        
    }
    
    @Override
    public void afterTextChanged(Editable s)
    {
       try 
       {
           int color = Color.parseColor(s.toString());
           propagating = true;
           mColorPicker.setColor(color, true);
           mNewColor.setColor(color);
           propagating = false;
       }
       catch ( Exception e ) 
       {
           //ignore
       }
    }
    
    public void setOnColorChangedListener(OnColorChangedListener listener)
    {
        mListener = listener;
    }
}
