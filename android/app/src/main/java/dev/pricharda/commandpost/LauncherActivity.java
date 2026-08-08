package dev.pricharda.commandpost;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Title picker - the app's launch screen.
 *
 * D-25. The app hosts more than one engine, and the choice has to be made
 * before any native library is loaded. SDL calls System.loadLibrary() from
 * SDLActivity.onCreate() and the engine then owns the process for its lifetime;
 * a native library cannot be unloaded and swapped. So the selection happens
 * here, in a plain Android activity with no SDL involvement, and the chosen
 * engine is passed to CommandPostActivity as an intent extra.
 *
 * Built in code rather than XML deliberately: this screen has three widgets and
 * no state, and keeping it in one file means the whole launch path is readable
 * without cross-referencing a layout resource.
 *
 * Naming: the two titles are "Dawn" and "Red" (D-22). Those are bare common
 * words. The full Electronic Arts product names are trademarks and are not used
 * as this application's identity, per EA's GPL v3 Section 7 additional terms.
 */
public class LauncherActivity extends Activity {

    // Muted amber on near-black - legible in a dark room, and deliberately not
    // an imitation of any Electronic Arts art direction.
    private static final int COLOR_BG = Color.parseColor("#0B0B0C");
    private static final int COLOR_FG = Color.parseColor("#E8C46A");
    private static final int COLOR_DIM = Color.parseColor("#8A8A8F");
    private static final int COLOR_BUTTON = Color.parseColor("#1C1C1F");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(COLOR_BG);
        root.setPadding(64, 64, 64, 64);

        final TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextColor(COLOR_FG);
        title.setTextSize(34);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setLetterSpacing(0.22f);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        final TextView hint = new TextView(this);
        hint.setText(R.string.launcher_hint);
        hint.setTextColor(COLOR_DIM);
        hint.setTextSize(13);
        hint.setTypeface(Typeface.MONOSPACE);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 16, 0, 56);
        root.addView(hint);

        root.addView(makeTitleButton(R.string.title_td, "vanillatd"));
        root.addView(makeTitleButton(R.string.title_ra, "vanillara"));

        // D-29. Not optional garnish: the credits screen is where the GPL v3
        // modification notice, the corresponding-source statement and the
        // third-party notices are actually presented to a user. It has to be
        // reachable from the app's first screen.
        final TextView credits = new TextView(this);
        credits.setText(R.string.launcher_credits);
        credits.setTextColor(COLOR_DIM);
        credits.setTextSize(11);
        credits.setTypeface(Typeface.MONOSPACE);
        credits.setLetterSpacing(0.10f);
        credits.setGravity(Gravity.CENTER);
        credits.setPadding(0, 34, 0, 0);
        credits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LauncherActivity.this, CreditsActivity.class));
            }
        });
        root.addView(credits);

        setContentView(root);

        // MUST come after setContentView(). getWindow().getInsetsController()
        // resolves through the DecorView, which does not exist until a content
        // view is set - calling it before returns a null DecorView and throws
        // NullPointerException. F-12: this crashed the app on launch, because
        // the method was lifted from CommandPostActivity where it is only ever
        // called from onResume/onWindowFocusChanged, i.e. always after the view
        // hierarchy exists. The method was copied; its precondition was not.
        applyImmersiveMode();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Re-apply on focus gain, same as the game activity: the bars return
        // after a swipe, a notification, or the recents switcher.
        if (hasFocus) {
            applyImmersiveMode();
        }
    }

    private Button makeTitleButton(int labelRes, final String engine) {
        final Button b = new Button(this);
        b.setText(labelRes);
        b.setTextColor(COLOR_FG);
        b.setTextSize(22);
        b.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        b.setLetterSpacing(0.16f);
        b.setAllCaps(true);
        b.setBackgroundColor(COLOR_BUTTON);

        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            (int) (getResources().getDisplayMetrics().density * 260),
            (int) (getResources().getDisplayMetrics().density * 64));
        lp.setMargins(0, 0, 0, (int) (getResources().getDisplayMetrics().density * 18));
        b.setLayoutParams(lp);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launch(engine);
            }
        });

        return b;
    }

    /**
     * D-44: sentinel files proving a title's game data is present.
     *
     * The engine opens seventeen .MIX archives for Tiberian Dawn and twenty-one
     * for Red Alert, but it does NOT need all of them - modern GOG and
     * Remastered layouts pack most content into a few large archives. Michael's
     * own working Red Alert install has six files. Requiring the full list
     * would reject valid installs, so each title is probed with the one archive
     * the engine caches during init and genuinely cannot start without.
     *
     * Red Alert accepts either name because the two common layouts differ.
     */
    private static String[] sentinelsFor(String engine) {
        if ("vanillara".equals(engine)) {
            return new String[] {"REDALERT.MIX", "MAIN.MIX"};
        }
        return new String[] {"CONQUER.MIX"};
    }

    /** True when at least one sentinel archive for this title is readable. */
    private boolean hasGameData(String engine) {
        final java.io.File dir = new java.io.File(getExternalFilesDir(null), engine);
        for (String name : sentinelsFor(engine)) {
            final java.io.File f = new java.io.File(dir, name);
            if (f.isFile() && f.length() > 0 && f.canRead()) {
                return true;
            }
        }
        return false;
    }

    /**
     * D-44: explain missing game data instead of crashing on it.
     *
     * Without its .MIX archives the engine does not degrade - it dereferences a
     * null file handle and takes the process down. That was observed twice
     * during development, on both the debug and release builds, and it is what
     * every first-time installer would have seen: an instant crash with no
     * explanation. Dolphin and ScummVM both show a "no games found" screen for
     * exactly this reason.
     *
     * The wording states plainly that the game must be owned. This app ships no
     * game content, cannot be made to, and points at no source for obtaining
     * any.
     */
    private void showMissingDataDialog(final String engine) {
        final String title = "vanillara".equals(engine) ? "Red Alert" : "Tiberian Dawn";
        final java.io.File dir = new java.io.File(getExternalFilesDir(null), engine);

        new android.app.AlertDialog.Builder(this)
            .setTitle("No " + title + " game data found")
            .setMessage(
                "Command Post is a game engine. It contains no game content and "
                + "cannot play anything on its own.\n\n"
                + "To play " + title + " you must already own it, and copy that "
                + "copy's data files into:\n\n"
                + dir.getAbsolutePath() + "\n\n"
                + "Expected: " + android.text.TextUtils.join(" or ", sentinelsFor(engine))
                + ", plus the other .MIX files from your copy.\n\n"
                + "This app does not supply game files and does not link to "
                + "anywhere that does. Where you obtain them is your "
                + "responsibility.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void launch(String engine) {
        // D-44: check before starting the engine. Once SDL_main is running a
        // missing archive is a segfault, not a message.
        if (!hasGameData(engine)) {
            showMissingDataDialog(engine);
            return;
        }

        final Intent intent = new Intent(this, CommandPostActivity.class);
        intent.putExtra(CommandPostActivity.EXTRA_ENGINE, engine);

        // CommandPostActivity is singleInstance, which SDL requires. Clearing
        // the task first means a previously-run title's dead task cannot be
        // resurrected with the wrong engine already loaded into its process.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

        // Finish rather than sit behind the game. Returning from the engine
        // should leave the app, not drop back onto a launcher whose process
        // already has an engine library loaded.
        finish();
    }

    /**
     * Same immersive treatment as the game activity, so the launcher does not
     * flash the system bars for a frame before the engine hides them.
     */
    private void applyImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();

            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }
}
