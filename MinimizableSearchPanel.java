package com.client;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class MinimizableSearchPanel extends JPanel {

    private final JTable table;
    private final int columnIndex;
    private JTextField searchField;
    private JButton toggleButton;
    private boolean isExpanded = false;

    public MinimizableSearchPanel(JTable table, int columnIndex) {
        this.table = table;
        this.columnIndex = columnIndex;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        
        searchField = new JTextField(20);
        searchField.setVisible(false); // Initially hidden (minimized)

        // Listen for changes in the text field to filter automatically
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateFilter(); }
        });

        toggleButton = new JButton("Search");
        toggleButton.addActionListener(e -> toggleSearch());

        add(toggleButton);
        add(searchField);
    }

    private void toggleSearch() {
        isExpanded = !isExpanded;
        searchField.setVisible(isExpanded);

        if (isExpanded) {
            toggleButton.setText("Close");
            searchField.requestFocusInWindow();
        } else {
            toggleButton.setText("Search");
            searchField.setText(""); // Clear text
            updateFilter(); // Clear filter
        }
        
        revalidate();
        repaint();
    }

    private void updateFilter() {
        TableFilterUtils.filterByColumn(table, columnIndex, searchField.getText());
    }
}