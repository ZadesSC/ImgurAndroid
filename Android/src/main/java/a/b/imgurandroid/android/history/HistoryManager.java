package a.b.imgurandroid.android.history;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Stack;

/**
 * Created by zades on 10/31/2015.
 *
 * Idea behind this is to store the array of history (up to a limit of 10 currently) in shared preferences by
 * serializing the array.  For a series of strings, it feels too costly to use the sql or internal storage.
 */
public class HistoryManager
{
    private static final String PREFERENCES = "myPrefs";
    private static final String HISTORY = "history";
    private static final Type HISTORY_TYPE = new TypeToken<Stack<String>>(){}.getType();

    private SharedPreferences prefs;
    private Stack<String> historyStack;

    public HistoryManager(Context context)
    {
        this.prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        String savedStr = this.prefs.getString(HISTORY, "");
        if(savedStr.equals(""))
        {
            this.historyStack = new Stack();
        }
        else
        {
            this.historyStack = new Gson().fromJson(savedStr, HISTORY_TYPE);
        }

    }

    public void store(String str)
    {
        this.historyStack.push(str);
        this.saveStack();
    }

    public String retreive()
    {
        if(!this.historyStack.isEmpty())
        {
            String poppedHistory = this.historyStack.pop();
            this.saveStack();
            return poppedHistory;
        }
        return null;
    }

    private void saveStack()
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(HISTORY, new Gson().toJson(this.historyStack));
    }

}
