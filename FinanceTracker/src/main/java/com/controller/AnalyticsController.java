package com.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entity.Transaction;
import com.entity.User;
import com.service.TransactionService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/monthly-summary")
    public Map<String, Object> monthlySummary(Authentication auth,
                                              @RequestParam(required = false) Integer year,
                                              @RequestParam(required = false) Integer month) {

        User user = (User) auth.getPrincipal();
        YearMonth ym = (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        double income = 0.0, expense = 0.0;
        for (Transaction t : transactionService.findByUser(user)) {
            if (t.getDate() == null) continue;
            if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                if ("INCOME".equalsIgnoreCase(t.getType())) income += t.getAmount() != null ? t.getAmount() : 0.0;
                else expense += t.getAmount() != null ? t.getAmount() : 0.0;
            }
        }

        Map<String,Object> res = new HashMap<>();
        res.put("year", ym.getYear());
        res.put("month", ym.getMonthValue());
        res.put("income", income);
        res.put("expense", expense);
        res.put("net", income - expense);
        return res;
    }

    @GetMapping("/category-breakdown")
    public Map<String, Double> categoryBreakdown(Authentication auth,
                                                 @RequestParam(required = false) Integer year,
                                                 @RequestParam(required = false) Integer month) {
        User user = (User) auth.getPrincipal();
        YearMonth ym = (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        Map<String, Double> map = new HashMap<>();
        for (Transaction t : transactionService.findByUser(user)) {
            if (t.getDate() == null) continue;
            // Only include expenses for category breakdown
            if (!"EXPENSE".equalsIgnoreCase(t.getType())) continue;
            
            if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                String cat = t.getCategory() != null ? t.getCategory().getName() : "Uncategorized";
                map.put(cat, map.getOrDefault(cat, 0.0) + (t.getAmount() != null ? t.getAmount() : 0.0));
            }
        }
        return map;
    }
}
