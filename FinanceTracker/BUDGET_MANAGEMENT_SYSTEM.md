# Budget Management System - Architecture & Design

## Overview

This document describes the comprehensive budget management system for the Personal Finance Tracker application. The system supports overall monthly budgets, category-wise budgets, real-time expense tracking, budget validation, alerts, and analytics.

---

## 1. MongoDB Schema Design

### 1.1 Budget Document

```json
{
  "_id": "ObjectId",
  "user": { "$ref": "users", "$id": "ObjectId" },
  "category": { "$ref": "categories", "$id": "ObjectId" } | null,
  "monthlyLimit": 5000.00,
  "year": 2024,
  "month": 11,
  "allowRollover": false,
  "preventExceed": false,
  "createdAt": ISODate("2024-11-01T10:00:00Z"),
  "updatedAt": ISODate("2024-11-15T14:30:00Z")
}
```

**Key Points:**
- `category: null` → Overall monthly budget
- `category: ObjectId` → Category-specific budget
- Unique constraint: `(user, year, month, category)` - prevents duplicate budgets
- `allowRollover`: If true, unused budget carries to next month (future enhancement)
- `preventExceed`: If true, blocks expenses exceeding budget; if false, allows with warning

### 1.2 Transaction Document

```json
{
  "_id": "ObjectId",
  "user": { "$ref": "users", "$id": "ObjectId" },
  "amount": 150.00,
  "type": "EXPENSE" | "INCOME",
  "date": ISODate("2024-11-15"),
  "category": { "$ref": "categories", "$id": "ObjectId" },
  "note": "Grocery shopping"
}
```

**Key Points:**
- Only `type: "EXPENSE"` transactions affect budgets
- `date` determines which month's budget is affected
- `category` links to category budgets

### 1.3 User Document

```json
{
  "_id": "ObjectId",
  "email": "user@example.com",
  "name": "John Doe",
  "password": "hashed_password",
  "createdAt": ISODate("2024-01-01T00:00:00Z")
}
```

### 1.4 Category Document

```json
{
  "_id": "ObjectId",
  "user": { "$ref": "users", "$id": "ObjectId" },
  "name": "Food"
}
```

---

## 2. Budget Calculation Logic (Step-by-Step)

### 2.1 Overall Budget Calculation

**Step 1: Identify Budget Period**
```
Input: year, month (defaults to current month if not provided)
Output: YearMonth object (e.g., YearMonth.of(2024, 11))
```

**Step 2: Get Overall Budget**
```
Query: findByUserAndYearAndMonthAndCategoryIsNull(user, year, month)
Result: Budget object with monthlyLimit
```

**Step 3: Calculate Total Expenses**
```
Query: findByUserAndTypeAndDateBetween(user, "EXPENSE", startDate, endDate)
Where:
  - startDate = YearMonth.atDay(1)  // First day of month
  - endDate = YearMonth.atEndOfMonth()  // Last day of month

Calculation:
  totalSpent = Σ(transaction.amount) for all EXPENSE transactions in date range
```

**Step 4: Calculate Metrics**
```
remaining = monthlyLimit - totalSpent
usagePercentage = (totalSpent / monthlyLimit) * 100
status = calculateStatus(usagePercentage)
```

**Step 5: Determine Status**
```
if usagePercentage >= 90%:
  status = EXCEEDED
else if usagePercentage >= 50%:
  status = WARNING
else:
  status = SAFE
```

### 2.2 Category Budget Calculation

**For each category budget:**

**Step 1: Get Category Budget**
```
Query: findByUserAndYearAndMonthAndCategory(user, year, month, category)
```

**Step 2: Calculate Category Expenses**
```
Query: findByUserAndTypeAndCategoryAndDateBetween(user, "EXPENSE", category, startDate, endDate)

Calculation:
  categorySpent = Σ(transaction.amount) 
    where transaction.category.id == category.id
    and transaction.date is within month range
```

