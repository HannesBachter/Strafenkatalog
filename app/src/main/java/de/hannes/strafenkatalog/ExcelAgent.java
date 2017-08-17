package de.hannes.strafenkatalog;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by hannes on 03.07.14.
 */
public class ExcelAgent {

    private static String UMGEBUNG;

    public static void setUmgebung(String umgebung){
        UMGEBUNG = umgebung;
    }

    public static boolean saveExcelFile(Context context, String fileName) {

        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            return false;
        }

        boolean success = false;

        //New Workbook
        Workbook wb = new HSSFWorkbook();

        Cell c = null;

        //Cell style for header row
        CellStyle cs = wb.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.LIME.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        //New Sheet
        Sheet sheet1 = null;
        sheet1 = wb.createSheet("Strafenkatalog");
/*
        // Generate column headings
        Row row = sheet1.createRow(0);

        c = row.createCell(0);
        c.setCellValue("Item Number");
        c.setCellStyle(cs);

        c = row.createCell(1);
        c.setCellValue("Quantity");
        c.setCellStyle(cs);

        c = row.createCell(2);
        c.setCellValue("Price");
        c.setCellStyle(cs);
        */
        sheet1.setColumnWidth(50, 500);
        /*
        sheet1.setColumnWidth(0, (15 * 500));
        sheet1.setColumnWidth(1, (15 * 500));
        sheet1.setColumnWidth(2, (15 * 500));
        */
        // Create a path where we will place our List of objects on external storage
        //File file = new File(context.getExternalFilesDir(null), fileName);
        File fileDir = new File(UMGEBUNG);
        if(!fileDir.exists())
            fileDir.mkdirs();

        File file = new File(UMGEBUNG, fileName);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }

        return success;
    }

    public static boolean changeExCell(Context context, String filename, int rowidx, int columnidx, int value){
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            //TODO: Fehlermeldung
        }
        try{
            // Creating Input Stream
            File file = new File(UMGEBUNG, filename);//"Strafenkatalog.xls");
            FileInputStream fileIn = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(fileIn);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            //Position der zu ändernden Zelle
            Row row = mySheet.getRow(rowidx+1);

            Cell cell = row.getCell(columnidx+1);
            if (cell == null){
                cell = row.createCell(columnidx+1);
            }
            double cellval;
            cellval = cell.getNumericCellValue();
            //Wert der Zelle ändern
            cell.setCellValue(cellval + value);

            //Write the output back to the File
            FileOutputStream fileOut = new FileOutputStream(new File(UMGEBUNG, filename));
            myWorkBook.write(fileOut);
            fileOut.close();
            return true;

        }
        catch (Exception e) {
                e.printStackTrace();
                return false;
        }

    }

    public static boolean writeExCell(Context context, String filename, int rowidx, int columnidx, String text){
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            //TODO: Fehlermeldung
        }
        try{
            // Creating Input Stream
            File file = new File(UMGEBUNG, filename);//"Strafenkatalog.xls");
            FileInputStream fileIn = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(fileIn);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            //Position der zu ändernden Zelle
            Row row = mySheet.getRow(rowidx);
            if (row == null) {
                row = mySheet.createRow(rowidx);//getRow(rowidx);
            }
            Cell cell = row.getCell(columnidx);
            if (cell == null){
                cell = row.createCell(columnidx);
            }
            String cellval;
//            cellval = cell.getStringCellValue();
/*            if(cellval != null || cellval != "" || cellval != " "){

            }
            else*/
            //Text in Zelle schreiben
            cell.setCellValue(text);

            //Write the output back to the File
            FileOutputStream fileOut = new FileOutputStream(new File(UMGEBUNG, filename));
            myWorkBook.write(fileOut);
            fileOut.close();
            return true;

        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    public static boolean writeExCell(Context context, String filename, int rowidx, int columnidx, Integer number){
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            //TODO: Fehlermeldung
        }
        try{
            // Creating Input Stream
            File file = new File(UMGEBUNG, filename);//"Strafenkatalog.xls");
            FileInputStream fileIn = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(fileIn);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            //Position der zu ändernden Zelle
            Row row = mySheet.getRow(rowidx);
            if (row == null) {
                row = mySheet.createRow(rowidx);//getRow(rowidx);
            }
            Cell cell = row.getCell(columnidx);
            if (cell == null){
                cell = row.createCell(columnidx);
            }
            String cellval;

            //cellval = cell.getStringCellValue();
/*            if(cellval != null || cellval != "" || cellval != " "){

            }
            else*/
            //Text in Zelle schreiben
            cell.setCellValue(number);

            //Write the output back to the File
            FileOutputStream fileOut = new FileOutputStream(new File(UMGEBUNG, filename));
            myWorkBook.write(fileOut);
            fileOut.close();
            return true;

        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean writeExCell(Context context, String filename, int rowidx, int columnidx, Float number){
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            //TODO: Fehlermeldung
        }
        try{
            // Creating Input Stream
            File file = new File(UMGEBUNG, filename);//"Strafenkatalog.xls");
            FileInputStream fileIn = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(fileIn);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            //Position der zu ändernden Zelle
            Row row = mySheet.getRow(rowidx);
            if (row == null) {
                row = mySheet.createRow(rowidx);//getRow(rowidx);
            }
            Cell cell = row.getCell(columnidx);
            if (cell == null){
                cell = row.createCell(columnidx);
            }
            String cellval;
            //cellval = cell.getStringCellValue();
/*            if(cellval != null || cellval != "" || cellval != " "){

            }
            else*/
            //Text in Zelle schreiben
            cell.setCellValue(number);

            //Write the output back to the File
            FileOutputStream fileOut = new FileOutputStream(new File(UMGEBUNG, filename));
            myWorkBook.write(fileOut);
            fileOut.close();
            return true;

        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static float readExCellFloat(Context context, String filename, int rowidx, int columnidx){
        try {
            // Creating Input Stream
            File file = new File(UMGEBUNG, filename);//"Strafenkatalog.xls");
            FileInputStream fileIn = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(fileIn);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            Row row = mySheet.getRow(rowidx);
            Cell cell = row.getCell(columnidx);
            float cellval;
            cellval = (float) cell.getNumericCellValue();
            return cellval;
            }
        catch (Exception e){e.printStackTrace(); return 0;}
    }

    public static int readExCellInt(Context context, String filename, int rowidx, int columnidx){
        try {
            // Creating Input Stream
            File file = new File(UMGEBUNG, filename);//"Strafenkatalog.xls");
            FileInputStream fileIn = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(fileIn);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            Row row = mySheet.getRow(rowidx);
            Cell cell = row.getCell(columnidx);
            int cellval;
            cellval = (int) cell.getNumericCellValue();
            return cellval;
        }
        catch (Exception e){e.printStackTrace(); return 0;}
    }

    public static String readExCellString(Context context, String filename, int rowidx, int columnidx){
        try {
            // Creating Input Stream
            File file = new File(UMGEBUNG, filename);//"Strafenkatalog.xls");
            FileInputStream fileIn = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(fileIn);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);


            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            Row row = mySheet.getRow(rowidx);
            Cell cell = row.getCell(columnidx);
            String cellval;
            cellval =  cell.getStringCellValue();
            return cellval;
        }
        catch (Exception e){e.printStackTrace(); return "Error reading Cell!";}
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}
