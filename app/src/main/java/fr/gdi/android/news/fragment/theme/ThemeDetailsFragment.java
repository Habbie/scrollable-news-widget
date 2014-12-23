package fr.gdi.android.news.fragment.theme;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.gdi.android.news.R;
import fr.gdi.android.news.model.Theme;
import fr.gdi.android.news.preference.color.ColorPickerPreference;
import fr.gdi.android.news.preference.layout.LayoutChoicePreference;

//doesn't feel right but is there any drastically better way? 
public class ThemeDetailsFragment extends PreferenceFragment implements OnPreferenceChangeListener
{
    private static final String DATE_FORMAT = "date_format"; //$NON-NLS-1$

    public static final String THEME_KEY = "theme"; //$NON-NLS-1$

    private static final String THEME_NAME = "theme_name"; //$NON-NLS-1$

    private static final String WIDGET_ROUNDED_CORNERS = "widget_rounded_corners"; //$NON-NLS-1$
    private static final String WIDGET_SHOW_TITLE = "widget_show_title"; //$NON-NLS-1$
    private static final String WIDGET_BACKGROUND_OPACITY = "widget_background_opacity"; //$NON-NLS-1$
    private static final String WIDGET_TITLE_COLOR = "widget_title_color"; //$NON-NLS-1$
    private static final String WIDGET_BACKGROUND_COLOR = "widget_background_color"; //$NON-NLS-1$
    private static final String LAYOUT_NUM_COLUMNS = "widget_num_columns"; //$NON-NLS-1$
    
    private static final String STORY_TITLE_MAX_LINES = "story_title_max_lines"; //$NON-NLS-1$
    private static final String STORY_TITLE_UPPERCASE = "story_title_uppercase"; //$NON-NLS-1$
    private static final String STORY_TITLE_FONT_SIZE = "story_title_font_size"; //$NON-NLS-1$
    private static final String STORY_TITLE_COLOR = "story_title_color"; //$NON-NLS-1$
    private static final String STORY_TITLE_HIDE = "story_title_hide"; //$NON-NLS-1$

    private static final String STORY_DESCRIPTION_MAX_WORDS = "story_description_max_words"; //$NON-NLS-1$
    private static final String STORY_DESCRIPTION_FONT_SIZE = "story_description_font_size"; //$NON-NLS-1$
    private static final String STORY_DESCRIPTION_COLOR = "story_description_color"; //$NON-NLS-1$
    
    private static final String STORY_FOOTER_DATE_COLOR = "story_date_color"; //$NON-NLS-1$
    private static final String STORY_FOOTER_AUTHOR_COLOR = "story_author_color"; //$NON-NLS-1$
    private static final String STORY_FOOTER_FONT_SIZE = "story_footer_font_size"; //$NON-NLS-1$
    private static final String STORY_FOOTER_UPPERCASE = "story_footer_uppercase"; //$NON-NLS-1$
    private static final String STORY_FOOTER_SHOW = "story_footer_show"; //$NON-NLS-1$

    private static final String STORY_LAYOUT_THUMBNAIL_SIZE = "thumbnail_size"; //$NON-NLS-1$
    private static final String STORY_LAYOUT_CHOICE = "story_layout"; //$NON-NLS-1$
    

    private Theme theme;

    private EditTextPreference themeNamePreference;

    //global appearance tweaks
    private ColorPickerPreference backgroundColorPreference;
    private EditTextPreference backgroundOpacityPreference;
    private CheckBoxPreference roundedCornersPreference;
    private CheckBoxPreference showTitlePreference;
    private ColorPickerPreference widgetTitleColorPreference;
    private EditTextPreference dateFormatPreference; 
    
    //story title tweaks
    private ColorPickerPreference storyTitleColorPreference;
    private EditTextPreference storyTitleFontSizePreference;
    private CheckBoxPreference storyTitleHidePreference;
    private CheckBoxPreference storyTitleUppercasePreference;
    private EditTextPreference storyTitleMaxLinesPreference;

    
    //story description tweaks
    private ColorPickerPreference storyDescriptionColorPreference;
    private EditTextPreference storyDescriptionFontSizePreference;
    private EditTextPreference storyDescriptionMaxWordsPreference;
    
