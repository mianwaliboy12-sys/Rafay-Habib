package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class KiryanaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val repository = KiryanaRepository(db)

    // Lang and Role states
    val isUrdu = MutableStateFlow(true) // Default to Urdu for Kiryana store friendliness
    val isAdmin = MutableStateFlow(true) // Admin/Cashier toggle
    val currentTab = MutableStateFlow("DASHBOARD") // DASHBOARD, INVENTORY, POS, LEDGER, EXPENSES

    // Flow Lists
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<LedgerTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filters
    val posSearchQuery = MutableStateFlow("")
    val inventorySearchQuery = MutableStateFlow("")
    val ledgerSearchQuery = MutableStateFlow("")
    val expensesSearchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("All")

    // POS Active State
    val cart = MutableStateFlow<List<CartItem>>(emptyList())
    val discount = MutableStateFlow(0.0) // Rupees
    val selectedCustomerForSale = MutableStateFlow<Customer?>(null)
    val checkoutNote = MutableStateFlow("")
    val isSaleCredit = MutableStateFlow(false)

    // Selected items for detail dialogs
    val selectedCustomerForLedger = MutableStateFlow<Customer?>(null)

    // UI Feedback messages
    val uiMessage = MutableStateFlow<String?>(null)

    // Filtered Products
    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        inventorySearchQuery,
        selectedCategoryFilter
    ) { prodList, query, cat ->
        prodList.filter {
            (cat == "All" || it.category == cat) &&
            (query.isEmpty() || it.name.contains(query, ignoreCase = true) || it.barcode.contains(query))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Customers
    val filteredCustomers: StateFlow<List<Customer>> = combine(
        customers,
        ledgerSearchQuery
    ) { custList, query ->
        custList.filter {
            query.isEmpty() || it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Available Categories
    val categories: StateFlow<List<String>> = products.map { list ->
        val cats = list.map { it.category }.distinct().toMutableList()
        if (cats.isEmpty()) {
            cats.addAll(listOf("General", "Grocery", "Beverages", "Spices", "Ghee / Oil", "Bakery"))
        }
        cats.sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("General", "Grocery", "Beverages", "Spices", "Ghee / Oil", "Bakery"))

    // Active screen functions
    fun toggleLanguage() {
        isUrdu.value = !isUrdu.value
    }

    fun toggleRole() {
        isAdmin.value = !isAdmin.value
    }

    fun selectTab(tab: String) {
        currentTab.value = tab
    }

    fun showMessage(msg: String) {
        uiMessage.value = msg
    }

    fun clearMessage() {
        uiMessage.value = null
    }

    // --- POS Cart Handling ---
    fun addProductToCart(product: Product) {
        val currentCart = cart.value.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.product.id == product.id }
        
        if (existingIndex != -1) {
            val existingItem = currentCart[existingIndex]
            if (existingItem.quantity < product.stockQuantity) {
                currentCart[existingIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
                cart.value = currentCart
            } else {
                showMessage(Lang.get("out_of_stock", isUrdu.value))
            }
        } else {
            if (product.stockQuantity > 0) {
                currentCart.add(CartItem(product, 1))
                cart.value = currentCart
            } else {
                showMessage(Lang.get("out_of_stock", isUrdu.value))
            }
        }
    }

    fun removeProductFromCart(product: Product) {
        val currentCart = cart.value.toMutableList()
        val index = currentCart.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val item = currentCart[index]
            if (item.quantity > 1) {
                currentCart[index] = item.copy(quantity = item.quantity - 1)
            } else {
                currentCart.removeAt(index)
            }
            cart.value = currentCart
        }
    }

    fun clearCart() {
        cart.value = emptyList()
        discount.value = 0.0
        selectedCustomerForSale.value = null
        checkoutNote.value = ""
        isSaleCredit.value = false
    }

    // --- Barcode Scanner Search / Action ---
    fun handleBarcodeScan(scannedCode: String) {
        viewModelScope.launch {
            val code = scannedCode.trim()
            if (code.isEmpty()) return@launch

            val matchedProduct = repository.getProductByBarcode(code)
            if (matchedProduct != null) {
                if (currentTab.value == "POS") {
                    addProductToCart(matchedProduct)
                    showMessage("+ ${matchedProduct.name}")
                } else if (currentTab.value == "INVENTORY") {
                    inventorySearchQuery.value = code
                }
            } else {
                showMessage("${Lang.get("invalid_barcode", isUrdu.value)}: $code")
            }
        }
    }

    // POS Checkout
    fun performPOSCheckout() {
        val activeCart = cart.value
        if (activeCart.isEmpty()) {
            showMessage(Lang.get("cart_empty", isUrdu.value))
            return
        }

        val subtotal = activeCart.sumOf { it.product.sellingPrice * it.quantity }
        val finalAmount = (subtotal - discount.value).coerceAtLeast(0.0)
        val costOfGoods = activeCart.sumOf { it.product.purchasePrice * it.quantity }

        // Rules verification
        if (isSaleCredit.value && selectedCustomerForSale.value == null) {
            showMessage(if (isUrdu.value) "برائے مہربانی ادھار کھاتہ دار منتخب کریں" else "Please select a ledger customer for credit sale")
            return
        }

        viewModelScope.launch {
            try {
                repository.checkoutPOS(
                    cart = activeCart,
                    isCredit = isSaleCredit.value,
                    customerId = selectedCustomerForSale.value?.id,
                    totalAmount = finalAmount,
                    costOfGoods = costOfGoods,
                    note = checkoutNote.value
                )
                showMessage(Lang.get("success_sale", isUrdu.value))
                clearCart()
            } catch (e: Exception) {
                showMessage("Checkout Error: ${e.localizedMessage}")
            }
        }
    }

    // --- Product Inventory CRUD ---
    fun saveProduct(
        id: Int,
        barcode: String,
        name: String,
        category: String,
        purchasePrice: Double,
        sellingPrice: Double,
        stockQuantity: Int,
        lowStockThreshold: Int
    ) {
        viewModelScope.launch {
            val prod = Product(
                id = if (id == 0) 0 else id,
                barcode = barcode.trim().ifEmpty { System.currentTimeMillis().toString().takeLast(6) },
                name = name.trim(),
                category = category.trim().ifEmpty { "General" },
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                stockQuantity = stockQuantity,
                lowStockThreshold = lowStockThreshold
            )
            repository.insertProduct(prod)
            showMessage(Lang.get("success_product", isUrdu.value))
        }
    }

    fun deleteProduct(prod: Product) {
        if (!isAdmin.value) {
            showMessage("Only owner can delete items!")
            return
        }
        viewModelScope.launch {
            repository.deleteProduct(prod)
            showMessage(if (isUrdu.value) "آئٹم خارج کر دیا گیا" else "Product removed")
        }
    }

    // --- Customer Ledger CRUD ---
    fun saveCustomer(id: Int, name: String, phone: String, address: String) {
        viewModelScope.launch {
            val cust = Customer(
                id = id,
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim()
            )
            repository.insertCustomer(cust)
            showMessage(Lang.get("success_customer", isUrdu.value))
        }
    }

    fun deleteCustomer(cust: Customer) {
        if (!isAdmin.value) {
            showMessage("Only admin can delete customer profiles!")
            return
        }
        viewModelScope.launch {
            repository.deleteCustomer(cust)
            showMessage(if (isUrdu.value) "گاہک کو حذف کر دیا گیا" else "Customer deleted")
        }
    }

    fun recordPayment(customerId: Int, amount: Double, note: String) {
        viewModelScope.launch {
            repository.recordCustomerPayment(customerId, amount, note)
            showMessage(Lang.get("success_payment", isUrdu.value))
        }
    }

    // --- Expense Tracking ---
    fun saveExpense(category: String, amount: Double, desc: String) {
        viewModelScope.launch {
            val expense = Expense(
                category = category,
                amount = amount,
                description = desc.trim()
            )
            repository.insertExpense(expense)
            showMessage(Lang.get("success_expense", isUrdu.value))
        }
    }

    fun deleteExpense(id: Int) {
        if (!isAdmin.value) {
            showMessage("Only Admin can delete expenses!")
            return
        }
        viewModelScope.launch {
            repository.deleteExpense(id)
            showMessage(if (isUrdu.value) "خرچہ حذف کر دیا گیا" else "Expense deleted")
        }
    }

    // --- Backup & Restore Logic (Manual text based JSON string) ---
    fun exportBackupJson(): String {
        val root = JSONObject()
        val prods = JSONArray()
        products.value.forEach {
            prods.put(JSONObject().apply {
                put("barcode", it.barcode)
                put("name", it.name)
                put("category", it.category)
                put("purchasePrice", it.purchasePrice)
                put("sellingPrice", it.sellingPrice)
                put("stockQuantity", it.stockQuantity)
                put("lowStockThreshold", it.lowStockThreshold)
            })
        }
        root.put("products", prods)

        val custs = JSONArray()
        customers.value.forEach {
            custs.put(JSONObject().apply {
                put("name", it.name)
                put("phone", it.phone)
                put("address", it.address)
                put("totalUdhaar", it.totalUdhaar)
            })
        }
        root.put("customers", custs)

        val trxs = JSONArray()
        transactions.value.forEach {
            trxs.put(JSONObject().apply {
                put("customerId", it.customerId ?: -1)
                put("type", it.type)
                put("amount", it.amount)
                put("costOfGoods", it.costOfGoods)
                put("timestamp", it.timestamp)
                put("itemsSummary", it.itemsSummary)
                put("note", it.note)
            })
        }
        root.put("transactions", trxs)

        val exps = JSONArray()
        expenses.value.forEach {
            exps.put(JSONObject().apply {
                put("category", it.category)
                put("description", it.description)
                put("amount", it.amount)
                put("timestamp", it.timestamp)
            })
        }
        root.put("expenses", exps)

        return root.toString(2)
    }

    fun importRestoreJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            viewModelScope.launch {
                db.withTransaction {
                    // Clear tables
                    db.clearAllTables()

                    // Parse & Insert Products
                    val prods = root.optJSONArray("products")
                    if (prods != null) {
                        for (i in 0 until prods.length()) {
                            val o = prods.getJSONObject(i)
                            repository.insertProduct(
                                Product(
                                    barcode = o.optString("barcode"),
                                    name = o.optString("name"),
                                    category = o.optString("category"),
                                    purchasePrice = o.optDouble("purchasePrice"),
                                    sellingPrice = o.optDouble("sellingPrice"),
                                    stockQuantity = o.optInt("stockQuantity"),
                                    lowStockThreshold = o.optInt("lowStockThreshold", 5)
                                )
                            )
                        }
                    }

                    // Parse & Insert Customers
                    val custs = root.optJSONArray("customers")
                    if (custs != null) {
                        for (i in 0 until custs.length()) {
                            val o = custs.getJSONObject(i)
                            repository.insertCustomer(
                                Customer(
                                    name = o.optString("name"),
                                    phone = o.optString("phone"),
                                    address = o.optString("address"),
                                    totalUdhaar = o.optDouble("totalUdhaar")
                                )
                            )
                        }
                    }

                    // Parse & Insert Transactions
                    val trxs = root.optJSONArray("transactions")
                    if (trxs != null) {
                        for (i in 0 until trxs.length()) {
                            val o = trxs.getJSONObject(i)
                            val rawCustId = o.optInt("customerId", -1)
                            repository.repositoryInsertDirectTransaction(
                                LedgerTransaction(
                                    customerId = if (rawCustId == -1) null else rawCustId,
                                    type = o.optString("type"),
                                    amount = o.optDouble("amount"),
                                    costOfGoods = o.optDouble("costOfGoods"),
                                    timestamp = o.optLong("timestamp"),
                                    itemsSummary = o.optString("itemsSummary"),
                                    note = o.optString("note")
                                )
                            )
                        }
                    }

                    // Parse & Insert Expenses
                    val exps = root.optJSONArray("expenses")
                    if (exps != null) {
                        for (i in 0 until exps.length()) {
                            val o = exps.getJSONObject(i)
                            repository.insertExpense(
                                Expense(
                                    category = o.optString("category"),
                                    description = o.optString("description"),
                                    amount = o.optDouble("amount"),
                                    timestamp = o.optLong("timestamp")
                                )
                            )
                        }
                    }
                }
                showMessage(if (isUrdu.value) "ڈیٹا کامیابی سے بحال ہو گیا!" else "Data backup restored successfully!")
            }
            true
        } catch (e: Exception) {
            showMessage("Failed to restore: ${e.localizedMessage}")
            false
        }
    }
}

// Helper addition inside database access for explicit restore inserts with historic values preserved
suspend fun KiryanaRepository.repositoryInsertDirectTransaction(trx: LedgerTransaction) {
    // Room custom shortcut bypasses checkout logic to restore exact timestamp histories
    this.allTransactions // accessing flow triggers initialization if required
    val dbField = this.javaClass.getDeclaredField("transactionDao")
    dbField.isAccessible = true
    val dao = dbField.get(this) as TransactionDao
    dao.insertTransaction(trx)
}
