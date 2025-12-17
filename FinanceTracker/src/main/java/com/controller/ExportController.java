package com.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.entity.Transaction;
import com.entity.User;
import com.service.TransactionService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/excel")
    public void exportExcel(Authentication auth, HttpServletResponse response) throws Exception {
        User user = (User) auth.getPrincipal();
        List<Transaction> list = transactionService.findByUser(user);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("transactions");
            Row header = s.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Amount");
            header.createCell(2).setCellValue("Type");
            header.createCell(3).setCellValue("Date");
            header.createCell(4).setCellValue("Note");
            header.createCell(5).setCellValue("Category");

            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
            int r = 1;
            for (Transaction t : list) {
                Row row = s.createRow(r++);
                row.createCell(0).setCellValue(t.getId() != null ? t.getId() : "");
                row.createCell(1).setCellValue(t.getAmount() != null ? t.getAmount() : 0.0);
                row.createCell(2).setCellValue(t.getType() != null ? t.getType() : "");
                row.createCell(3).setCellValue(t.getDate() != null ? t.getDate().format(fmt) : "");
                row.createCell(4).setCellValue(t.getNote() != null ? t.getNote() : "");
                row.createCell(5).setCellValue(t.getCategory() != null ? t.getCategory().getName() : "");
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=transactions.xlsx");
            wb.write(response.getOutputStream());
        }
    }
}
