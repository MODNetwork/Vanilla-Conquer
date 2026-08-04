package dev.pricharda.commandpost;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
 * On-screen command bar - D-31, restyled and gated by D-32.
 *
 * WHY THIS EXISTS. docs/KEYBOARD-AUDIT.md found sixteen live engine commands
 * with no route on a touch device. Four change how the game plays rather than
 * being conveniences: team groups, and the Shift / Ctrl / Alt modifiers.
 *
 * HOW IT REACHES THE ENGINE. SDLActivity exposes onNativeKeyDown(int) and
 * onNativeKeyUp(int) as public static natives taking Android KeyEvent codes.
 * Feeding them produces an ordinary SDL_KEYDOWN which the existing platform
 * keyboard path already handles. No engine change, and Red Alert inherits it
 * because the path is shared.
 *
 * WHY LATCHING MODIFIERS WORK - verified in the engine source before this was
 * built, because the obvious approach does NOT work:
 *
 *   common/wwkeyboard.cpp:218  Put_Key_Message deliberately does NOT attach
 *                              modifier bits to MOUSE messages ("would be
 *                              incompatible with the dos version"), so a
 *                              modifier cannot ride along on a click.
 *   tiberiandawn/techno.cpp:2593-2595 instead queries LIVE key state:
 *                              Keyboard->Down(Options.KeySelect1) etc.
 *   common/wwkeyboard.cpp:371  Put() sets DownState when a key is queued;
 *                              Down() reads that bit.
 *
 * So holding a modifier down - keyDown with no matching keyUp - makes
 * Keyboard->Down() return true and the engine behaves as with a physical key
 * held. Team CREATE then falls out for free: the engine reads Ctrl/Alt when a
 * team key arrives, so latching CTL and tapping 3 creates team 3. Nothing here
 * special-cases that.
 *
 * FOOTPRINT. Added to SDLActivity's own RelativeLayout as a wrap-content child
 * pinned to the left edge, so it occupies only its own bounds and every touch
 * elsewhere falls through to the SDL surface untouched.
 *
 * A NOTE ON "GLASSMORPHIC". True backdrop blur is not available here and is not
 * claimed. The game renders into a SurfaceView, which sits outside the view
 * hierarchy's render tree, so RenderEffect blur would blur this panel's own
 * content rather than what is behind it, and Window.setBackgroundBlurRadius
 * blurs behind the whole window - which the game already fills. What is used
 * instead is translucency, a vertical gradient, a hairline light border and
 * rounded corners, which is what actually reads as glass over a bright, moving
 * background.
 */
public class CommandBarView extends LinearLayout {

    // Same palette as the launcher and credits screens.
    private static final int COLOR_TEXT     = Color.parseColor("#E8C46A");
    private static final int COLOR_TEXT_ON  = Color.parseColor("#101012");

    // Glass: translucent, brighter at the top, with a hairline warm edge.
    private static final int GLASS_TOP      = Color.parseColor("#59202028");
    private static final int GLASS_BOTTOM   = Color.parseColor("#7A0E0E12");
    private static final int GLASS_STROKE   = Color.parseColor("#33E8C46A");

    private static final int KEY_TOP        = Color.parseColor("#4DFFFFFF");
    private static final int KEY_BOTTOM     = Color.parseColor("#1AFFFFFF");
    private static final int KEY_STROKE     = Color.parseColor("#40E8C46A");

    private static final int KEY_ON_TOP     = Color.parseColor("#FFF0D48A");
    private static final int KEY_ON_BOTTOM  = Color.parseColor("#FFD9B45C");

    /** Latching modifiers: tapped once to hold, again to release. */
    private static final class Mod {
        final int keycode;
        boolean held;
        TextView view;
        Mod(int k) { keycode = k; }
    }

    private final List<Mod> mods = new ArrayList<>();
    private LinearLayout panel;
    private TextView tab;
    private boolean expanded = false;

    /**
     * D-32. Whether the engine is in gameplay. Starts false so the bar is
     * hidden through the intro movie and main menu rather than flashing up
     * before the first signal arrives.
     */
    private boolean inGame = false;

    public CommandBarView(Context ctx) {
        super(ctx);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        panel = buildPanel(ctx);
        panel.setVisibility(GONE);

        tab = buildTab(ctx);

        addView(tab);
        addView(panel);

        setVisibility(GONE); // hidden until the engine says it is in gameplay
    }

    private int dp(float v) {
        return (int) (getResources().getDisplayMetrics().density * v);
    }

    /** Rounded, translucent, vertically graded, hairline-stroked. */
    private GradientDrawable glass(int top, int bottom, int stroke, float radiusDp, boolean leftFlat) {
        final GradientDrawable g = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, new int[] { top, bottom });
        g.setStroke(dp(1), stroke);

        final float r = dp(radiusDp);
        if (leftFlat) {
            // Flat against the screen edge, rounded on the exposed side only -
            // a panel with rounded corners floating against the bezel looks
            // like a mistake rather than a design.
            g.setCornerRadii(new float[] { 0, 0, r, r, r, r, 0, 0 });
        } else {
            g.setCornerRadius(r);
        }
        return g;
    }

    /**
     * The always-visible handle. Spans the full height with the panel so the
     * two read as one object.
     */
    private TextView buildTab(Context ctx) {
        final TextView t = new TextView(ctx);
        t.setText("▸");
        t.setTextColor(COLOR_TEXT);
        t.setTextSize(18);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setBackground(glass(GLASS_TOP, GLASS_BOTTOM, GLASS_STROKE, 10, true));

        final LayoutParams lp = new LayoutParams(dp(28), LayoutParams.MATCH_PARENT);
        t.setLayoutParams(lp);

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
        col.setPadding(dp(6), dp(6), dp(6), dp(6));
        col.setLayoutParams(new LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        // Modifiers first: they change what everything below them does.
        col.addView(row(ctx, new TextView[] {
            modKey(ctx, "SHF", KeyEvent.KEYCODE_SHIFT_LEFT),
            modKey(ctx, "CTL", KeyEvent.KEYCODE_CTRL_LEFT),
            modKey(ctx, "ALT", KeyEvent.KEYCODE_ALT_LEFT)
        }));

        // Team groups. With CTL or ALT latched these CREATE a team; plain they
        // recall it. That is the engine's behaviour, not this view's.
        col.addView(row(ctx, new TextView[] {
            cmdKey(ctx, "1", KeyEvent.KEYCODE_1), cmdKey(ctx, "2", KeyEvent.KEYCODE_2),
            cmdKey(ctx, "3", KeyEvent.KEYCODE_3), cmdKey(ctx, "4", KeyEvent.KEYCODE_4),
            cmdKey(ctx, "5", KeyEvent.KEYCODE_5)
        }));

        col.addView(row(ctx, new TextView[] {
            cmdKey(ctx, "6", KeyEvent.KEYCODE_6), cmdKey(ctx, "7", KeyEvent.KEYCODE_7),
            cmdKey(ctx, "8", KeyEvent.KEYCODE_8), cmdKey(ctx, "9", KeyEvent.KEYCODE_9),
            cmdKey(ctx, "0", KeyEvent.KEYCODE_0)
        }));

        // Unit orders.
        col.addView(row(ctx, new TextView[] {
            cmdKey(ctx, "STP", KeyEvent.KEYCODE_S), cmdKey(ctx, "GRD", KeyEvent.KEYCODE_G),
            cmdKey(ctx, "SCT", KeyEvent.KEYCODE_X), cmdKey(ctx, "FRM", KeyEvent.KEYCODE_F),
            cmdKey(ctx, "QUE", KeyEvent.KEYCODE_Q)
        }));

        // Navigation and view.
        col.addView(row(ctx, new TextView[] {
            cmdKey(ctx, "NXT", KeyEvent.KEYCODE_N), cmdKey(ctx, "PRV", KeyEvent.KEYCODE_B),
            cmdKey(ctx, "BSE", KeyEvent.KEYCODE_H), cmdKey(ctx, "VEW", KeyEvent.KEYCODE_E),
            cmdKey(ctx, "MAP", KeyEvent.KEYCODE_U)
        }));

        return col;
    }

    /** Rows carry equal weight so the panel fills its full height evenly. */
    private LinearLayout row(Context ctx, TextView[] keys) {
        final LinearLayout r = new LinearLayout(ctx);
        r.setOrientation(HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);

        final LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, 0, 1f);
        r.setLayoutParams(lp);

        for (TextView k : keys) {
            r.addView(k);
        }
        return r;
    }

    private TextView baseKey(Context ctx, String label) {
        final TextView t = new TextView(ctx);
        t.setText(label);
        t.setTextColor(COLOR_TEXT);
        t.setTextSize(11);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setBackground(glass(KEY_TOP, KEY_BOTTOM, KEY_STROKE, 8, false));

        // 46dp: 10% wider than the 42dp first cut, per Michael.
        final LayoutParams lp = new LayoutParams(dp(46), LayoutParams.MATCH_PARENT);
        lp.setMargins(dp(3), dp(3), dp(3), dp(3));
        t.setLayoutParams(lp);
        return t;
    }

    /** A one-shot command: pressed and released immediately, like a real tap. */
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
     * A latching modifier: held until tapped again.
     *
     * Deliberately no auto-release after one action. Shift-selecting six units
     * is six taps, and a latch that dropped after the first would be worse than
     * not having one.
     */
    private TextView modKey(Context ctx, String label, final int keycode) {
        final Mod m = new Mod(keycode);
        final TextView t = baseKey(ctx, label);
        m.view = t;
        mods.add(m);

        t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m.held = !m.held;

                if (m.held) {
                    SDLActivity.onNativeKeyDown(m.keycode);
                    t.setBackground(glass(KEY_ON_TOP, KEY_ON_BOTTOM, KEY_ON_BOTTOM, 8, false));
                    t.setTextColor(COLOR_TEXT_ON);
                } else {
                    SDLActivity.onNativeKeyUp(m.keycode);
                    t.setBackground(glass(KEY_TOP, KEY_BOTTOM, KEY_STROKE, 8, false));
                    t.setTextColor(COLOR_TEXT);
                }
            }
        });

        return t;
    }

    /**
     * D-32. Show only during gameplay.
     *
     * Leaving gameplay also drops any latched modifier. A Shift held into a
     * menu would still be held on return, and the player would have no way to
     * work out why their clicks had stopped behaving.
     */
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
    }

    /**
     * Attach to SDL's own layout, spanning the full height of the left edge.
     * Left rather than right because Tiberian Dawn's sidebar owns the right.
     */
    public static CommandBarView attach(SDLActivity activity, ViewGroup sdlLayout) {
        final CommandBarView bar = new CommandBarView(activity);

        final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        sdlLayout.addView(bar, lp);
        return bar;
    }
}
