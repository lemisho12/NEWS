package com.client;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.regex.Pattern;

public class TableFilterUtils {

    /**
     * Filters the given JTable by a specific column value.
     * 
     * @param table       The JTable to filter.
     * @param columnIndex The index of the column to filter by.
     * @param filterText  The text to filter for. Pass null or "All" to clear.
     */
    public static void filterByColumn(JTable table, int columnIndex, String filterText) {
        TableModel model = table.getModel();
        TableRowSorter<TableModel> sorter;

        // Check if a sorter already exists, otherwise create one
        if (table.getRowSorter() instanceof TableRowSorter) {
            sorter = (TableRowSorter<TableModel>) table.getRowSorter();
        } else {
            sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);
        }

        if (filterText == null || filterText.trim().isEmpty() || "All".equalsIgnoreCase(filterText)) {
            sorter.setRowFilter(null);
        } else {
            // Create a case-insensitive regex filter for the specified column
            String regex = "(?i)" + Pattern.quote(filterText);
            sorter.setRowFilter(RowFilter.regexFilter(regex, columnIndex));
        }
    }
}