package io.keam.utils;

import io.quarkus.panache.common.Sort;

/**
 * Helper for common model behavior.
 */
public class ModelUtils {
    public static Sort createSort(String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.Ascending : Sort.Direction.Descending;
        return Sort.by("id", direction);
    }
}