**Step 3: Calculate Category Metrics**
```
categoryRemaining = categoryLimit - categorySpent
categoryUsagePercentage = (categorySpent / categoryLimit) * 100
categoryStatus = calculateStatus(categoryUsagePercentage)
```

### 2.3 Budget Status Formula

```
Status Calculation:
  usagePercentage = (spent / budget) * 100

  if usagePercentage >= 100:
    → EXCEEDED (over budget)
  else if usagePercentage >= 90:
    → EXCEEDED (critical warning)
  else if usagePercentage >= 50:
    → WARNING (moderate usage)
  else:
    → SAFE (low usage)
```

### 2.4 Alert Generation

Alerts are generated when budget usage crosses thresholds:

**50% Threshold:**
```
if 50% <= usagePercentage < 75%:
  Alert: {
    type: "OVERALL" | categoryId,
    threshold: 50,
    severity: "INFO",
    message: "Budget is X% used (Y / Z)"
  }
```

**75% Threshold:**
```
if 75% <= usagePercentage < 90%:
  Alert: {
    type: "OVERALL" | categoryId,
    threshold: 75,
    severity: "WARNING",
    message: "Budget is X% used (Y / Z)"
  }
```

**90% Threshold:**
```
if 90% <= usagePercentage < 100%:
  Alert: {
    type: "OVERALL" | categoryId,
    threshold: 90,
    severity: "CRITICAL",
    message: "Budget is X% used (Y / Z). Approaching limit!"
  }
```

**100% Threshold (Exceeded):**
```
if usagePercentage >= 100%:
  Alert: {
    type: "OVERALL" | categoryId,
    threshold: 100,
    severity: "CRITICAL",
    message: "Budget EXCEEDED! Spent X exceeds limit of Y by Z"
  }
```

---

## 3. Budget Validation Rules

### 3.1 Expense Validation Flow

When adding/updating an expense:

**Step 1: Identify Budget Period**
```
expenseDate = transaction.date (or current date if null)
year = expenseDate.getYear()
month = expenseDate.getMonthValue()
```

**Step 2: Check Overall Budget**
```
overallBudget = findByUserAndYearAndMonthAndCategoryIsNull(user, year, month)
currentSpent = getCurrentSpending(user, year, month, null)
newTotal = currentSpent + expenseAmount

if newTotal > overallBudget.monthlyLimit:
  if overallBudget.preventExceed == true:
    → REJECT: "Expense would exceed overall monthly budget"
  else:
    → ALLOW with WARNING: "Warning: Expense exceeds overall monthly budget"
```

**Step 3: Check Category Budget (if category specified)**
```
categoryBudget = findByUserAndYearAndMonthAndCategory(user, year, month, category)
currentCategorySpent = getCurrentSpending(user, year, month, category)
newCategoryTotal = currentCategorySpent + expenseAmount

if newCategoryTotal > categoryBudget.monthlyLimit:
  if categoryBudget.preventExceed == true:
    → REJECT: "Expense would exceed category budget: {categoryName}"
  else:
    → ALLOW with WARNING: "Warning: Expense exceeds category budget: {categoryName}"
```

**Step 4: Save Transaction**
```
if validation.allowed == true:
  save(transaction)
else:
  throw IllegalArgumentException(validation.message)
```

---

## 4. Edge Cases & Handling

### 4.1 Month Change

**Scenario:** User adds expense on Nov 30, then adds another on Dec 1.

**Handling:**
- Each expense's `date` field determines which month's budget it affects
- Nov 30 expense → affects November budget
- Dec 1 expense → affects December budget
- Budgets are scoped by `(user, year, month)`, so they're independent

**Implementation:**
```java
YearMonth yearMonth = YearMonth.from(expense.getDate());
Integer year = yearMonth.getYear();
Integer month = yearMonth.getMonthValue();
// Query budget for this specific year/month
```

### 4.2 Budget Update Mid-Month

**Scenario:** User sets budget to $1000 on Nov 1, spends $600, then updates budget to $500 on Nov 15.

