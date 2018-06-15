package mingxin.wang.common.excel;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.monitorjbl.xlsx.StreamingReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class ExcelUtil {
    private static final String MERGE_PREFIX = "MERGED_TO_";
    private static final String[][] EMPTY_SHEET_DATA = new String[0][0];

    private static final int MERGE_NULL = 0;
    private static final int MERGE_UP = 1;
    private static final int MERGE_LEFT = 2;
    private static final int MERGE_UP_LEFT = 3;

    private static final String MERGE_UP_TOKEN = MERGE_PREFIX + "UP";
    private static final String MERGE_LEFT_TOKEN = MERGE_PREFIX + "LEFT";
    private static final String MERGE_UP_LEFT_TOKEN = MERGE_PREFIX + "UP_LEFT";

    private static final ImmutableMap<String, Integer> MERGE_TOKEN_MAP = ImmutableMap.<String, Integer>builder()
            .put(MERGE_UP_TOKEN, MERGE_UP)
            .put(MERGE_LEFT_TOKEN, MERGE_LEFT)
            .put(MERGE_UP_LEFT_TOKEN, MERGE_UP_LEFT)
            .build();

    private static final ImmutableMap<ExcelFormat, WorkbookCreator> WORKBOOK_CREATOR_MAP = ImmutableMap.<ExcelFormat, WorkbookCreator>builder()
            .put(ExcelFormat.XLS, new WorkbookCreator() {
                @Override
                public Workbook createOutput() {
                    return new HSSFWorkbook();
                }

                @Override
                public Workbook createInput(InputStream inputStream) throws IOException, InvalidFormatException {
                    return WorkbookFactory.create(inputStream);
                }
            })
            .put(ExcelFormat.XLSX, new WorkbookCreator() {
                @Override
                public Workbook createOutput() {
                    return new SXSSFWorkbook();
                }

                @Override
                public Workbook createInput(InputStream inputStream) {
                    return StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(inputStream);
                }
            })
            .build();

    private interface WorkbookCreator {
        Workbook createOutput();

        Workbook createInput(InputStream inputStream) throws IOException, InvalidFormatException;
    }

    public static void writeSheet(OutputStream outputStream, ExcelFormat format, SheetData sheetData) throws IllegalExcelDataException, IOException {
        write(outputStream, format, Collections.singletonList(sheetData));
    }

    public static SheetData readActiveSheet(InputStream inputStream) throws IOException, InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(inputStream);
        return readByIndex(workbook, Collections.singletonList(workbook.getActiveSheetIndex())).iterator().next();
    }

    public static Optional<SheetData> readSheet(InputStream inputStream, ExcelFormat format, int index) throws IOException, InvalidFormatException {
        Workbook workbook = WORKBOOK_CREATOR_MAP.get(format).createInput(inputStream);
        List<SheetData> sheetData = readByIndex(workbook, Collections.singletonList(index));
        return sheetData.isEmpty() ? Optional.empty() : Optional.of(sheetData.get(0));
    }

    public static Optional<SheetData> readSheet(InputStream inputStream, ExcelFormat format, String name) throws IOException, InvalidFormatException {
        Workbook workbook = WORKBOOK_CREATOR_MAP.get(format).createInput(inputStream);
        List<SheetData> sheetData = readByName(workbook, Collections.singletonList(name));
        return sheetData.isEmpty() ? Optional.empty() : Optional.of(sheetData.get(0));
    }

    public static List<SheetData> readAll(InputStream inputStream, ExcelFormat format) throws IOException, InvalidFormatException {
        return readAll(WORKBOOK_CREATOR_MAP.get(format).createInput(inputStream));
    }

    public static List<SheetData> readAll(InputStream inputStream, ExcelFormat format, Predicate<? super Sheet> predicate) throws IOException, InvalidFormatException {
        return readAll(WORKBOOK_CREATOR_MAP.get(format).createInput(inputStream), predicate);
    }

    public static List<SheetData> readByIndex(InputStream inputStream, ExcelFormat format, Iterable<? extends Integer> indexes) throws IOException, InvalidFormatException {
        return readByIndex(WORKBOOK_CREATOR_MAP.get(format).createInput(inputStream), indexes);
    }

    public static List<SheetData> readByName(InputStream inputStream, ExcelFormat format, Iterable<? extends String> names) throws IOException, InvalidFormatException {
        return readByName(WORKBOOK_CREATOR_MAP.get(format).createInput(inputStream), names);
    }

    public static void write(OutputStream outputStream, ExcelFormat format, Iterable<? extends SheetData> workbookData) throws IllegalExcelDataException, IOException {
        Workbook result = WORKBOOK_CREATOR_MAP.get(format).createOutput();
        for (SheetData sheetData : workbookData) {
            Sheet sheet = result.createSheet(sheetData.getName());
            String[][] data = sheetData.getData();
            for (CellRangeAddress mergence : getMerged(data)) {
                sheet.addMergedRegion(mergence);
            }
            for (int i = 0; i < data.length; ++i) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < data[i].length; ++j) {
                    if (getMergeOption(data[i][j]) == MERGE_NULL) {
                        row.createCell(j).setCellValue(data[i][j]);
                    }
                }
            }
        }
        result.write(outputStream);
    }

    public static void mergeCells(String[][] data, int n0, int m0, int n1, int m1) {
        for (int i = n0 + 1; i < n1; ++i) {
            data[i][m0] = MERGE_UP_TOKEN;
        }
        for (int i = m0 + 1; i < m1; ++i) {
            data[n0][i] = MERGE_LEFT_TOKEN;
        }
        for (int i = n0 + 1; i < n1; ++i) {
            for (int j = m0 + 1; j < m1; ++j) {
                data[i][j] = MERGE_UP_LEFT_TOKEN;
            }
        }
    }

    public static String[][] toSheetData(Iterable<? extends Iterable<? extends String>> data) {
        Preconditions.checkNotNull(data);
        int n = 0, m = -1;
        for (Iterable<? extends String> row : data) {
            ++n;
            Preconditions.checkNotNull(row);
            int columnIndex = 0;
            for (String ignored : row) {
                ++columnIndex;
            }
            if (m == -1) {
                m = columnIndex;
            } else {
                Preconditions.checkState(columnIndex == m);
            }
        }
        if (m == -1) {
            return EMPTY_SHEET_DATA;
        }
        String[][] result = new String[n][m];
        int rowIndex = 0;
        for (Iterable<? extends String> row : data) {
            int columnIndex = 0;
            for (String cell : row) {
                result[rowIndex][columnIndex++] = cell;
            }
            ++rowIndex;
        }
        return result;
    }

    public static ArrayList<ArrayList<String>> toArrayList(String[][] sheetData) {
        Preconditions.checkNotNull(sheetData);
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        for (String[] row : sheetData) {
            Preconditions.checkState(sheetData[0].length == row.length);
            ArrayList<String> rowData = new ArrayList<>();
            rowData.addAll(Arrays.asList(row));
            result.add(rowData);
        }
        return result;
    }

    private ExcelUtil() {
    }

    private static List<SheetData> readByIndex(Workbook workbook, Iterable<? extends Integer> indexes) {
        List<SheetData> result = new ArrayList<>();
        for (int index : indexes) {
            Sheet sheet = workbook.getSheetAt(index);
            if (sheet != null) {
                result.add(readSheet(sheet));
            }
        }
        return result;
    }

    private static List<SheetData> readByName(Workbook workbook, Iterable<? extends String> names) {
        List<SheetData> result = new ArrayList<>();
        for (String name : names) {
            Sheet sheet = workbook.getSheet(name);
            if (sheet != null) {
                result.add(readSheet(sheet));
            }
        }
        return result;
    }

    private static List<SheetData> readAll(Workbook workbook) {
        List<SheetData> result = new ArrayList<>();
        for (Sheet sheet : workbook) {
            result.add(readSheet(sheet));
        }
        return result;
    }

    private static List<SheetData> readAll(Workbook workbook, Predicate<? super Sheet> predicate) {
        List<SheetData> result = new ArrayList<>();
        for (Sheet sheet : workbook) {
            if (predicate.test(sheet)) {
                result.add(readSheet(sheet));
            }
        }
        return result;
    }

    private static SheetData readSheet(Sheet sheet) {
        @Data
        @AllArgsConstructor
        class CellInfo {
            private int row;
            private int column;
            private String data;
        }
        int n = 0, m = 0;
        List<CellInfo> cellInfos = new ArrayList<>();
        for (Row row : sheet) {
            int rowIndex = row.getRowNum();
            for (Cell cell : row) {
                int columnIndex = cell.getColumnIndex();
                String data = cell.toString();
                if (!data.isEmpty()) {
                    m = Math.max(m, columnIndex);
                    n = Math.max(n, rowIndex);
                    cellInfos.add(new CellInfo(rowIndex, columnIndex, data));
                }
            }
        }
        ++n;
        ++m;
        String[][] result = new String[n][m];
        for (CellInfo cellInfo : cellInfos) {
            result[cellInfo.getRow()][cellInfo.getColumn()] = cellInfo.getData();
        }
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                if (result[i][j] == null) {
                    result[i][j] = "";
                }
            }
        }
        return new SheetData(sheet.getSheetName(), result);
    }

    private static void checkMergeToken(String[][] data, int x, int y, int expectMergeOption) throws IllegalExcelDataException {
        if (!isValidCell(data, x, y) || getMergeOption(data[x][y]) != expectMergeOption) {
            throw new IllegalExcelDataException("Invalid merge token at cell (" + x + "," + y + ")"
                    + System.lineSeparator()
                    + "Expected token option: " + expectMergeOption);
        }
    }

    private static boolean isValidCell(String[][] data, int n, int m) {
        return n >= 0 && m >= 0 && n < data.length && m < data[n].length;
    }

    private static CellRangeAddress getMerged(String[][] data, int n, int m, int mergeOption) throws IllegalExcelDataException {
        int x = n, y = m;
        if (mergeOption == MERGE_LEFT) {
            do {
                --y;
            } while (y >= 0 && getMergeOption(data[n][y]) == MERGE_LEFT);
            checkMergeToken(data, n, y, MERGE_NULL);
        } else if (mergeOption == MERGE_UP) {
            do {
                --x;
            } while (x >= 0 && getMergeOption(data[x][m]) == MERGE_UP);
            checkMergeToken(data, x, m, MERGE_NULL);
        } else {
            do {
                --x;
            } while (x >= 0 && getMergeOption(data[x][m]) == MERGE_UP_LEFT);
            checkMergeToken(data, x, m, MERGE_LEFT);
            do {
                --y;
            } while (y >= 0 && getMergeOption(data[n][y]) == MERGE_UP_LEFT);
            checkMergeToken(data, n, y, MERGE_UP);
            for (int i = x + 1; i < n; ++i) {
                for (int j = y + 1; j < m; ++j) {
                    checkMergeToken(data, i, j, MERGE_UP_LEFT);
                }
            }
            for (int i = x + 1; i < n; ++i) {
                checkMergeToken(data, i, y, MERGE_UP);
            }
            for (int i = y + 1; i < m; ++i) {
                checkMergeToken(data, x, i, MERGE_LEFT);
            }
            checkMergeToken(data, x, y, MERGE_NULL);
        }
        return new CellRangeAddress(x, n, y, m);
    }

    private static Iterable<CellRangeAddress> getMerged(String[][] data) throws IllegalExcelDataException {
        List<CellRangeAddress> result = new ArrayList<>();
        for (int i = 0; i < data.length; ++i) {
            for (int j = 0; j < data[i].length; ++j) {
                int mergeOption = getMergeOption(data[i][j]);
                if (mergeOption != MERGE_NULL &&
                        (!isValidCell(data, i + 1, j) || (getMergeOption(data[i + 1][j]) & MERGE_UP) == 0) &&
                        (!isValidCell(data, i, j + 1) || (getMergeOption(data[i][j + 1]) & MERGE_LEFT) == 0)) {
                    result.add(getMerged(data, i, j, mergeOption));
                }
            }
        }
        return result;
    }

    private static int getMergeOption(String data) throws IllegalExcelDataException {
        if (!data.startsWith(MERGE_PREFIX)) {
            return MERGE_NULL;
        }
        Integer result = MERGE_TOKEN_MAP.get(data);
        if (result == null) {
            throw new IllegalExcelDataException("Unknown merge token: " + data);
        }
        return result;
    }
}
