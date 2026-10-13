package com.blackholecodes.macroscript.ide;

import com.blackholecodes.macroscript.diagnostics.Diagnostic;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** The Problems tool window: the errors and warnings of the current file. Double-click to go to one. */
final class ProblemsPanel extends JPanel {

    private final ProblemsModel model = new ProblemsModel();
    private final JTable table = new JTable(model);

    ProblemsPanel(Consumer<Diagnostic> navigate) {
        super(new BorderLayout());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(2).setMaxWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setMaxWidth(260);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (e.getClickCount() == 2 && row >= 0) {
                    navigate.accept(model.rows.get(table.convertRowIndexToModel(row)));
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    void show(List<Diagnostic> diagnostics) {
        model.rows = new ArrayList<>(diagnostics);
        model.fireTableDataChanged();
    }

    /** A short summary for the tool window title, for example "2 errors, 1 warning". */
    static String summary(List<Diagnostic> diagnostics) {
        long errors = diagnostics.stream().filter(Diagnostic::isError).count();
        long warnings = diagnostics.size() - errors;
        return errors + (errors == 1 ? " error, " : " errors, ") + warnings + (warnings == 1 ? " warning" : " warnings");
    }

    private static final class ProblemsModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"Type", "Message", "Line", "File"};
        private List<Diagnostic> rows = List.of();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            Diagnostic diagnostic = rows.get(row);
            Path file = diagnostic.file();
            return switch (column) {
                case 0 -> diagnostic.isError() ? "Error" : "Warning";
                case 1 -> diagnostic.message();
                case 2 -> diagnostic.range().line();
                default -> file == null ? "" : file.getFileName().toString();
            };
        }
    }
}