**Handling:**
- Budget update overwrites existing budget for that month
- Real-time calculation uses current budget limit
- Previous spending ($600) is recalculated against new limit ($500)
- Status changes from SAFE (60%) to EXCEEDED (120%)

**Implementation:**
```java
// BudgetService.createOrUpdateBudget() finds existing budget
Optional<Budget> existing = findByUserAndYearAndMonthAndCategory(...);
if (existing.isPresent()) {
    budget = existing.get();
    budget.setMonthlyLimit(newLimit); // Update limit
    budget.setUpdatedAt(LocalDateTime.now());
}
save(budget);
```

### 4.3 Budget Deletion

**Scenario:** User deletes a budget that has associated expenses.

**Handling:**
- Budget deletion is allowed even if expenses exist
- Expenses remain in database (not deleted)
- Budget status queries return "no budget" (0.0 limit) for that month
- Expenses are still tracked but not compared against any budget

**Implementation:**
```java
// Budget deletion
deleteBudget(budgetId, user);
// Expenses remain untouched
// Future getBudgetStatus() will show no budget for that month
```

### 4.4 Multiple Budgets for Same Category

**Scenario:** User tries to create two budgets for "Food" category in November.

**Handling:**
- Unique constraint `(user, year, month, category)` prevents duplicates
- Second create/update operation updates the existing budget instead of creating duplicate

**Implementation:**
```java
@CompoundIndex(name = "user_year_month_category_idx", 
                def = "{'user': 1, 'year': 1, 'month': 1, 'category': 1}", 
                unique = true)
```

### 4.5 Missing Budget

**Scenario:** User adds expense but hasn't set a budget for that month.

**Handling:**
- Expense is allowed (no validation failure)
- Budget status shows `overallBudget: 0.0`, `status: SAFE`
- No alerts generated
- Expenses are still tracked

**Implementation:**
```java
Optional<Budget> overallBudget = findByUserAndYearAndMonthAndCategoryIsNull(...);
if (overallBudget.isPresent()) {
    // Validate against budget
} else {
    // No budget set - allow expense, show 0.0 in status
}
```

### 4.6 Category Without Budget

**Scenario:** User adds expense to "Entertainment" category, but only has budget for "Food".

**Handling:**
- Expense is allowed (no category budget to validate against)
- Category budget status shows `budget: 0.0` for "Entertainment"
- Overall budget (if set) still applies

**Implementation:**
```java
Optional<Budget> categoryBudget = findByUserAndYearAndMonthAndCategory(...);
if (categoryBudget.isPresent()) {
    // Validate against category budget
} else {
    // No category budget - skip category validation
}
```

### 4.7 Budget Rollover (Future Enhancement)

**Scenario:** User has $200 remaining in November budget, sets `allowRollover: true`.

**Handling:**
- Currently not implemented, but schema supports it
- Future: December budget would start with $200 + December limit
- Requires tracking "previous month remaining" and adding to current month limit

**Implementation (Future):**
```java
if (budget.getAllowRollover()) {
    // Get previous month's remaining budget
    Budget prevMonth = findByUserAndYearAndMonth(user, prevYear, prevMonth);
    double rolloverAmount = prevMonth.getMonthlyLimit() - prevMonth.getSpent();
    // Add rollover to current month's limit
    effectiveLimit = budget.getMonthlyLimit() + rolloverAmount;
}
```

---

## 5. Sample API Flow

### 5.1 Setting Up Budgets

**Step 1: Create Overall Monthly Budget**
```http
POST /api/budgets
Authorization: Bearer {token}
Content-Type: application/json

{
  "categoryId": null,
  "monthlyLimit": 5000.00,
  "year": 2024,
  "month": 11,
  "allowRollover": false,
  "preventExceed": false
}

Response: 201 Created
{
  "id": "budget123",
  "user": {...},
  "category": null,
  "monthlyLimit": 5000.00,
  "year": 2024,
  "month": 11,
  "allowRollover": false,
  "preventExceed": false
}
```

