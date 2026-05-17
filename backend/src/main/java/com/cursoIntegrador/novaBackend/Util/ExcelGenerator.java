package com.cursoIntegrador.novaBackend.Util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class ExcelGenerator {

    public static <T> ByteArrayInputStream generateExcel(List<T> dataList, String sheetName) throws IOException {
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("La lista de datos está vacía");
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        Class<?> clazz = dataList.get(0).getClass();
        Field[] fields = clazz.getDeclaredFields();

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < fields.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(fields[i].getName());
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (T item : dataList) {
            Row row = sheet.createRow(rowIdx++);
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                Object value;
                try {
                    value = fields[i].get(item);
                } catch (IllegalAccessException e) {
                    value = "ERROR";
                }
                Cell cell = row.createCell(i);
                cell.setCellValue(value != null ? value.toString() : "");
            }
        }

        for (int i = 0; i < fields.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}
