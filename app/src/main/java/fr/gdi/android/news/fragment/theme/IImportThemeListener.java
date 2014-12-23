package fr.gdi.android.news.fragment.theme;

import java.util.List;

import fr.gdi.android.news.model.Theme;

public interface IImportThemeListener
{

    enum Error 
    {
        NO_THEME_FOUND,
        FILE_PARSE_ERROR
    }
    
    void onThemeSelected(List<Theme> selected);

    void onThemeImportError(Error fileParseError);
    
}
