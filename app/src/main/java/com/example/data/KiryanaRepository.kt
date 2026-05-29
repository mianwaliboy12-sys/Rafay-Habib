package com.example.data

import kotlinx.coroutines.flow.Flow
import androidx.room.withTransaction

data class CartItem(
    val product: Product,
    val quantity: Int
)

class KiryanaRepository(private val db: AppDatabase) {
    private val productDao = db.productDao()
    private val customerDao = db.customerDao()
    private val transactionDao = db.transactionDao()
    private val expenseDao = db.expenseDao()

    // Flows
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allTransactions: Flow<List<LedgerTransaction>> = transactionDao.getAllTransactions()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    // Products
    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    suspend fun getProductByBarcode(barcode: String): Product? = productDao.getProductByBarcode(barcode)
    suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)

    // Customers
    suspend fun insertCustomer(customer: Customer) = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)
    fun getCustomerById(id: Int): Flow<Customer?> = customerDao.getCustomerById(id)
    suspend fun getCustomerByIdDirect(id: Int): Customer? = customerDao.getCustomerByIdDirect(id)

    // Transactions
    fun getTransactionsForCustomer(customerId: Int): Flow<List<LedgerTransaction>> =
        transactionDao.getTransactionsForCustomer(customerId)

    suspend fun deleteTransaction(transactionId: Int) = transactionDao.deleteTransactionById(transactionId)

    // Checkout Sales Order
    suspend fun checkoutPOS(
        cart: List<CartItem>,
        isCredit: Boolean,
        customerId: Int?,
        totalAmount: Double,
        costOfGoods: Double,
        note: String
    ) {
        db.withTransaction {
            // 1. Create and insert ledger transaction
            val itemsSummary = cart.joinToString(", ") { "${it.quantity}x ${it.product.name}" }
            val transactionType = if (isCredit && customerId != null) "UDHAAR_DEBIT" else "CASH_REVENUE"
            
            val trx = LedgerTransaction(
                customerId = customerId,
                type = transactionType,
                amount = totalAmount,
                costOfGoods = costOfGoods,
                itemsSummary = itemsSummary,
                note = note
            )
            transactionDao.insertTransaction(trx)

            // 2. Adjust inventories
            for (item in cart) {
                val updatedStock = (item.product.stockQuantity - item.quantity).coerceAtLeast(0)
                productDao.updateStock(item.product.id, updatedStock)
            }

            // 3. Update customer udhaar balance if credit
            if (isCredit && customerId != null) {
                customerDao.updateUdhaarBalance(customerId, totalAmount)
            }
        }
    }

    // Customer Payment received towards Udhaar
    suspend fun recordCustomerPayment(
        customerId: Int,
        amount: Double,
        note: String
    ) {
        db.withTransaction {
            // Create payment received transaction (UDHAAR_CREDIT)
            val trx = LedgerTransaction(
                customerId = customerId,
                type = "UDHAAR_CREDIT",
                amount = amount,
                costOfGoods = 0.0,
                itemsSummary = "Received Udhaar Payment / ادائیگی موصول ہوئی",
                note = note
            )
            transactionDao.insertTransaction(trx)

            // Deduct the paid balance from udhaar (so update balance by negative amount)
            customerDao.updateUdhaarBalance(customerId, -amount)
        }
    }

    // Expenses
    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    suspend fun deleteExpense(expenseId: Int) = expenseDao.deleteExpenseById(expenseId)
}