**Step 2: Create Category Budget**
```http
POST /api/budgets
Authorization: Bearer {token}
Content-Type: application/json

{
  "categoryId": "cat_food_123",
  "monthlyLimit": 1000.00,
  "year": 2024,
  "month": 11,
  "allowRollover": false,
  "preventExceed": true
}

Response: 201 Created
{
  "id": "budget456",
  "user": {...},
  "category": {"id": "cat_food_123", "name": "Food"},
  "monthlyLimit": 1000.00,
  "year": 2024,
  "month": 11,
  "preventExceed": true
}
```

### 5.2 Adding Expenses

**Step 3: Add Expense (Within Budget)**
```http
POST /api/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 150.00,
  "type": "EXPENSE",
  "date": "2024-11-15",
  "category": {"id": "cat_food_123"},
  "note": "Grocery shopping"
}

Response: 200 OK
{
  "id": "txn789",
  "amount": 150.00,
  "type": "EXPENSE",
  "date": "2024-11-15",
  "category": {...},
  "note": "Grocery shopping"
}
```

**Step 4: Add Expense (Exceeds Category Budget)**
```http
POST /api/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 1200.00,
  "type": "EXPENSE",
  "date": "2024-11-20",
  "category": {"id": "cat_food_123"},
  "note": "Expensive restaurant"
}

Response: 400 Bad Request
{
  "message": "Expense would exceed category budget: Food"
}
```

**Step 5: Add Expense (Exceeds Overall Budget, preventExceed=false)**
```http
POST /api/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 6000.00,
  "type": "EXPENSE",
  "date": "2024-11-25",
  "category": {"id": "cat_other_456"},
  "note": "Large purchase"
}

Response: 200 OK (with warning in logs)
{
  "id": "txn999",
  ...
}
```

### 5.3 Checking Budget Status

**Step 6: Get Budget Status**
```http
GET /api/budgets/status?year=2024&month=11
Authorization: Bearer {token}

Response: 200 OK
{
  "overallStatus": "WARNING",
  "overallBudget": 5000.00,
  "overallSpent": 3500.00,
  "overallRemaining": 1500.00,
  "overallUsagePercentage": 70.0,
  "categoryBudgets": [
    {
      "categoryId": "cat_food_123",
      "categoryName": "Food",
      "budget": 1000.00,
      "spent": 850.00,
      "remaining": 150.00,
      "usagePercentage": 85.0,
      "status": "WARNING"
    }
  ],
  "alerts": [
    {
      "type": "OVERALL",
      "message": "Budget is 70.0% used (3500.00 / 5000.00).",
      "severity": "WARNING",
      "threshold": 75.0
    },
    {
      "type": "cat_food_123",
      "message": "Budget is 85.0% used (850.00 / 1000.00).",
      "severity": "WARNING",
      "threshold": 75.0
    }
  ]
}
```

### 5.4 Monthly Summary

**Step 7: Get Monthly Summary**
```http
GET /api/budgets/monthly-summary?year=2024&month=11
Authorization: Bearer {token}

Response: 200 OK
{
  "year": 2024,
  "month": 11,
  "totalIncome": 8000.00,
  "totalExpenses": 3500.00,
  "savings": 4500.00,
  "savingsPercentage": 56.25,
  "budgetStatus": {
    "overallStatus": "WARNING",
    "overallBudget": 5000.00,
    "overallSpent": 3500.00,
    "overallRemaining": 1500.00,
    "overallUsagePercentage": 70.0,
    ...
  },
  "categoryExpenses": [
    {
      "categoryId": "cat_food_123",
      "categoryName": "Food",
      "amount": 850.00,
      "percentage": 24.29
    },
    {
      "categoryId": "cat_rent_789",
      "categoryName": "Rent",
      "amount": 2000.00,
      "percentage": 57.14
    }
  ]
}
```

### 5.5 Updating Budget

