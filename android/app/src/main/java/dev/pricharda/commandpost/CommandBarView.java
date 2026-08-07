package dev.pricharda.commandpost;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
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
 * On-screen command bar - D-31, gated by D-32, corrected by D-33.
 *
 * WHY THIS EXISTS. docs/KEYBOARD-AUDIT.md found sixteen live engine commands
 * with no route on a touch device. Four change how the game plays rather than
 * being conveniences: team groups, and the Shift / Ctrl / Alt modifiers.
 *
 * HOW IT REACHES THE ENGINE. SDLActivity exposes onNativeKeyDown(int) and
 * onNativeKeyUp(int) as public static natives taking Android KeyEvent codes.
 * Feeding them produces an ordinary SDL_KEYDOWN which the existing platform
 * keyboard path already handles. No engine change; both titles share it.
 *
 * WHY LATCHING MODIFIERS WORK - verified in source before building, because
 * the obvious approach does NOT work:
 *
 *   common/wwkeyboard.cpp:218  Put_Key_Message deliberately does NOT attach
 *                              modifier bits to MOUSE messages ("would be
 *                              incompatible with the dos version").
 *   tiberiandawn/techno.cpp:2593-2595 instead queries LIVE key state via
 *                              Keyboard->Down(Options.KeySelect1) etc.
 *   common/wwkeyboard.cpp:371  Put() sets DownState when a key is queued;
 *                              Down() reads that bit.
 *
 * So holding a modifier down - keyDown with no matching keyUp - makes
 * Keyboard->Down() return true and the engine behaves as with a physical key
 * held.
 *
 * -------------------------------------------------------------------------
 * D-33: PER-TITLE AVAILABILITY, AND THE LABELS
 * -------------------------------------------------------------------------
 *
 * Michael's ruling: do not delete buttons that are dead in one title but live
 * in the other - dim them per game instead. He was right, and checking proved
 * it. Two buttons I had written off as dead are fully implemented in Red
 * Alert:
 *
 *   FRM  tiberiandawn/conquer.cpp:642  "/* TODO, formations not implemented
 *                                       in TD yet. *\/"  - an empty block
 *        redalert/conquer.cpp:672      Toggle_Formation() - real
 *
 *   QUE  Tiberian Dawn                 no consumer anywhere in the engine
 *        redalert/techno.cpp:3528      Keyboard->Down(Options.KeyQueueMove1)
 *
 * That second line also corrected a design error of mine. QUE in Red Alert is
 * read as a HELD key, not a tapped one - it is a modifier you hold while
 * issuing move orders to queue them. My original button tapped it down and up
 * instantly, which could never have registered. It is now a latching button
 * like SHF/CTL/ALT.
 *
 * MAP stays enabled in both titles but does nothing without an active radar:
 * SidebarClass::Zoom_Mode_Control (sidebar.cpp:2626) wraps its whole body in
 * if (IsRadarActive). That is a runtime state, not a per-title fact, so it is
 * not dimmed - dimming it would be wrong the moment a Comm Center is built.
 *
 * LABELS. The first cut used key names (SHF/CTL/ALT), which cost Michael a
 * test cycle: he tried SHIFT to create a team, but Shift is additive select
 * and only Ctrl creates (Handle_Team, conquer.cpp). Labels now say what the
 * button DOES, not which key it sends.
 */
public class CommandBarView extends LinearLayout {

    private static final int COLOR_TEXT     = Color.parseColor("#E8C46A");
    private static final int COLOR_TEXT_ON  = Color.parseColor("#101012");
    private static final int COLOR_TEXT_OFF = Color.parseColor("#4A4A52");
    private static final int COLOR_HINT     = Color.parseColor("#8A8A8F");

    private static final int GLASS_TOP      = Color.parseColor("#59202028");
    private static final int GLASS_BOTTOM   = Color.parseColor("#7A0E0E12");
    private static final int GLASS_STROKE   = Color.parseColor("#33E8C46A");

    private static final int KEY_TOP        = Color.parseColor("#4DFFFFFF");
    private static final int KEY_BOTTOM     = Color.parseColor("#1AFFFFFF");
    private static final int KEY_STROKE     = Color.parseColor("#40E8C46A");

    private static final int KEY_ON_TOP     = Color.parseColor("#FFF0D48A");
    private static final int KEY_ON_BOTTOM  = Color.parseColor("#FFD9B45C");

    // Momentary flash on a one-shot press, so a tap that produces no visible
    // game effect is still obviously registered.
    private static final int KEY_HIT_TOP    = Color.parseColor("#CCE8C46A");
    private static final int KEY_HIT_BOTTOM = Color.parseColor("#99C9A24E");
    private static final int FLASH_MS       = 120;

    private static final int KEY_OFF_TOP    = Color.parseColor("#14FFFFFF");
    private static final int KEY_OFF_BOTTOM = Color.parseColor("#0AFFFFFF");
    private static final int KEY_OFF_STROKE = Color.parseColor("#1AE8C46A");

    /** Which engine is running. Decides per-title availability. */
    private final String engine;
    private final boolean isRedAlert;

    private static final class Mod {
        final int keycode;
        boolean held;
        TextView view;
        Mod(int k) { keycode = k; }
    }

    private final List<Mod> mods = new ArrayList<>();
    private final Handler ui = new Handler(Looper.getMainLooper());
    private LinearLayout panel;
    private TextView tab;
    private TextView hint;
    private boolean expanded = false;
    private boolean inGame = false;

    public CommandBarView(Context ctx, String engineName) {
        super(ctx);
        engine = engineName;
        isRedAlert = "vanillara".equals(engineName);

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        panel = buildPanel(ctx);
        panel.setVisibility(GONE);
        tab = buildTab(ctx);

        addView(tab);
        addView(panel);

        setVisibility(GONE);
    }

    private int dp(float v) {
        return (int) (getResources().getDisplayMetrics().density * v);
    }

    private GradientDrawable glass(int top, int bottom, int stroke, float radiusDp, boolean leftFlat) {
        final GradientDrawable g = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, new int[] { top, bottom });
        g.setStroke(dp(1), stroke);

        final float r = dp(radiusDp);
        if (leftFlat) {
            g.setCornerRadii(new float[] { 0, 0, r, r, r, r, 0, 0 });
        } else {
            g.setCornerRadius(r);
        }
        return g;
    }

    private TextView buildTab(Context ctx) {
        final TextView t = new TextView(ctx);
        t.setText("▸");
        t.setTextColor(COLOR_TEXT);
        t.setTextSize(18);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setBackground(glass(GLASS_TOP, GLASS_BOTTOM, GLASS_STROKE, 10, true));
        t.setLayoutParams(new LayoutParams(dp(28), LayoutParams.MATCH_PARENT));

        t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                panel.setVisibility(expanded ? VISIBLE : GONE);
                t.setText(expanded ? "◂" : "▸");
            }
        });

        return t;
    }

    private LinearLayout buildPanel(Context ctx) {
        final LinearLayout col = new LinearLayout(ctx);
        col.setOrientation(VERTICAL);
        col.setBackground(glass(GLASS_TOP, GLASS_BOTTOM, GLASS_STROKE, 12, false));
        col.setPadding(dp(6), dp(4), dp(6), dp(4));
        col.setLayoutParams(new LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        /*
        ** Modifiers, labelled by effect rather than by key. Verified against
        ** Handle_Team(team, action) in conquer.cpp:
        **    no modifier -> action 0  recall team
        **    Shift       -> action 1  additive select
        **    Ctrl        -> action 2  CREATE team
        **    Alt         -> action 3  recall and centre map
        ** Michael lost a test cycle to "SHF" reading as create. It does not.
        */
        col.addView(row(ctx, new TextView[] {
            modKey(ctx, "ADD",  KeyEvent.KEYCODE_SHIFT_LEFT, true,
                   "ADD: tap units to add to selection"),
            modKey(ctx, "TEAM", KeyEvent.KEYCODE_CTRL_LEFT,  true,
                   "TEAM: tap # to create that team"),
            modKey(ctx, "GOTO", KeyEvent.KEYCODE_ALT_LEFT,   true,
                   "GOTO: tap # to recall and centre"),
        }));

        col.addView(row(ctx, new TextView[] {
            cmdKey(ctx, "1", KeyEvent.KEYCODE_1, true), cmdKey(ctx, "2", KeyEvent.KEYCODE_2, true),
            cmdKey(ctx, "3", KeyEvent.KEYCODE_3, true), cmdKey(ctx, "4", KeyEvent.KEYCODE_4, true),
            cmdKey(ctx, "5", KeyEvent.KEYCODE_5, true)
        }));

        col.addView(row(ctx, new TextView[] {
            cmdKey(ctx, "6", KeyEvent.KEYCODE_6, true), cmdKey(ctx, "7", KeyEvent.KEYCODE_7, true),
            cmdKey(ctx, "8", KeyEvent.KEYCODE_8, true), cmdKey(ctx, "9", KeyEvent.KEYCODE_9, true),
            cmdKey(ctx, "0", KeyEvent.KEYCODE_0, true)
        }));

        // FORM is Red Alert only - Toggle_Formation() exists there, while
        // Tiberian Dawn's handler is an empty TODO block.
        col.addView(row(ctx, new TextView[] {
            cmdKey(ctx, "STOP", KeyEvent.KEYCODE_S, true),
            cmdKey(ctx, "GUARD", KeyEvent.KEYCODE_G, true),
            cmdKey(ctx, "SCAT", KeyEvent.KEYCODE_X, true),
            cmdKey(ctx, "FORM", KeyEvent.KEYCODE_F, isRedAlert),
            // QUEUE is Red Alert only AND is a HELD key there, not a tap:
            // techno.cpp:3528 reads it with Keyboard->Down().
            modKey(ctx, "QUEUE", KeyEvent.KEYCODE_Q, isRedAlert,
                   "QUEUE: give orders to queue them")
        }));

        // MAP is deliberately NOT dimmed. It works in both titles but needs an
        // active radar, which is runtime state rather than a per-title fact.
        col.addView(row(ctx, new TextView[] {
            cmdKey(ctx, "NEXT", KeyEvent.KEYCODE_N, true),
            cmdKey(ctx, "PREV", KeyEvent.KEYCODE_B, true),
            cmdKey(ctx, "BASE", KeyEvent.KEYCODE_H, true),
            cmdKey(ctx, "ALL",  KeyEvent.KEYCODE_E, true),
            cmdKey(ctx, "MAP",  KeyEvent.KEYCODE_U, true)
        }));

        hint = new TextView(ctx);
        hint.setText(isRedAlert ? "" : "FORM/QUEUE: Red Alert only");
        hint.setTextColor(COLOR_HINT);
        hint.setTextSize(9);
        hint.setTypeface(Typeface.MONOSPACE);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, dp(3), 0, 0);
        col.addView(hint);

        return col;
    }

    private LinearLayout row(Context ctx, TextView[] keys) {
        final LinearLayout r = new LinearLayout(ctx);
        r.setOrientation(HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, 0, 1f));

        for (TextView k : keys) {
            r.addView(k);
        }
        return r;
    }

    private TextView baseKey(Context ctx, String label, boolean enabled) {
        final TextView t = new TextView(ctx);
        t.setText(label);
        t.setTextSize(label.length() > 4 ? 9 : 11);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);

        if (enabled) {
            t.setTextColor(COLOR_TEXT);
            t.setBackground(glass(KEY_TOP, KEY_BOTTOM, KEY_STROKE, 8, false));
        } else {
            // Dimmed, not removed. The command is real - it simply is not
            // implemented in the title currently loaded.
            t.setTextColor(COLOR_TEXT_OFF);
            t.setBackground(glass(KEY_OFF_TOP, KEY_OFF_BOTTOM, KEY_OFF_STROKE, 8, false));
        }

        final LayoutParams lp = new LayoutParams(dp(46), LayoutParams.MATCH_PARENT);
        lp.setMargins(dp(3), dp(3), dp(3), dp(3));
        t.setLayoutParams(lp);
        return t;
    }

    /** A one-shot command: pressed and released immediately, with a flash. */
    private TextView cmdKey(Context ctx, String label, final int keycode, boolean enabled) {
        final TextView t = baseKey(ctx, label, enabled);

        if (!enabled) {
            return t; // no listener: dimmed buttons do nothing at all
        }

        t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SDLActivity.onNativeKeyDown(keycode);
                SDLActivity.onNativeKeyUp(keycode);
                flash(t);
            }
        });
        return t;
    }

    /**
     * Momentary highlight so a press is visibly acknowledged.
     *
     * Several commands have no visible effect without a precondition - STOP
     * with nothing selected, MAP with no radar. Without this, "I tapped it and
     * nothing happened" cannot be told apart from "the button is broken",
     * which is exactly the ambiguity that cost a round of testing.
     */
    private void flash(final TextView t) {
        t.setBackground(glass(KEY_HIT_TOP, KEY_HIT_BOTTOM, KEY_ON_BOTTOM, 8, false));
        t.setTextColor(COLOR_TEXT_ON);

        ui.postDelayed(new Runnable() {
            @Override
            public void run() {
                t.setBackground(glass(KEY_TOP, KEY_BOTTOM, KEY_STROKE, 8, false));
                t.setTextColor(COLOR_TEXT);
            }
        }, FLASH_MS);
    }

    /**
     * A latching modifier: held until tapped again.
     *
     * No auto-release after one action. Shift-selecting six units is six taps,
     * and a latch that dropped after the first would be worse than none.
     */
    private TextView modKey(Context ctx, String label, final int keycode,
                            boolean enabled, final String hintText) {
        final Mod m = new Mod(keycode);
        final TextView t = baseKey(ctx, label, enabled);
        m.view = t;

        if (!enabled) {
            return t;
        }

        mods.add(m);

        t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m.held = !m.held;

                if (m.held) {
                    SDLActivity.onNativeKeyDown(m.keycode);
                    t.setBackground(glass(KEY_ON_TOP, KEY_ON_BOTTOM, KEY_ON_BOTTOM, 8, false));
                    t.setTextColor(COLOR_TEXT_ON);
                    if (hint != null) hint.setText(hintText);
                } else {
                    SDLActivity.onNativeKeyUp(m.keycode);
                    t.setBackground(glass(KEY_TOP, KEY_BOTTOM, KEY_STROKE, 8, false));
                    t.setTextColor(COLOR_TEXT);
                    if (hint != null) hint.setText(defaultHint());
                }
            }
        });

        return t;
    }

    private String defaultHint() {
        for (Mod m : mods) {
            if (m.held) {
                return "";  // another modifier still latched; leave its text
            }
        }
        return isRedAlert ? "" : "FORM/QUEUE: Red Alert only";
    }

    /** D-32. Show only during gameplay. */
    public void setInGame(boolean value) {
        if (inGame == value) {
            return;
        }
        inGame = value;

        if (!inGame) {
            releaseAllModifiers();
        }
        setVisibility(inGame ? VISIBLE : GONE);
    }

    /** Release every latched modifier and restore its unlatched appearance. */
    public void releaseAllModifiers() {
        for (Mod m : mods) {
            if (m.held) {
                m.held = false;
                SDLActivity.onNativeKeyUp(m.keycode);
                m.view.setBackground(glass(KEY_TOP, KEY_BOTTOM, KEY_STROKE, 8, false));
                m.view.setTextColor(COLOR_TEXT);
            }
        }
        if (hint != null) {
            hint.setText(defaultHint());
        }
    }

    /**
     * Attach to SDL's own layout, spanning the full height of the left edge.
     * Left rather than right because both titles put their sidebar on the right.
     */
    public static CommandBarView attach(SDLActivity activity, ViewGroup sdlLayout, String engineName) {
        final CommandBarView bar = new CommandBarView(activity, engineName);

        final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        sdlLayout.addView(bar, lp);
        return bar;
    }
}
