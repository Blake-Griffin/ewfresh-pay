package com.ewfresh.pay.util;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 本工具类是一个通用的Excel导出工具类
 * 模板：一级标题（title0）  二级标题（title1）  数据表（dataRow）
 * 使用方法：二级标题汉化调用setChineseMap<String,String>()方法
 *          调用exportExcel()方法设计导出Excel
 * @author blue sun
 */
public class POIUtil {
    private static final short FONTSIZE_0=18;
    private static final short FONTSIZE_1=14;
    private static final short FONTSIZE_2=10;
    private static final int COLUMN_START=0;
    private static final int COLUMN_TITLE0=0;
    private static final int COLUMN_WIDTH=15;
    private static final int ROW_START=0;
    private static final int ROW_END=1;
    private static final int ROWNUM_TITLE0=0;
    private static final int ROWNUM_TITLE1=2;
    private static final int FIELD_SUB_START=0;
    private static final int FIELD_SUB_END=1;
    private static final int DATAROW_ADD=3;

    private static Map<String,String> ChineseMap = null;

    /**
     * 导出方法
     * @param workbook：工作簿，仅仅支持HSSFWorkBook
     * @param datas：倒数对象集合
     * @param title：文件名，一级标题
     * @param clazz：对象类型
     */
    public static Workbook  exportExcel(Workbook workbook, List datas, String title, Class clazz){
        try {
            if (null == datas || datas.isEmpty()){//没有需要导出的数据
                throw new RuntimeException("No datas what you want to export");
            }
            if (StringUtils.isEmpty(title))
                title = "newFile";
            Sheet sheet = workbook.createSheet(title);//建立新的sheet对象（excel的表单）
            Field[] fields = clazz.getDeclaredFields();//获取实体属性名
            //合并区域
            CellRangeAddress address = new CellRangeAddress(ROW_START, ROW_END, COLUMN_START, fields.length-1);
            sheet.addMergedRegion(address);//设置合并区域生效
            sheet.setDefaultColumnWidth(COLUMN_WIDTH);//设置默认列宽
            /*for(int i = 0;i<fields.length;i++){
                sheet.autoSizeColumn(i);
            }//列宽自适应*/
            //创建一级标题
            Row title0 = sheet.createRow(ROWNUM_TITLE0);//开始行
            Cell cell = title0.createCell(COLUMN_TITLE0);//开始列
            cell.setCellStyle(getCellStyle(workbook,FONTSIZE_0));//设置样式
            cell.setCellValue(title);//设置一级标题名
            //创建二级标题
            Row title1 = sheet.createRow(ROWNUM_TITLE1);//从哪一行开始
            title1.setHeight((short)350);
            if (ChineseMap == null) {//不需要标题汉化
                for (int i = 0; i < fields.length; i++) {
                    Cell colCell = title1.createCell(i);//循环创建列
                    colCell.setCellStyle(getCellStyle(workbook,FONTSIZE_1));//设置样式
                    colCell.setCellValue(fields[i].getName());//设置值
                }
            }else {//需要标题汉化
                for (int i = 0; i < fields.length; i++) {
                    Cell colCell = title1.createCell(i);
                    colCell.setCellStyle(getCellStyle(workbook,FONTSIZE_1));
                    colCell.setCellValue(toChinese(fields[i].getName()));
                }
            }
            //创建数据行
            CellStyle cellStyle = getCellStyle(workbook, FONTSIZE_2);
            for (int i = 0; i < datas.size(); i++) {
                Row dataRow = sheet.createRow(i + DATAROW_ADD);
                for (int j = 0; j < fields.length; j++) {
                    Cell dataCell = dataRow.createCell(j);
                    //获取属性get方法名
                    String getMethodName = "get" +
                            fields[j].getName().substring(FIELD_SUB_START, FIELD_SUB_END).toUpperCase()
                            + fields[j].getName().substring(FIELD_SUB_END);
                    //返回方法对象 //参数一:方法的名字   //参数二:方法参数的类型（无参）
                    Method getMethod = clazz.getDeclaredMethod(getMethodName, new Class[]{});
                    //执行方法  参数一:执行那个对象中的方法    参数二:该方法的参数
                    Object value = getMethod.invoke(datas.get(i), new Object[]{});
                    //设置单元格显示格式控件
                    judgeAndSetValue(workbook, value, dataCell,cellStyle);
                }
            }
            return workbook;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置样式
     * @param workbook：工作簿
     * @param fontSize：字号
     * font.setFontName("");：设置字体类型，如：宋体
     */
    private static CellStyle getCellStyle(Workbook workbook,short fontSize){
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//居中对齐
        Font font = workbook.createFont();
        if (fontSize == FONTSIZE_0 || fontSize == FONTSIZE_1)
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//设置粗体显示
        font.setFontHeightInPoints(fontSize);//设置字体大小
        cellStyle.setFont(font);//设置字体样式生效
//        cellStyle.setWrapText(true);//设置自动换行
        return cellStyle;
    }

    /**
     * description：根据值得类型设置单元格格式并给单元格设值
     * @param workbook：工作簿
     * @param value：通过get方法获取的值
     * @param dataCell：数据单元格
     */
    private static void judgeAndSetValue(Workbook workbook, Object value, Cell dataCell,CellStyle cellStyle){
        DataFormat df = workbook.createDataFormat();//数据格式对象
        if (value instanceof Integer){//整数类型
//            CellStyle cellStyle = getCellStyle(workbook, FONTSIZE_2);
            dataCell.setCellStyle(cellStyle);
            dataCell.setCellValue((Integer)value);
        }
        if (value instanceof Long){//长整数类型
//            CellStyle cellStyle = getCellStyle(workbook, FONTSIZE_2);
            dataCell.setCellStyle(cellStyle);
            dataCell.setCellValue((Long)value);
        }
        if (value instanceof Double){//双精度小数类型
//            CellStyle cellStyle = getCellStyle(workbook, FONTSIZE_2);
            cellStyle.setDataFormat(df.getFormat("#,#0.00"));
            dataCell.setCellStyle(cellStyle);
            dataCell.setCellValue((Double)value);
        }
        if (value instanceof Float){//单精度小数类型
//            CellStyle cellStyle = getCellStyle(workbook, FONTSIZE_2);
            cellStyle.setDataFormat(df.getFormat("#,#0.0"));
            dataCell.setCellStyle(cellStyle);
            dataCell.setCellValue((Float)value);
        }
        if (value instanceof Date){//日期类型
//            CellStyle cellStyle = getCellStyle(workbook, FONTSIZE_2);
//            cellStyle.setDataFormat(df.getFormat("yyyy-MM-dd HH:mm:ss"));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dataCell.setCellStyle(cellStyle);
            dataCell.setCellValue(simpleDateFormat.format(value));
        }
        if(value instanceof String){//字符串类型
//            CellStyle cellStyle = getCellStyle(workbook, FONTSIZE_2);
            dataCell.setCellStyle(cellStyle);
            dataCell.setCellValue((String)value);
        }
        if (value instanceof BigDecimal){
            double doubleValue = ((BigDecimal) value).doubleValue();
//            CellStyle cellStyle = getCellStyle(workbook, FONTSIZE_2);
            dataCell.setCellStyle(cellStyle);
            dataCell.setCellValue(doubleValue);
        }
    }

    /**
     * 标题汉化,可以使用枚举来维护ChineseMap，如下 PeopleFields是一个枚举
     * PeopleFields[] fields = PeopleFields.values();
     * for (PeopleFields field : fields) {
     *   ChineseMap.put(field.name(),field.toChinese());
     * }
     * @param ChineseMap1：key为实体类属性名，value为对应的汉语意思
     */
    public static void setChineseMap(Map<String, String> ChineseMap1){
        ChineseMap=ChineseMap1;
    }

    /**
     * 获取汉化二级标题
     * @param key：实体类的属性名
     */
    private static String toChinese(String key){
        return ChineseMap.get(key);
    }
}