package ga.lupuss.planlekcji.managers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ga.lupuss.planlekcji.BuildConfig;
import ga.lupuss.planlekcji.statics.Info;
import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.tools.Files;

public class ChangelogManager {

    private final String CHANGELOG_FILENAME = "changelog.txt";
    private final String CHANGELOG_VERSION_FILENAME = "changelog_version.txt";
    private List<String> changeLog = new ArrayList<>();
    private String changelogVersion;

    public void save(List<String> changeLog, String version) {

        File path = new File(Info.APP_FILES_DIR, CHANGELOG_FILENAME);
        File versionFile = new File(Info.APP_FILES_DIR, CHANGELOG_VERSION_FILENAME);

        changelogVersion = version;

        StringBuilder data = new StringBuilder("");

        for (String log : changeLog) {
            data.append(log);
            data.append(";");
        }
        try {

            Log.d(ChangelogManager.class.getName(), data.toString());
            Files.writeAllBytes(path, data.toString().getBytes());

            Log.d(ChangelogManager.class.getName(), changelogVersion);
            Files.writeAllBytes(versionFile, changelogVersion.getBytes());

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void load() {

        File file =  new File(Info.APP_FILES_DIR, CHANGELOG_FILENAME);
        File versionFile = new File(Info.APP_FILES_DIR, CHANGELOG_VERSION_FILENAME);

        loadChangelog(file);

        try {

            changelogVersion = new String(Files.readAllBytes(versionFile));

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public void clear() {

        File changelogFile = new File(Info.APP_FILES_DIR, CHANGELOG_FILENAME);
        File versionFile = new File(Info.APP_FILES_DIR, CHANGELOG_VERSION_FILENAME);

        try {
            Files.writeAllBytes(changelogFile, "".getBytes());
            Files.writeAllBytes(versionFile, "".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        changeLog.clear();

    }

    public void showIfReady(Context context, LayoutInflater inflater) {

        if (changeLog.isEmpty() || !versionControl()) {

            Log.d(ChangelogManager.class.getName(), "Changelog rejected");
            return;
        }

        @SuppressLint("InflateParams")
        ScrollView scrollView =
                (ScrollView) inflater.inflate(R.layout.scrollable_alert, null, false);

        ((TextView) scrollView.findViewById(R.id.textViewWithScroll)).setText(getChangelogString());

        new AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle(context.getString(R.string.changelog))
                .setView(scrollView)
                .setPositiveButton("OK", null)
                .show();

    }

    private boolean versionControl() {

        if (changelogVersion == null) {

            Log.d(ChangelogManager.class.getName(), "Changelog version control not supported");
            return true;
        }

        return changelogVersion.equals(BuildConfig.VERSION_NAME);
    }

    private String getChangelogString() {

        StringBuilder builder = new StringBuilder("");

        builder.append("Wersja ").append(BuildConfig.VERSION_NAME).append( ":\n\n");

        for( String log : changeLog ){
            builder.append( "- " ).append( log ).append( "\n\n" );
        }

        return builder.toString();
    }

    private void loadChangelog(File file) {

        if( file.exists() ){

            changeLog = new ArrayList<>();

            try {

                byte[] data = Files.readAllBytes(file);

                if (data == null || data.length == 0) {

                    Log.d(
                            ChangelogManager.class.getName(),
                            "Changelog status: Empty file"
                    );

                } else {
                    int start;
                    int breakLine = 0;
                    String dataString = new String(data, "UTF-8");

                    do {

                        start = breakLine;
                        breakLine = dataString.indexOf(';', breakLine);

                        if (breakLine != -1) {
                            changeLog.add(dataString.substring(start, breakLine));
                        }
                    } while (breakLine++ != -1);

                    Log.d(
                            ChangelogManager.class.getName(),
                            "Changelog status: " + (changeLog.isEmpty() ? "NONE" : "OK")
                    );
                }

            } catch(Exception e) {

                e.printStackTrace();

            }
        } else {

            Log.d(ChangelogManager.class.getName(), "Changelog status: File not found");

        }

    }
}
