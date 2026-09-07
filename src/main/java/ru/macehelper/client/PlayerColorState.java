package ru.macehelper.client;

final class PlayerColorState {
    private int settledColor;
    private int originColor;
    private int targetColor;
    private long transitionStartNanos;
    private long transitionEndNanos;
    private boolean transitioning;

    PlayerColorState(int initialColor) {
        this.settledColor = opaque(initialColor);
    }

    void onHit(int firstColor, int secondColor, boolean localPlayer, long nowNanos, long durationNanos) {
        getColor(nowNanos);

        if (transitioning) {
            if (localPlayer) {
                return;
            }

            settledColor = originColor;
            startTransition(originColor, targetColor, nowNanos, durationNanos);
            return;
        }

        int currentColor = settledColor;
        int nextColor = currentColor == opaque(firstColor) ? opaque(secondColor) : opaque(firstColor);
        startTransition(currentColor, nextColor, nowNanos, durationNanos);
    }

    boolean isTransitioning(long nowNanos) {
        getColor(nowNanos);
        return transitioning;
    }

    int getColor(long nowNanos) {
        if (!transitioning) {
            return settledColor;
        }

        if (nowNanos >= transitionEndNanos) {
            transitioning = false;
            settledColor = targetColor;
            return settledColor;
        }

        double progress = (double) (nowNanos - transitionStartNanos)
                / (double) (transitionEndNanos - transitionStartNanos);
        return lerpColor(originColor, targetColor, Math.max(0.0, Math.min(1.0, progress)));
    }

    private void startTransition(int fromColor, int toColor, long nowNanos, long durationNanos) {
        originColor = opaque(fromColor);
        targetColor = opaque(toColor);
        transitionStartNanos = nowNanos;
        transitionEndNanos = nowNanos + Math.max(1L, durationNanos);
        transitioning = true;
    }

    private static int lerpColor(int fromColor, int toColor, double progress) {
        int red = lerpChannel(fromColor >> 16, toColor >> 16, progress);
        int green = lerpChannel(fromColor >> 8, toColor >> 8, progress);
        int blue = lerpChannel(fromColor, toColor, progress);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static int lerpChannel(int from, int to, double progress) {
        return (int) Math.round(((from & 0xFF) * (1.0 - progress)) + ((to & 0xFF) * progress));
    }

    private static int opaque(int color) {
        return 0xFF000000 | (color & 0xFFFFFF);
    }
}
