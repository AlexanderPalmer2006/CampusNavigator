package za.ac.wits.campusnavigator.ui.common;

/**
 * A single-consumption wrapper for {@code LiveData} content that represents a one-shot
 * *event* rather than persistent, re-observable *state* (Story 4.2). The standard,
 * minimal Android Architecture Components pattern for exactly this mismatch: plain
 * {@code LiveData} always redelivers its last value to any newly-registered observer,
 * which is correct for state (e.g. {@code NavigationViewModel.getActiveRoute()} -- a
 * route already in progress *should* still show when the Map tab is revisited) but wrong
 * for an event (e.g. "here's what your Category Pick tap just resolved to" -- a later,
 * unrelated tab revisit must not silently re-fire a tab switch or re-show a stale
 * failure Snackbar). {@link #getContentIfNotHandled()} returns the wrapped content
 * exactly once; every call after the first (from any observer) returns {@code null}.
 */
public final class Event<T> {

    private final T content;
    private boolean handled = false;

    public Event(T content) {
        this.content = content;
    }

    /** Returns the content and marks it handled, or {@code null} if already handled. */
    public T getContentIfNotHandled() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }
}
