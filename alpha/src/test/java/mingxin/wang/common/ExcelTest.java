package mingxin.wang.common;

import mingxin.wang.common.excel.ExcelFormat;
import mingxin.wang.common.excel.ExcelUtils;
import mingxin.wang.common.excel.IllegalExcelDataException;
import mingxin.wang.common.excel.SheetData;
import mingxin.wang.common.util.ResourceMonitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class ExcelTest {
    private static SheetData generateTest(String name, int n, int m) {
        String[][] data = new String[n][m];
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                data[i][j] = "(" + i + "," + j + ")";
            }
        }
        return new SheetData(name, data);
    }

    private static FileOutputStream getOutputStream(String name) {
        try {
            return new FileOutputStream(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static FileInputStream getInputStream(String name) {
        try {
            return new FileInputStream(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void testWrite() {
        SheetData data = generateTest("test", 10000, 100);
        try (FileOutputStream fileOutputStream = getOutputStream("test.xlsx")) {
            ResourceMonitor monitor = new ResourceMonitor("Excel Write");
            ExcelUtils.writeSheet(fileOutputStream, ExcelFormat.XLSX, data);
            monitor.record();
            monitor.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testRead() {
        try (FileInputStream fileInputStream = getInputStream("test.xlsx")) {
            ResourceMonitor monitor = new ResourceMonitor("Excel Read");
            Optional<SheetData> sheetData = ExcelUtils.readSheet(fileInputStream, ExcelFormat.XLSX, "test");
            monitor.record();
            monitor.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, IllegalExcelDataException {
        testRead();
        //testWrite();
    }
}
