package fr.gdi.android.news.data;

import android.database.Cursor;

public interface IQueryCallback
{
    void doInTransaction(Cursor cursor);
}
