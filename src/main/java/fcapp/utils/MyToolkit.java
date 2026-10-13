package fcapp.utils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Utility class providing common helper methods.
 * <p>
 * This class cannot be instantiated.
 */
public final class MyToolkit {

    private MyToolkit() {
        // Utility class – prevent instantiation
    }

    /**
     * Sorts the given {@code keys} array in ascending order and rearranges
     * the parallel {@code values} array so that the association between
     * elements is preserved.
     *
     * @param keys   the array used as the sort key (must not be {@code null})
     * @param values parallel array that will be reordered together with {@code keys}
     *               (must have the same length as {@code keys})
     * @throws NullPointerException     if {@code keys} or {@code values} is {@code null}
     * @throws IllegalArgumentException if the two arrays have different lengths
     */
    public static void sortBuffer(String[] keys, Object[] values) {
        requireSameLength(keys, values);
        sortParallel(keys, values, Comparator.naturalOrder());
    }

    /**
     * Sorts the given {@code keys} array in ascending order and rearranges
     * the parallel {@code values} array so that the association between
     * elements is preserved.
     *
     * @param keys   the array used as the sort key (must not be {@code null})
     * @param values parallel array that will be reordered together with {@code keys}
     *               (must have the same length as {@code keys})
     * @throws NullPointerException     if {@code keys} or {@code values} is {@code null}
     * @throws IllegalArgumentException if the two arrays have different lengths
     */
    public static void sortBufferN(long[] keys, Object[] values) {
        requireSameLength(keys, values);
        // Wrapper needed because primitive arrays cannot be used directly with Comparator
        Long[] boxed = Arrays.stream(keys).boxed().toArray(Long[]::new);
        sortParallel(boxed, values, Comparator.naturalOrder());
        // Write the sorted order back into the original primitive array
        for (int i = 0; i < keys.length; i++) {
            keys[i] = boxed[i];
        }
    }

    /**
     * Pads or truncates a string to the requested length.
     * <ul>
     *   <li>If the string is longer than {@code length}, it is truncated.</li>
     *   <li>If shorter, it is padded with the given fill character.</li>
     * </ul>
     *
     * @param text        the original string (may be {@code null}, treated as empty)
     * @param length      desired final length (must be ≥ 0)
     * @param fillChar    character used for padding
     * @param padLeft     {@code true} → left-pad, {@code false} → right-pad
     * @return the formatted string of exactly {@code length} characters
     */
    public static String formatText(String text, int length, char fillChar, boolean padLeft) {
        if (length < 0) {
            throw new IllegalArgumentException("length must be ≥ 0");
        }
        String s = (text == null) ? "" : text;

        if (s.length() >= length) {
            return s.substring(0, length);
        }

        String padding = String.valueOf(fillChar).repeat(length - s.length());
        return padLeft ? padding + s : s + padding;
    }

    /**
     * Convenience overload that accepts the fill character as a {@code String}
     * (only the first character is used) and a positional flag matching the
     * original API ({@code 0} = left-pad, any other value = right-pad).
     */
    public static String formatText(String text, int length, String fill, int pos) {
        char fillChar = (fill == null || fill.isEmpty()) ? ' ' : fill.charAt(0);
        return formatText(text, length, fillChar, pos == 0);
    }

    /**
     * Checks whether the given day/month/year combination represents a valid date.
     * Years ≤ 1900 are considered invalid (historical restriction from the original code).
     *
     * @param day   day of month (1-31)
     * @param month month (1-12)
     * @param year  year (> 1900)
     * @return {@code true} if the date is valid, {@code false} otherwise
     */
    public static boolean isValidDate(int day, int month, int year) {
        if (year <= 1900) {
            return false;
        }
        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void requireSameLength(Object a, Object b) {
        if (a == null || b == null) {
            throw new NullPointerException("arrays must not be null");
        }
        int lenA = java.lang.reflect.Array.getLength(a);
        int lenB = java.lang.reflect.Array.getLength(b);
        if (lenA != lenB) {
            throw new IllegalArgumentException(
                    "arrays must have the same length: " + lenA + " vs " + lenB);
        }
    }

    /**
     * Sorts two parallel arrays using the provided comparator on the key array.
     * The values array is reordered so that the original key→value association
     * is preserved.
     */
    private static <K> void sortParallel(K[] keys, Object[] values, Comparator<? super K> cmp) {
        Integer[] indices = new Integer[keys.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        Arrays.sort(indices, Comparator.comparing(i -> keys[i], cmp));

        // Apply the permutation to both arrays (in-place, using a temporary copy of keys)
        K[] sortedKeys = keys.clone();
        Object[] sortedValues = values.clone();

        for (int i = 0; i < indices.length; i++) {
            keys[i] = sortedKeys[indices[i]];
            values[i] = sortedValues[indices[i]];
        }
    }
}