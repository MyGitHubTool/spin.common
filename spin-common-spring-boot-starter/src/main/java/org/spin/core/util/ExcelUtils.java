package org.spin.core.util;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.function.serializable.ExceptionalSupplier;
import org.spin.core.io.BytesCombinedInputStream;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.excel.ExcelGrid;
import org.spin.core.util.excel.ExcelModel;
import org.spin.core.util.excel.ExcelRow;
import org.spin.core.util.excel.GridColumn;
import org.spin.core.util.file.FileType;
import org.spin.core.util.file.FileTypeUtils;

import java.io.*;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * Excel工具类
 * <p>Created by xuweinan on 2017/9/3.</p>
 *
 * @author xuweinan
 */
public abstract class ExcelUtils {
    private static final Map<String, String> DEFAULT_DATA_TYPE_FORMAT = new HashMap<>();
    private static final ThreadLocal<Map<String, String>> DATA_TYPE_FORMAT = new ThreadLocal<>();

    static {
        String dateFormat = DateFormatConverter.convert(Locale.SIMPLIFIED_CHINESE, "yyyy-MM-dd HH:mm:ss");
        DEFAULT_DATA_TYPE_FORMAT.put("date", dateFormat);
    }

    public enum Type {
        XLS, XLSX
    }

    /**
     * 读取Excel文件内容
     *
     * @param is     输入流
     * @param reader Excel行处理器
     */
    public static void readWorkBook(InputStream is, FinalConsumer<ExcelRow> reader) {
        byte[] trait = new byte[16];
        FileType fileType;
        BytesCombinedInputStream bcis;
        try {
            bcis = new BytesCombinedInputStream(is, 16);
            bcis.readCombinedBytes(trait);
            fileType = FileTypeUtils.detectFileType(trait);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "输入流读取失败", e);
        }
        if (Objects.isNull(fileType)) {
            throw new SpinException(ErrorCode.IO_FAIL, "不支持的文件类型");
        } else if (fileType.equals(FileType.Document.XLS)) {
            readWorkBook(bcis, Type.XLS, reader);
        } else if (fileType.equals(FileType.Document.XLSX)) {
            readWorkBook(bcis, Type.XLSX, reader);
        } else {
            throw new SpinException(ErrorCode.IO_FAIL, "不支持的文件类型");
        }
    }

    /**
     * 读取Excel文件内容
     *
     * @param is     输入流
     * @param type   类型，xls或xlsx
     * @param reader Excel行处理器
     */
    public static void readWorkBook(InputStream is, Type type, FinalConsumer<ExcelRow> reader) {
        Workbook workbook;
        try {
            if (Type.XLS.equals(type)) {
                workbook = new HSSFWorkbook(is);
            } else {
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "读取文件失败", e);
        }
        // 循环工作表Sheet
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            if (sheet == null) {
                continue;
            }
            ExcelRow rowData = new ExcelRow(sheetIndex, sheet.getSheetName(), -1, sheet.getRow(sheet.getLastRowNum()).getLastCellNum() * 3);
            // 循环行Row
            Row row;
            boolean hasValidCell;
            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                hasValidCell = false;
                row = sheet.getRow(rowNum);
                if (row == null || row.getLastCellNum() < 1) {
                    continue;
                }
                rowData.cleanRow();
                rowData.setRowIndex(rowNum);

                Iterator<Cell> cells = row.cellIterator();
                String cellValue;
                Cell cell;
                while (cells.hasNext()) {
                    cell = cells.next();
                    cellValue = getCellValue(cell);
                    rowData.setColumn(cell.getColumnIndex(), cellValue);
                    hasValidCell = hasValidCell || StringUtils.isNotEmpty(cellValue);
                }
                if (hasValidCell) {
                    reader.accept(rowData);
                }
            }
        }
    }

    /**
     * 读取Excel文件内容
     *
     * @param workbookFile Excel文件
     * @param reader       Excel行处理器
     */
    public static void readFromFile(File workbookFile, FinalConsumer<ExcelRow> reader) {
        Type type;
        if (workbookFile.getName().toLowerCase().endsWith("xls")) {
            type = Type.XLS;
        } else if (workbookFile.getName().toLowerCase().endsWith("xlsx")) {
            type = Type.XLSX;
        } else {
            throw new SpinException(ErrorCode.IO_FAIL, "不支持的文件类型");
        }

        try (InputStream is = new FileInputStream(workbookFile)) {
            readWorkBook(is, type, reader);
        } catch (FileNotFoundException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "读取的文件不存在", e);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "读取文件失败", e);
        }
    }

    /**
     * 读取Excel文件内容
     *
     * @param workbookFilePath Excel文件路径
     * @param reader           Excel行处理器
     */
    public static void readFromFile(String workbookFilePath, FinalConsumer<ExcelRow> reader) {
        File workbookFile = new File(workbookFilePath);
        readFromFile(workbookFile, reader);
    }

    /**
     * 得到Excel表中的值
     *
     * @param cell Excel中的每一个单元格
     * @return 单元格的值(字符串形式)
     */
    private static String getCellValue(Cell cell) {
        String result;
        switch (cell.getCellType()) {
            case STRING:
                result = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    result = DateUtils.formatDateForSecond(date);
                } else {
                    result = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case FORMULA:
                try {
                    result = cell.getStringCellValue();
                } catch (IllegalStateException ignore) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        result = DateUtils.formatDateForSecond(cell.getDateCellValue());
                    } else {
                        result = String.valueOf(cell.getNumericCellValue());
                    }
                }
                break;
            case BOOLEAN:
                result = String.valueOf(cell.getBooleanCellValue());
                break;
            case BLANK:
                result = "";
                break;
            default:
                result = String.valueOf(cell.getStringCellValue());

        }
        return result;
    }

    public static void generateWorkBook(FileType fileType, ExcelModel excelModel, OutputStream outputStream) {
        try {
            Workbook workbook = generateWorkbook(fileType, excelModel);
            Throwable var4 = null;

            try {
                workbook.write(outputStream);
                outputStream.flush();
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (workbook != null) {
                    if (var4 != null) {
                        try {
                            workbook.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        workbook.close();
                    }
                }

            }

        } catch (IOException var16) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "Excel写出workbook失败", var16);
        }
    }

    public static void generateWorkBook(FileType fileType, ExcelModel excelModel, ExceptionalSupplier<OutputStream, IOException> outputStreamSupplier) {
        try {
            Workbook workbook = generateWorkbook(fileType, excelModel);
            Throwable var4 = null;

            try {
                OutputStream outputStream = outputStreamSupplier.get();
                Throwable var6 = null;

                try {
                    workbook.write(outputStream);
                    outputStream.flush();
                } catch (Throwable var31) {
                    var6 = var31;
                    throw var31;
                } finally {
                    if (outputStream != null) {
                        if (var6 != null) {
                            try {
                                outputStream.close();
                            } catch (Throwable var30) {
                                var6.addSuppressed(var30);
                            }
                        } else {
                            outputStream.close();
                        }
                    }

                }
            } catch (Throwable var33) {
                var4 = var33;
                throw var33;
            } finally {
                if (workbook != null) {
                    if (var4 != null) {
                        try {
                            workbook.close();
                        } catch (Throwable var29) {
                            var4.addSuppressed(var29);
                        }
                    } else {
                        workbook.close();
                    }
                }

            }

        } catch (IOException var35) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "Excel写出workbook失败", var35);
        }
    }

    public static Workbook generateWorkbook(FileType fileType, ExcelModel excelModel) {
        ExcelGrid grid = excelModel.getGrid();
        Iterable<?> data = excelModel.getData();
        Workbook workbook = createWorkbook(fileType);
        Sheet sheet = workbook.createSheet();
        sheet.createFreezePane(1, 1);
        CellStyle columnHeadStyle = getHeaderCellStyle(workbook);

        try {
            Row row0 = sheet.createRow(0);
            row0.setHeight((short) 285);
            Map<String, CellStyle> columnStyleMap = new HashMap();

            int i;
            for (i = 0; i < grid.getColumns().size(); ++i) {
                GridColumn col = grid.getColumns().get(i);
                if (!grid.getExcludeColumns().contains(col.getHeader())) {
                    if (col.getWidth() != null) {
                        sheet.setColumnWidth(i, col.getWidth() * 37);
                    } else {
                        sheet.setColumnWidth(i, 3700);
                    }

                    Cell cell = row0.createCell(i);
                    cell.setCellStyle(columnHeadStyle);
                    if (workbook instanceof HSSFWorkbook) {
                        cell.setCellValue(new HSSFRichTextString(col.getHeader()));
                    } else if (workbook instanceof XSSFWorkbook) {
                        cell.setCellValue(new XSSFRichTextString(col.getHeader()));
                    }

                    columnStyleMap.put(col.getDataIndex(), getDataCellStyle(workbook, col.getDataType()));
                }
            }

            i = 1;

            for (Iterator var20 = data.iterator(); var20.hasNext(); ++i) {
                Object robj = var20.next();
                Row row = sheet.createRow(i);
                row0.setHeight((short) 285);

                for (int c = 0; c < grid.getColumns().size(); ++c) {
                    GridColumn col = grid.getColumns().get(c);
                    if (!grid.getExcludeColumns().contains(col.getHeader())) {
                        Cell cell = row.createCell(c);
                        setDataCellValue(robj, cell, col);
                        if (col.getWidth() == null) {
                            int columnWidth = 10;
                            int length = cell.getStringCellValue().getBytes().length;
                            if (columnWidth < length) {
                                columnWidth = length;
                            }

                            int columnWidthFinal = columnWidth + 2;
                            if (columnWidthFinal > 255) {
                                columnWidthFinal = 255;
                            }

                            sheet.setColumnWidth(c, columnWidthFinal * 256);
                        }

                        cell.setCellStyle(columnStyleMap.get(col.getDataIndex()));
                    }
                }
            }

            return workbook;
        } catch (Exception var19) {
            throw new SimplifiedException("生成Excel文件[" + grid.getFileName() + "]出错", var19);
        }
    }

    private static Workbook createWorkbook(FileType fileType) {
        String var1 = fileType.getExtension();
        byte var2 = -1;
        switch (var1.hashCode()) {
            case 1489169:
                if (var1.equals(".xls")) {
                    var2 = 0;
                }
                break;
            case 46164359:
                if (var1.equals(".xlsx")) {
                    var2 = 1;
                }
        }

        switch (var2) {
            case 0:
                return new HSSFWorkbook();
            case 1:
                return new XSSFWorkbook();
            default:
                throw new SimplifiedException("不支持的文件类型: " + fileType.getExtension());
        }
    }

    private static CellStyle getHeaderCellStyle(Workbook workbook) {
        Font columnHeadFont = workbook.createFont();
        columnHeadFont.setFontName("宋体");
        columnHeadFont.setFontHeightInPoints((short) 10);
        columnHeadFont.setBold(true);
        CellStyle columnHeadStyle = workbook.createCellStyle();
        columnHeadStyle.setFont(columnHeadFont);
        columnHeadStyle.setAlignment(HorizontalAlignment.CENTER);
        columnHeadStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        columnHeadStyle.setLocked(true);
        columnHeadStyle.setWrapText(false);
        columnHeadStyle.setBorderTop(BorderStyle.THIN);
        columnHeadStyle.setBorderRight(BorderStyle.THIN);
        columnHeadStyle.setBorderBottom(BorderStyle.THIN);
        columnHeadStyle.setBorderLeft(BorderStyle.THIN);
        columnHeadStyle.setTopBorderColor((short) 8);
        columnHeadStyle.setRightBorderColor((short) 8);
        columnHeadStyle.setBottomBorderColor((short) 8);
        columnHeadStyle.setLeftBorderColor((short) 8);
        columnHeadStyle.setFillForegroundColor((short) 9);
        return columnHeadStyle;
    }

    private static CellStyle getDataCellStyle(Workbook workbook, String dataType) {
        Font font = workbook.createFont();
        CreationHelper createHelper = workbook.getCreationHelper();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(false);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setTopBorderColor((short) 8);
        style.setRightBorderColor((short) 8);
        style.setBottomBorderColor((short) 8);
        style.setLeftBorderColor((short) 8);
        style.setFillForegroundColor((short) 9);
        String format = getColumnDataTypeFormat(dataType);
        if (StringUtils.isNotEmpty(format)) {
            style.setDataFormat(createHelper.createDataFormat().getFormat(format));
        }

        return style;
    }

    private static void setDataCellValue(Object rdata, Cell cell, GridColumn col) {
        Object o;
        if (rdata instanceof Map) {
            o = ((Map) rdata).get(col.getDataIndex());
        } else {
            o = BeanUtils.getFieldValue(rdata, col.getDataIndex());
        }

        if (o != null) {
            Object t = null;
            if (o instanceof Enum) {
                t = ((Enum) o).name();
            } else if (o instanceof TemporalAccessor) {
                try {
                    t = DateUtils.toDate((TemporalAccessor) o);
                } catch (Exception var10) {
                }
            }

            if (null != t) {
                o = t;
            }

            String dataType = StringUtils.trimToEmpty(col.getDataType());
            byte var7 = -1;
            switch (dataType.hashCode()) {
                case 3076014:
                    if (dataType.equals("date")) {
                        var7 = 0;
                    }
                    break;
                case 64711720:
                    if (dataType.equals("boolean")) {
                        var7 = 1;
                    }
            }

            switch (var7) {
                case 0:
                    if (o instanceof Date) {
                        cell.setCellValue((Date) o);
                    } else {
                        try {
                            cell.setCellValue(DateUtils.toDate(o.toString()));
                        } catch (Exception var9) {
                            cell.setCellValue(o.toString());
                        }
                    }
                    break;
                case 1:
                    if (o instanceof Boolean) {
                        if (Boolean.TRUE.equals(o)) {
                            cell.setCellValue("是");
                        } else {
                            cell.setCellValue("否");
                        }
                    } else if (!o.toString().equalsIgnoreCase("false") && !o.toString().equals("0")) {
                        cell.setCellValue("是");
                    } else {
                        cell.setCellValue("否");
                    }
                    break;
                default:
                    if (o instanceof Boolean) {
                        if (Boolean.TRUE.equals(o)) {
                            cell.setCellValue("是");
                        } else {
                            cell.setCellValue("否");
                        }
                    } else if (o instanceof Date) {
                        cell.setCellValue(DateUtils.formatDateForSecond((Date) o));
                    } else {
                        cell.setCellValue(o.toString());
                    }
            }

        }
    }

    private static String getColumnDataTypeFormat(String dataType) {
        if (StringUtils.isEmpty(dataType)) {
            return null;
        } else {
            String format = BeanUtils.getFieldValue(DATA_TYPE_FORMAT.get(), "#" + dataType);
            return StringUtils.isEmpty(format) ? DEFAULT_DATA_TYPE_FORMAT.get(dataType) : format;
        }
    }
}
