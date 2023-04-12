package com.zzj.main;

import com.zzj.util.StringUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;

public class ExcelToTxt {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            return;
        }
        File file = new File(args[0]);
        Workbook workbook;
        if (file.getName().endsWith(".xls")) {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = new HSSFWorkbook(fis);
            }
        } else if (file.getName().endsWith(".xlsx")) {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = new XSSFWorkbook(fis);
            }
        } else {
            return;
        }
        for (Sheet sheet : workbook) {
            System.out.println(StringUtil.format("===================={}:{}=====================", file.getName(), sheet.getSheetName()));
            for (Row row : sheet) {
                for (Cell cell : row) {
                    System.out.print(cell.getStringCellValue() + "\t");
                }
                System.out.println();
            }
        }
    }
}

