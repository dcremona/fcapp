package fcapp.utils;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

/**
 * In-memory record buffer that also acts as a TableModel and ComboBoxModel.
 * <p>
 * Records are stored as rows of String values. Field types (STRING, NUMERIC, DATE)
 * are inferred on insertion and used for sorting.
 *
 * @author dcremona (refactored)
 */
public class Buffer extends AbstractTableModel implements ComboBoxModel<Object> {

    @Serial
    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    // Field type
    // -------------------------------------------------------------------------
    public enum FieldType {
        STRING, NUMERIC, DATE, UNKNOWN
    }

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------
    private String[] columnNames;
    private final List<String[]> records = new ArrayList<>();
    private FieldType[] fieldTypes = new FieldType[0];

    private int currentRow = 0;               // 0-based
    private int sortedColumn = 0;             // 1-based, 0 = unsorted
    private boolean alwaysSort = false;
    private int defaultCapacityIncrement = 200;

    // Find state
    private String findText = "";
    private int findColumn = 0;               // 1-based
    private boolean findCaseSensitive = false;

    // Metadata / error state (kept for compatibility)
    private boolean error = false;
    private int errorCode = 0;
    private String tag;
    private String dbTime;
    private String queryName;
    private String exit;
    public boolean eof = true;

    private boolean dataTypeKnown = false;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    public Buffer() {
        // empty
    }