**Step 8: Update Budget Mid-Month**
```http
PUT /api/budgets/budget123
Authorization: Bearer {token}
Content-Type: application/json

{
  "categoryId": null,
  "monthlyLimit": 4000.00,
  "year": 2024,
  "month": 11,
  "preventExceed": true
}

Response: 200 OK
{
  "id": "budget123",
  "monthlyLimit": 4000.00,
  "preventExceed": true,
  "updatedAt": "2024-11-20T10:00:00Z"
}
```

---

## 6. Multi-User Support

### 6.1 User Isolation

**Implementation:**
- All queries filter by `user` field
- Budgets are scoped: `(user, year, month, category)`
- Transactions are scoped: `(user, date)`
- Users cannot access other users' budgets or transactions

**Example:**
```java
// User A's budgets
List<Budget> userABudgets = budgetRepository.findByUser(userA);

// User B's budgets (separate)
List<Budget> userBBudgets = budgetRepository.findByUser(userB);
```

### 6.2 Authentication & Authorization

- All endpoints require JWT authentication
- User is extracted from `Authentication` principal
- Budget operations automatically scoped to authenticated user

---

## 7. Performance Considerations

### 7.1 Query Optimization

**Indexes:**
- Compound index on `(user, year, month, category)` for fast budget lookups
- Index on `(user, type, date)` for expense queries
- Index on `(user, date)` for date range queries

**Query Patterns:**
- Budget status calculation: 1 query for budgets + 1 query for expenses
- Category breakdown: Single aggregation query
- Monthly summary: Single date range query

### 7.2 Caching (Future Enhancement)

- Cache budget status for current month (TTL: 5 minutes)
- Invalidate cache on transaction create/update/delete
- Cache monthly summaries (TTL: 1 hour)

---

## 8. Scalability

### 8.1 Data Volume

**Assumptions:**
- Average user: 50 transactions/month, 10 categories, 5 budgets/month
- 1M users: ~50M transactions/year, ~10M budgets/year

**MongoDB Sharding:**
- Shard by `user._id` (ensures user data co-located)
- Replica sets for read scaling

### 8.2 Real-Time Updates

- Budget status calculated on-demand (no pre-computation)
- Transaction creation triggers immediate budget validation
- Status queries are fast (< 100ms) with proper indexes

---

## 9. Testing Scenarios

### 9.1 Unit Tests

- Budget calculation logic
- Status determination (SAFE, WARNING, EXCEEDED)
- Alert generation at thresholds
- Budget validation rules

### 9.2 Integration Tests

- Create budget → Add expense → Check status
- Update budget mid-month → Verify recalculation
- Delete budget → Verify expenses still tracked
- Multi-user isolation

### 9.3 Edge Case Tests

- Missing budget scenarios
- Month boundary crossing
- Budget update with existing expenses
- Category without budget

---

## 10. Future Enhancements

1. **Budget Rollover:** Implement carry-forward of unused budget
2. **Budget Templates:** Pre-defined budget templates (e.g., "Student Budget")
3. **Budget Forecasting:** Predict future spending based on trends
4. **Recurring Budgets:** Auto-create budgets for each month
5. **Budget Sharing:** Share budgets with family members
6. **Budget Goals:** Set savings goals alongside spending budgets
7. **Budget Notifications:** Push notifications for budget alerts
8. **Budget Analytics:** Historical budget performance charts

---

## Summary

The budget management system provides:

✅ **Overall & Category Budgets** - Flexible budget structure  
✅ **Real-Time Tracking** - Expenses calculated on-demand  
✅ **Budget Validation** - Prevent or warn on budget exceed  
✅ **Status & Alerts** - SAFE, WARNING, EXCEEDED with threshold alerts  
✅ **Multi-User Support** - Complete user isolation  
✅ **Monthly Analytics** - Income, expenses, savings, category breakdown  
✅ **Edge Case Handling** - Month changes, budget updates, deletions  
✅ **Scalable Design** - MongoDB indexes, efficient queries  

The system is production-ready and handles real-world financial tracking scenarios with correctness and scalability.