    //story footer tweaks
    private CheckBoxPreference storyFooterShowPreference;
    private CheckBoxPreference storyFooterUppercasePreference;
    private EditTextPreference storyFooterFontSizePreference;
    private ColorPickerPreference storyAuthorColorPreference;
    private ColorPickerPreference storyDateColorPreference;
    
    //story layout tweaks
    private LayoutChoicePreference storyLayoutPreference;
    private ListPreference widgetNumColumnsPreference; 
    private EditTextPreference thumbnailSizePreference;
    
    
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        if ( getArguments() != null )
        {
            theme = (Theme) getArguments().getSerializable(THEME_KEY);
        }
        addPreferencesFromResource(R.xml.theme_details);
        
        setup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.buttonized_list, null);
    }
    
    private void setup()
    {
        themeNamePreference = (EditTextPreference) findPreference(THEME_NAME);
        themeNamePreference.setOnPreferenceChangeListener(this);
        
        //global appearance
        backgroundColorPreference = (ColorPickerPreference) findPreference(WIDGET_BACKGROUND_COLOR);
        backgroundOpacityPreference = (EditTextPreference) findPreference(WIDGET_BACKGROUND_OPACITY);
        roundedCornersPreference = (CheckBoxPreference) findPreference(WIDGET_ROUNDED_CORNERS);
        showTitlePreference = (CheckBoxPreference) findPreference(WIDGET_SHOW_TITLE);
        widgetTitleColorPreference = (ColorPickerPreference) findPreference(WIDGET_TITLE_COLOR);
        dateFormatPreference = (EditTextPreference) findPreference(DATE_FORMAT);
        
        if ( VERSION.SDK_INT <= VERSION_CODES.HONEYCOMB_MR2 ) 
        {
            showTitlePreference.setEnabled(false);
        }
        
        backgroundColorPreference.setOnPreferenceChangeListener(this);
        backgroundOpacityPreference.setOnPreferenceChangeListener(this);
        roundedCornersPreference.setOnPreferenceChangeListener(this);
        showTitlePreference.setOnPreferenceChangeListener(this);
        widgetTitleColorPreference.setOnPreferenceChangeListener(this);
        dateFormatPreference.setOnPreferenceChangeListener(this);
        
        //title 
        storyTitleColorPreference = (ColorPickerPreference) findPreference(STORY_TITLE_COLOR);
        storyTitleFontSizePreference = (EditTextPreference) findPreference(STORY_TITLE_FONT_SIZE);
        storyTitleHidePreference = (CheckBoxPreference) findPreference(STORY_TITLE_HIDE);
        storyTitleUppercasePreference = (CheckBoxPreference) findPreference(STORY_TITLE_UPPERCASE);
        storyTitleMaxLinesPreference = (EditTextPreference) findPreference(STORY_TITLE_MAX_LINES);
        
        storyTitleColorPreference.setOnPreferenceChangeListener(this);
        storyTitleFontSizePreference.setOnPreferenceChangeListener(this);
        storyTitleUppercasePreference.setOnPreferenceChangeListener(this);
        storyTitleHidePreference.setOnPreferenceChangeListener(this);
        storyTitleMaxLinesPreference.setOnPreferenceChangeListener(this);
        
        //description
        storyDescriptionColorPreference = (ColorPickerPreference) findPreference(STORY_DESCRIPTION_COLOR);
        storyDescriptionFontSizePreference = (EditTextPreference) findPreference(STORY_DESCRIPTION_FONT_SIZE);
        storyDescriptionMaxWordsPreference = (EditTextPreference) findPreference(STORY_DESCRIPTION_MAX_WORDS);
        
        storyDescriptionColorPreference.setOnPreferenceChangeListener(this);
        storyDescriptionFontSizePreference.setOnPreferenceChangeListener(this);
        storyDescriptionMaxWordsPreference.setOnPreferenceChangeListener(this);
        
        //footer
        storyFooterShowPreference = (CheckBoxPreference) findPreference(STORY_FOOTER_SHOW);
        storyFooterUppercasePreference = (CheckBoxPreference) findPreference(STORY_FOOTER_UPPERCASE);
        storyFooterFontSizePreference = (EditTextPreference) findPreference(STORY_FOOTER_FONT_SIZE);
        storyAuthorColorPreference = (ColorPickerPreference) findPreference(STORY_FOOTER_AUTHOR_COLOR);
        storyDateColorPreference = (ColorPickerPreference) findPreference(STORY_FOOTER_DATE_COLOR);
        
        storyFooterUppercasePreference.setDependency(STORY_FOOTER_SHOW);
        storyFooterFontSizePreference.setDependency(STORY_FOOTER_SHOW);
        storyAuthorColorPreference.setDependency(STORY_FOOTER_SHOW);
        storyDateColorPreference.setDependency(STORY_FOOTER_SHOW);
        
        storyFooterShowPreference.setOnPreferenceChangeListener(this);
        storyFooterUppercasePreference.setOnPreferenceChangeListener(this);
        storyFooterFontSizePreference.setOnPreferenceChangeListener(this);
        storyAuthorColorPreference.setOnPreferenceChangeListener(this);
        storyDateColorPreference.setOnPreferenceChangeListener(this);
        
        //layout
        storyLayoutPreference = (LayoutChoicePreference) findPreference(STORY_LAYOUT_CHOICE);
        widgetNumColumnsPreference = (ListPreference) findPreference(LAYOUT_NUM_COLUMNS);
        thumbnailSizePreference = (EditTextPreference) findPreference(STORY_LAYOUT_THUMBNAIL_SIZE);
        
        storyLayoutPreference.setOnPreferenceChangeListener(this);
        widgetNumColumnsPreference.setOnPreferenceChangeListener(this);
        thumbnailSizePreference.setOnPreferenceChangeListener(this);
        
        setPreferenceValues();
    }
    
    private void setPreferenceValues() 
    {
        themeNamePreference.setText(theme.getName());
        
        backgroundColorPreference.setValue(theme.getBackgroundColor());
        backgroundOpacityPreference.setText(Integer.toString(theme.getBackgroundOpacity()));
        roundedCornersPreference.setChecked(theme.isRoundedCorners());
        showTitlePreference.setChecked(theme.isShowWidgetTitle());
        widgetTitleColorPreference.setValue(theme.getWidgetTitleColor());
        dateFormatPreference.setText(theme.getDateFormat());
        
        storyTitleColorPreference.setValue(theme.getStoryTitleColor());
        storyTitleFontSizePreference.setText(Integer.toString(theme.getStoryTitleFontSize()));
        storyTitleHidePreference.setChecked(theme.isStoryTitleHide());
        storyTitleUppercasePreference.setChecked(theme.isStoryTitleUppercase());
        storyTitleMaxLinesPreference.setText(Integer.toString(theme.getStoryTitleMaxLines()));
        
        storyDescriptionColorPreference.setValue(theme.getStoryDescriptionColor());
        storyDescriptionFontSizePreference.setText(Integer.toString(theme.getStoryDescriptionFontSize()));
        storyDescriptionMaxWordsPreference.setText(Integer.toString(theme.getStoryDescriptionMaxWordCount()));
        
        storyFooterShowPreference.setChecked(theme.isShowFooter());
        storyFooterUppercasePreference.setChecked(theme.isFooterUppercase());
        storyFooterFontSizePreference.setText(Integer.toString(theme.getFooterFontSize()));
        storyAuthorColorPreference.setValue(theme.getStoryAuthorColor());
        storyDateColorPreference.setValue(theme.getStoryDateColor());
        
        storyLayoutPreference.setValue(theme.getLayout());
        widgetNumColumnsPreference.setValue(Integer.toString(theme.getNumColumns()));
        thumbnailSizePreference.setText(Integer.toString(theme.getThumbnailSize()));
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        if ( theme == null ) return false;

        String k = preference.getKey();
        
        globalAppearanceChanged(k, newValue);
        storyTitleAppearanceChanged(k, newValue);
        storyDescriptionAppearanceChanged(k, newValue);
        storyFooterAppearanceChanged(k, newValue);
        storyLayoutAppearanceChanged(k, newValue);
        
        return true;
    }

    private void storyLayoutAppearanceChanged(String k, Object newValue)
    {
        if ( TextUtils.equals(k, STORY_LAYOUT_THUMBNAIL_SIZE) ) 
        {
            theme.setThumbnailSize(getInteger(newValue));
        }
        if ( TextUtils.equals(k, STORY_LAYOUT_CHOICE) ) 
        {
            theme.setLayout(getInteger(newValue));
        }
        if ( TextUtils.equals(k, LAYOUT_NUM_COLUMNS) ) 
        {
            theme.setNumColumns(getInteger(newValue));
        }
    }
    
    private void storyFooterAppearanceChanged(String k, Object newValue)
    {
        if ( TextUtils.equals(k, STORY_FOOTER_FONT_SIZE) ) 
        {
            theme.setFooterFontSize(getInteger(newValue));
        }
        if ( TextUtils.equals(k, STORY_FOOTER_AUTHOR_COLOR) ) 
        {
            theme.setStoryAuthorColor(getInteger(newValue));
        }
        if ( TextUtils.equals(k, STORY_FOOTER_DATE_COLOR) ) 
        {
            theme.setStoryDateColor(getInteger(newValue));
        }
        if ( TextUtils.equals(k, STORY_FOOTER_SHOW) ) 
        {
            theme.setShowFooter((Boolean) newValue);
        }
        if ( TextUtils.equals(k, STORY_FOOTER_UPPERCASE) ) 
        {
            theme.setFooterUppercase((Boolean) newValue);
        }
    }
    
    private void storyTitleAppearanceChanged(String k, Object newValue)
    {
        if ( TextUtils.equals(k, STORY_TITLE_FONT_SIZE) ) 
        {
            theme.setStoryTitleFontSize(getInteger(newValue));
        }
        if ( TextUtils.equals(k, STORY_TITLE_MAX_LINES) ) 
        {
            theme.setStoryTitleMaxLines(getInteger(newValue));
        }
        if ( TextUtils.equals(k, STORY_TITLE_UPPERCASE) ) 
        {
            theme.setStoryTitleUppercase((Boolean) newValue);
        }
        if ( TextUtils.equals(k, STORY_TITLE_HIDE) ) 
        {
            theme.setStoryTitleHide((Boolean) newValue);
        }
        if ( TextUtils.equals(k, STORY_TITLE_COLOR) ) 
        {
            theme.setStoryTitleColor(getInteger(newValue));
        }
    }
    
    private void storyDescriptionAppearanceChanged(String k, Object newValue)
    {
        if ( TextUtils.equals(k, STORY_DESCRIPTION_FONT_SIZE) ) 
        {
            theme.setStoryDescriptionFontSize(getInteger(newValue));
        }
        if ( TextUtils.equals(k, STORY_DESCRIPTION_MAX_WORDS) ) 
        {
            theme.setStoryDescriptionMaxWordCount(getInteger(newValue));
        }
        if ( TextUtils.equals(k, STORY_DESCRIPTION_COLOR) ) 
        {
            theme.setStoryDescriptionColor(getInteger(newValue));
        }
    }
    
    private void globalAppearanceChanged(String k, Object newValue)
    {
        if ( TextUtils.equals(k, THEME_NAME) ) 
        {
            theme.setName((String) newValue);
        }
        if ( TextUtils.equals(k, DATE_FORMAT) ) 
        {
            theme.setDateFormat((String) newValue);
        }
        if ( TextUtils.equals(k, WIDGET_ROUNDED_CORNERS) ) 
        {
            theme.setRoundedCorners(newValue == null ? false : (Boolean) newValue);
        }
        if ( TextUtils.equals(k, WIDGET_SHOW_TITLE) ) 
        {
            theme.setShowWidgetTitle(newValue == null ? false : (Boolean) newValue);
        }
        if ( TextUtils.equals(k, WIDGET_BACKGROUND_OPACITY) ) 
        {
            theme.setBackgroundOpacity(getInteger(newValue));
        }
        if ( TextUtils.equals(k, WIDGET_BACKGROUND_COLOR) )
        {
            theme.setBackgroundColor(getInteger(newValue));
        }
        if ( TextUtils.equals(k, WIDGET_TITLE_COLOR) )
        {
            theme.setWidgetTitleColor(getInteger(newValue));
        }
    }
    
    private int getInteger(Object newValue)
    {
        if ( newValue instanceof String ) 
        {
            try 
            {
                return Integer.parseInt((String) newValue);
            }
            catch ( Exception e ) 
            {
                return 0;
            }
        }
        if ( newValue instanceof Integer ) return (Integer) newValue;
        
        return 0;
        
    }

    public Theme getTheme()
    {
        return theme;
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }
}