    public Buffer(String[] columnNames) {
        this.columnNames = columnNames != null ? columnNames.clone() : null;
        reset();
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------
    public void clear() {
        int oldCount = records.size();
        if (oldCount > 0) {
            fireIntervalRemoved(0, oldCount - 1);
            fireTableRowsDeleted(0, oldCount - 1);
        }
        records.clear();
        fieldTypes = new FieldType[0];
        currentRow = 0;
        findText = "";
        findColumn = 0;
        findCaseSensitive = false;
        sortedColumn = 0;
        alwaysSort = false;
        eof = true;
        tag = null;
        dbTime = null;
        error = false;
        errorCode = 0;
        dataTypeKnown = false;
    }

    private void reset() {
        records.clear();
        fieldTypes = new FieldType[0];
        currentRow = 0;
        error = false;
        findText = "";
        findColumn = 0;
        findCaseSensitive = false;
        tag = null;
        dbTime = null;
        queryName = null;
        exit = null;
        dataTypeKnown = false;
        sortedColumn = 0;
        alwaysSort = false;
        eof = true;
    }

    // -------------------------------------------------------------------------
    // Capacity / field count
    // -------------------------------------------------------------------------
    public void setDefaultAdd(int increment) {
        this.defaultCapacityIncrement = Math.max(1, increment);
    }

    public int getFieldCount() {
        return fieldTypes.length;
    }

    void setFieldCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Field count cannot be negative");
        }
        fieldTypes = new FieldType[count];
        Arrays.fill(fieldTypes, FieldType.UNKNOWN);
    }

    // -------------------------------------------------------------------------
    // Sorting
    // -------------------------------------------------------------------------
    public void setSortedField(int column) {          // 1-based
        this.sortedColumn = column;
    }

    public int getSortedField() {
        return sortedColumn;
    }

    public void setSortAlways(boolean always) {
        this.alwaysSort = always;
    }

    public void sort(int column) {                    // 1-based
        if (records.size() < 2 || column < 1 || column > fieldTypes.length
                || (column == sortedColumn && !alwaysSort)) {
            return;
        }

        FieldType type = getFieldType(column);
        Comparator<String[]> comparator = switch (type) {
            case NUMERIC -> Comparator.comparingLong(row -> parseLongSafe(row[column - 1]));
            case DATE -> Comparator.comparing(row -> normalizeDate(row[column - 1]));
            default -> Comparator.comparing(row -> row[column - 1], String.CASE_INSENSITIVE_ORDER);
        };

        records.sort(comparator);
        sortedColumn = column;
        fireTableRowsUpdated(0, records.size() - 1);
    }

    public void sort(int[] columns) {                 // 1-based, most significant first
        if (records.size() < 2 || columns == null || columns.length == 0) {
            return;
        }
        for (int col : columns) {
            if (col < 1 || col > fieldTypes.length) {
                return;
            }
        }

        // Build a composite key comparator
        Comparator<String[]> comparator = (a, b) -> {
            for (int col : columns) {
                FieldType type = getFieldType(col);
                int cmp = switch (type) {
                    case NUMERIC -> Long.compare(
                            parseLongSafe(a[col - 1]), parseLongSafe(b[col - 1]));
                    case DATE -> normalizeDate(a[col - 1]).compareTo(normalizeDate(b[col - 1]));
                    default -> a[col - 1].compareToIgnoreCase(b[col - 1]);
                };
                if (cmp != 0) {
                    return cmp;
                }
            }
            return 0;
        };

        records.sort(comparator);
        sortedColumn = 0;
        fireTableRowsUpdated(0, records.size() - 1);
    }

    public void revert() {
        Collections.reverse(records);
        sortedColumn = 0;
        fireTableRowsUpdated(0, records.size() - 1);
        fireContentsChanged(0, records.size() - 1);
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------
    public int getRecordCount() {
        return records.size();
    }

    public void moveFirst() {
        if (!records.isEmpty()) {
            currentRow = 0;
            eof = false;
        }
    }

    public void moveNext() {
        if (currentRow + 1 < records.size()) {
            currentRow++;
        } else {
            eof = true;
        }
    }

    public void setCurrentIndex(int row) {            // 1-based
        if (row >= 1 && row <= records.size()) {
            currentRow = row - 1;
        }
    }

    public int getCurrentIndex() {                    // 1-based
        return currentRow + 1;
    }

    // -------------------------------------------------------------------------
    // Find
    // -------------------------------------------------------------------------
    public int findFirst(String text, int column, boolean caseSensitive) {
        if (records.isEmpty() || column < 1 || column > fieldTypes.length) {
            return -1;
        }
        String target = text.trim();
        for (int i = 0; i < records.size(); i++) {
            String value = records.get(i)[column - 1];
            boolean match = caseSensitive
                    ? value.equals(target)
                    : value.equalsIgnoreCase(target);
            if (match) {
                findText = text;
                findColumn = column;
                findCaseSensitive = caseSensitive;
                currentRow = i;
                eof = false;
                return i + 1;                         // 1-based
            }
        }
        findText = "";
        findColumn = 0;
        return -1;
    }

    public int findNext() {
        if (currentRow >= records.size() - 1 || records.size() < 2 || findColumn == 0) {
            return -1;
        }
        String target = findText.trim();
        for (int i = currentRow + 1; i < records.size(); i++) {
            String value = records.get(i)[findColumn - 1];
            boolean match = findCaseSensitive
                    ? value.equals(target)
                    : value.equalsIgnoreCase(target);
            if (match) {
                currentRow = i;
                return i + 1;
            }
        }
        return -1;
    }

    // -------------------------------------------------------------------------
    // Field access
    // -------------------------------------------------------------------------
    public String getField(int fieldNumber) {         // 1-based
        if (fieldNumber < 1 || fieldNumber > fieldTypes.length) {
            return "END OF BUFFER";
        }
        return records.get(currentRow)[fieldNumber - 1];
    }

    public int getFieldByInt(int fieldNumber) {
        if (fieldNumber < 1 || fieldNumber > fieldTypes.length) {
            return 0;
        }
        try {
            return Integer.parseInt(records.get(currentRow)[fieldNumber - 1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setField(int recordNumber, int fieldNumber, String value) {
        if (recordNumber < 1 || recordNumber > records.size()
                || fieldNumber < 1 || fieldNumber > fieldTypes.length) {
            return;
        }
        records.get(recordNumber - 1)[fieldNumber - 1] = value;
        updateFieldType(fieldNumber, inferFieldType(value));

        if (sortedColumn == fieldNumber) {
            sortedColumn = 0;
        }
        fireTableCellUpdated(recordNumber - 1, fieldNumber - 1);
    }

    public FieldType getFieldType(int field) {        // 1-based
        if (field < 1 || field > fieldTypes.length) {
            return FieldType.UNKNOWN;
        }
        return fieldTypes[field - 1];
    }

    // -------------------------------------------------------------------------
    // Record mutation
    // -------------------------------------------------------------------------
    public void delete(int item) {                    // 1-based
        if (item < 1 || item > records.size()) {
            return;
        }
        records.remove(item - 1);
        if (records.isEmpty()) {
            reset();
        }
        fireTableRowsDeleted(item - 1, item - 1);
        fireIntervalRemoved(item - 1, item - 1);
    }

    public void replace(int row, String item) {       // 1-based
        if (row < 1 || row > records.size()) {
            return;
        }
        setRecordValues(item, row - 1);
        fireTableDataChanged();
        fireContentsChanged(row - 1, row - 1);
    }

    public void addNew(String item) {
        if (records.isEmpty() && fieldTypes.length == 0) {
            createRecordSet(item);
        }
        String[] row = new String[fieldTypes.length];
        records.add(row);

        if (!dataTypeKnown) {
            setRecordValues(item, records.size() - 1);
        } else {
            setRecordValuesNoCheckFieldType(item, records.size() - 1);
        }

        sortedColumn = 0;
        eof = false;
        int newIndex = records.size() - 1;
        fireTableRowsInserted(newIndex, newIndex);
        fireIntervalAdded(newIndex, newIndex);
    }

    public String getItem(int item) {                 // 1-based
        if (item < 1 || item > records.size()) {
            return "END OF BUFFER";
        }
        StringBuilder sb = new StringBuilder();
        String[] row = records.get(item - 1);
        for (int i = 0; i < fieldTypes.length; i++) {
            sb.append('@').append(i + 1).append(row[i]);
            if (i < fieldTypes.length - 1) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Cloning / bulk set
    // -------------------------------------------------------------------------
    public Buffer getClone() {
        Buffer clone = new Buffer();
        if (records.isEmpty()) {
            return clone;
        }
        clone.setDefaultAdd(defaultCapacityIncrement);
        clone.fieldTypes = fieldTypes.clone();
        clone.records.clear();
        for (String[] row : records) {
            clone.records.add(row.clone());
        }
        clone.sortedColumn = sortedColumn;
        clone.tag = tag;
        clone.dbTime = dbTime;
        clone.queryName = queryName;
        clone.exit = exit;
        return clone;
    }

    public void setRecordSet(String[][] data, String[] typeNames, int fieldCount, int recordCount) {
        this.fieldTypes = new FieldType[fieldCount];
        for (int i = 0; i < fieldCount; i++) {
            fieldTypes[i] = parseTypeName(typeNames[i]);
        }
        records.clear();
        for (int i = 0; i < recordCount; i++) {
            records.add(data[i].clone());
        }
        currentRow = 0;
    }

    // -------------------------------------------------------------------------
    // Internal parsing helpers
    // -------------------------------------------------------------------------
    private void createRecordSet(String firstItem) {
        int count = countRecordFields(firstItem);
        setFieldCount(count);
        records.clear();
    }

    private int countRecordFields(String rec) {
        int cont = 1;
        int pos = 0;
        while (true) {
            pos = rec.indexOf("@" + cont, pos);
            if (pos == -1) {
                break;
            }
            cont++;
        }
        return cont - 1;
    }

    void setRecordValues(String line, int recordIndex) {
        parseAndStore(line, recordIndex, true);
    }

    void setRecordValuesNoCheckFieldType(String line, int recordIndex) {
        parseAndStore(line, recordIndex, false);
    }

    private void parseAndStore(String line, int recordIndex, boolean checkTypes) {
        if (line.isEmpty()) {
            return;
        }
        if (line.charAt(0) == ' ') {
            line = line.substring(1);
        }

        StringTokenizer tokenizer = new StringTokenizer(line, "@");
        int tokenCount = tokenizer.countTokens();
        String[] row = records.get(recordIndex);

        int token = 1;
        for (int field = 1; field <= tokenCount; field++) {
            String raw = tokenizer.nextToken();
            int extra = token < 10 ? 1 : 2;
            String value = raw.substring(extra);
            token++;

            if (field > fieldTypes.length) {
                // Fallback for records that contain literal '@'
                try {
                    valoreBuffer(line, recordIndex);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    // original code showed a dialog; we just abort
                }
                return;
            }

            if (checkTypes) {
                FieldType inferred = inferFieldType(value);
                updateFieldType(field, inferred);
            }

            if (getFieldType(field) == FieldType.DATE && value.length() == 10) {
                row[field - 1] = value.substring(0, 10);
            } else {
                row[field - 1] = value.trim();
            }
        }
    }

    private void valoreBuffer(String row, int recordIndex) {
        String[] target = records.get(recordIndex);
        for (int field = 1; field <= fieldTypes.length; field++) {
            String marker = "@" + field;
            String nextMarker = "@" + (field + 1);

            int start = row.indexOf(marker);
            if (start == -1) {
                throw new ArrayIndexOutOfBoundsException();
            }
            // Prefer last occurrence if nested
            int last = row.lastIndexOf(marker);
            int end = row.indexOf(nextMarker, start);
            if (end == -1) {
                end = row.length();
            }
            if (last > start && last < end) {
                start = last;
            }

            String value = (end > start)
                    ? row.substring(start + marker.length(), end).trim()
                    : row.substring(start + marker.length()).trim();

            FieldType type = inferFieldType(value);
            updateFieldType(field, type);

            if (type == FieldType.DATE && value.length() == 10) {
                target[field - 1] = value.substring(0, 10);
            } else {
                target[field - 1] = value;
            }
        }
    }

    private static FieldType inferFieldType(String value) {
        value = value.trim();
        if (value.isEmpty()) {
            return FieldType.UNKNOWN;                 // keep previous
        }
        if (value.length() == 10
                && value.charAt(2) == '/'
                && value.charAt(5) == '/') {
            return FieldType.DATE;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return FieldType.STRING;
            }
        }
        return FieldType.NUMERIC;
    }

    private void updateFieldType(int field, FieldType candidate) {  // 1-based
        if (field < 1 || field > fieldTypes.length) {
            return;
        }
        FieldType current = fieldTypes[field - 1];
        if (current == FieldType.UNKNOWN || candidate != FieldType.UNKNOWN) {
            // Prefer STRING if mixed, otherwise take the candidate
            if (current == FieldType.STRING) {
                // keep STRING once it has been seen
            } else {
                fieldTypes[field - 1] = candidate;
            }
        }
    }

    private static FieldType parseTypeName(String name) {
        if (name == null) {
            return FieldType.UNKNOWN;
        }
        return switch (name.toUpperCase()) {
            case "STRING" -> FieldType.STRING;
            case "NUMERIC" -> FieldType.NUMERIC;
            case "DATE" -> FieldType.DATE;
            default -> FieldType.UNKNOWN;
        };
    }

    private static long parseLongSafe(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /** Convert dd/mm/yyyy… to yyyy/mm/dd… for lexicographic sort. */
    private static String normalizeDate(String date) {
        if (date == null || date.length() < 10) {
            return date == null ? "" : date;
        }
        try {
            return date.substring(6, 10) + "/"
                    + date.substring(3, 5) + "/"
                    + date.substring(0, 2)
                    + (date.length() > 10 ? date.substring(10) : "");
        } catch (StringIndexOutOfBoundsException e) {
            return date;
        }
    }

    // -------------------------------------------------------------------------
    // Metadata / error flags (kept for API compatibility)
    // -------------------------------------------------------------------------
    public void setDataTypeKnow(boolean known) {
        this.dataTypeKnown = known;
    }

    public boolean isERROR() {
        return error;
    }

    public void setERROR(boolean error) {
        this.error = error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int code) {
        this.errorCode = code;
    }

    // -------------------------------------------------------------------------
    // AbstractTableModel
    // -------------------------------------------------------------------------
    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        return fieldTypes.length;
    }

    @Override
    public String getColumnName(int column) {
        if (columnNames != null && column < columnNames.length) {
            return columnNames[column];
        }
        return super.getColumnName(column);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return records.get(rowIndex)[columnIndex];
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        setField(row + 1, column + 1, aValue.toString().trim());
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    // -------------------------------------------------------------------------
    // ComboBoxModel / ListModel
    // -------------------------------------------------------------------------
    private Object selectedItem;

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void setSelectedItem(Object anObject) {
        selectedItem = anObject;
        fireContentsChanged(-1, -1);
    }

    @Override
    public Object getElementAt(int index) {
        if (index >= 0 && index < records.size()) {
            return records.get(index);
        }
        return null;
    }

    @Override
    public int getSize() {
        return records.size();
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listenerList.add(ListDataListener.class, l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listenerList.remove(ListDataListener.class, l);
    }

    protected void fireIntervalAdded(int index0, int index1) {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).intervalAdded(e);
            }
        }
    }

    protected void fireIntervalRemoved(int index0, int index1) {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).intervalRemoved(e);
            }
        }
    }

    protected void fireContentsChanged(int index0, int index1) {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).contentsChanged(e);
            }
        }
    }

    public void refresh() {
        fireTableRowsUpdated(0, Math.max(0, records.size() - 1));
        fireContentsChanged(0, Math.max(0, records.size() - 1));
    }
}