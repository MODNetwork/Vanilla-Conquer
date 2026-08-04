package dev.pricharda.commandpost;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.libsdl.app.SDLActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * On-screen command bar - D-31.
 *
 * WHY THIS EXISTS. docs/KEYBOARD-AUDIT.md found sixteen live engine commands
 * with no route on a touch device. Four of them change how the game plays
 * rather than merely being conveniences: team groups, and the Shift / Ctrl /
 * Alt modifiers.
 *
 * HOW IT REACHES THE ENGINE. SDLActivity exposes onNativeKeyDown(int) and
 * onNativeKeyUp(int) as public static natives taking Android KeyEvent codes.
 * Feeding them produces an ordinary SDL_KEYDOWN, which the existing platform
 * keyboard path already handles. No engine change, no new input route, and
 * Red Alert gets it for free because the path is shared.
 *
 * WHY LATCHING MODIFIERS WORK - verified in the engine source before building
 * this, because the obvious approach does NOT work:
 *
 *   common/wwkeyboard.cpp:218  Put_Key_Message deliberately does NOT attach
 *                              modifier bits to MOUSE messages ("would be
 *                              incompatible with the dos version"), so a
 *                              modifier cannot simply ride along on a click.
 *
 *   tiberiandawn/techno.cpp:2593-2595 instead queries LIVE key state:
 *                              Keyboard->Down(Options.KeySelect1) etc.
 *
 *   common/wwkeyboard.cpp:371  Put() sets DownState when a key is queued, and
 *                              Down() reads that bit.
 *
 * So holding a modifier down - keyDown with no matching keyUp - makes
 * Keyboard->Down() return true and the engine behaves exactly as it does with
 * a physical key held. That is what makes a latch the correct mechanism here
 * rather than a workaround.
 *
 * A consequence worth noting: team CREATE falls out for free. The engine reads
 * Ctrl/Alt when a team key arrives, so latching CTRL and tapping "3" creates
 * team 3, exactly as on desktop. Nothing here special-cases it.
 *
 * FOOTPRINT. The bar is added to SDLActivity's own RelativeLayout as a
 * wrap-content child pinned to an edge, so it occupies only its own bounds.
 * Touches anywhere else fall through to the SDL surface untouched - the game's
 * gestures are unaffected.
 */
public class CommandBarView extends LinearLayout {

    private static final int COLOR_PANEL   = Color.parseColor("#CC121214");
    private static final int COLOR_KEY     = Color.parseColor("#22222A");
    private static final int COLOR_KEY_ON  = Color.parseColor("#E8C46A");
    private static final int COLOR_TEXT    = Color.parseColor("#E8C46A");
    private static final int COLOR_TEXT_ON = Color.parseColor("#101012");
    private static final int COLOR_TAB     = Color.parseColor("#CC1C1C22");

    /** Latching modifiers: tapped once to hold, again to release. */
    private static final class Mod {
        final String label;
        final int keycode;
        boolean held;
        TextView view;
        Mod(String l, int k) { label = l; keycode = k; }
    }

    private final List<Mod> mods = new ArrayList<>();
    private LinearLayout panel;
    private boolean expanded = false;

    public CommandBarView(Context ctx) {
        super(ctx);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        panel = buildPanel(ctx);
        panel.setVisibility(GONE);

        addView(buildTab(ctx));
        addView(panel);
    }

    private int dp(float v) {
        return (int) (getResources().getDisplayMetrics().density * v);
    }

    /**
     * The always-visible handle. Deliberately narrow: when collapsed this is
     * the entire cost of the feature in screen area.
     */
    private TextView buildTab(Context ctx) {
        final TextView tab = new TextView(ctx);
        tab.setText("▸");            // right-pointing triangle
        tab.setTextColor(COLOR_TEXT);
        tab.setTextSize(18);
        tab.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tab.setGravity(Gravity.CENTER);
        tab.setBackgroundColor(COLOR_TAB);
        tab.setLayoutParams(new LayoutParams(dp(26), dp(56)));

        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                panel.setVisibility(expanded ? VISIBLE : GONE);
                tab.setText(expanded ? "◂" : "▸");
            }
        });

        return tab;
    }

    private LinearLayout buildPanel(Context ctx) {
        final LinearLayout col = new LinearLayout(ctx);
        col.setOrientation(VERTICAL);
        col.setBackgroundColor(COLOR_PANEL);
        col.setPadding(dp(4), dp(4), dp(4), dp(4));

        // Modifiers first: they change what every other button below does.
        final LinearLayout modRow = row(ctx);
        modRow.addView(modKey(ctx, "SHF", KeyEvent.KEYCODE_SHIFT_LEFT));
        modRow.addView(modKey(ctx, "CTL", KeyEvent.KEYCODE_CTRL_LEFT));
        modRow.addView(modKey(ctx, "ALT", KeyEvent.KEYCODE_ALT_LEFT));
        col.addView(modRow);

        // Team groups. With CTRL or ALT latched these CREATE a team; plain they
        // recall it. That behaviour is the engine's, not this view's.
        final LinearLayout teamA = row(ctx);
        teamA.addView(cmdKey(ctx, "1", KeyEvent.KEYCODE_1));
        teamA.addView(cmdKey(ctx, "2", KeyEvent.KEYCODE_2));
        teamA.addView(cmdKey(ctx, "3", KeyEvent.KEYCODE_3));
        teamA.addView(cmdKey(ctx, "4", KeyEvent.KEYCODE_4));
        teamA.addView(cmdKey(ctx, "5", KeyEvent.KEYCODE_5));
        col.addView(teamA);

        final LinearLayout teamB = row(ctx);
        teamB.addView(cmdKey(ctx, "6", KeyEvent.KEYCODE_6));
        teamB.addView(cmdKey(ctx, "7", KeyEvent.KEYCODE_7));
        teamB.addView(cmdKey(ctx, "8", KeyEvent.KEYCODE_8));
        teamB.addView(cmdKey(ctx, "9", KeyEvent.KEYCODE_9));
        teamB.addView(cmdKey(ctx, "0", KeyEvent.KEYCODE_0));
        col.addView(teamB);

        // Unit orders.
        final LinearLayout ordRow = row(ctx);
        ordRow.addView(cmdKey(ctx, "STP", KeyEvent.KEYCODE_S));
        ordRow.addView(cmdKey(ctx, "GRD", KeyEvent.KEYCODE_G));
        ordRow.addView(cmdKey(ctx, "SCT", KeyEvent.KEYCODE_X));
        ordRow.addView(cmdKey(ctx, "FRM", KeyEvent.KEYCODE_F));
        ordRow.addView(cmdKey(ctx, "QUE", KeyEvent.KEYCODE_Q));
        col.addView(ordRow);

        // Navigation and view.
        final LinearLayout navRow = row(ctx);
        navRow.addView(cmdKey(ctx, "NXT", KeyEvent.KEYCODE_N));
        navRow.addView(cmdKey(ctx, "PRV", KeyEvent.KEYCODE_B));
        navRow.addView(cmdKey(ctx, "BSE", KeyEvent.KEYCODE_H));
        navRow.addView(cmdKey(ctx, "VEW", KeyEvent.KEYCODE_E));
        navRow.addView(cmdKey(ctx, "MAP", KeyEvent.KEYCODE_U));
        col.addView(navRow);

        return col;
    }

    private LinearLayout row(Context ctx) {
        final LinearLayout r = new LinearLayout(ctx);
        r.setOrientation(HORIZONTAL);
        return r;
    }

    private TextView baseKey(Context ctx, String label) {
        final TextView t = new TextView(ctx);
        t.setText(label);
        t.setTextColor(COLOR_TEXT);
        t.setTextSize(11);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setBackgroundColor(COLOR_KEY);

        final LayoutParams lp = new LayoutParams(dp(42), dp(34));
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        t.setLayoutParams(lp);
        return t;
    }

    /** A one-shot command: press and release immediately, like a real tap. */
    private TextView cmdKey(Context ctx, String label, final int keycode) {
        final TextView t = baseKey(ctx, label);
        t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SDLActivity.onNativeKeyDown(keycode);
                SDLActivity.onNativeKeyUp(keycode);
            }
        });
        return t;
    }

    /**
     * A latching modifier: held down until tapped again.
     *
     * No auto-release after one action, deliberately. Shift-selecting a group
     * of six units means six taps, and a latch that dropped after the first
     * would be worse than useless.
     */
    private TextView modKey(Context ctx, String label, final int keycode) {
        final Mod m = new Mod(label, keycode);
        final TextView t = baseKey(ctx, label);
        m.view = t;
        mods.add(m);

        t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m.held = !m.held;

                if (m.held) {
                    SDLActivity.onNativeKeyDown(m.keycode);
                    t.setBackgroundColor(COLOR_KEY_ON);
                    t.setTextColor(COLOR_TEXT_ON);
                } else {
                    SDLActivity.onNativeKeyUp(m.keycode);
                    t.setBackgroundColor(COLOR_KEY);
                    t.setTextColor(COLOR_TEXT);
                }
            }
        });

        return t;
    }

    /**
     * Release every latched modifier.
     *
     * Called when the activity loses focus. A modifier left held across a
     * pause would still be held on resume from the engine's point of view,
     * and the player would have no idea why their clicks had stopped behaving.
     */
    public void releaseAllModifiers() {
        for (Mod m : mods) {
            if (m.held) {
                m.held = false;
                SDLActivity.onNativeKeyUp(m.keycode);
                m.view.setBackgroundColor(COLOR_KEY);
                m.view.setTextColor(COLOR_TEXT);
            }
        }
    }

    /**
     * Attach to SDL's own layout, pinned to the left edge and vertically
     * centred. Left rather than right because Tiberian Dawn's sidebar occupies
     * the right side of the screen.
     */
    public static CommandBarView attach(SDLActivity activity, ViewGroup sdlLayout) {
        final CommandBarView bar = new CommandBarView(activity);

        final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);

        sdlLayout.addView(bar, lp);
        return bar;
    }
}
