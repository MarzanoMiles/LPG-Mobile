package com.example.gastrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gastrack.ui.presentation.*
import com.example.gastrack.ui.theme.GasTrackTheme
import com.example.gastrack.viewmodel.AuthViewModel
import com.example.gastrack.viewmodel.CartViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GasTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "startup"
    ) {
        composable("startup") {
            StartUpScreen(
                onNavigateToCustomer = {
                    navController.navigate("login_customer")
                }
            ) {
                navController.navigate("login_employee")
            }
        }
        composable("login_employee") {
            LogInEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToSignUp = {
                    navController.navigate("create_account_employee")
                },
                onNavigateToHome = {
                    navController.navigate("employee_menu") {
                        popUpTo("startup") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        composable("create_account_employee") {
            CreateAccountEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                },
                onActivateAccount = {
                    navController.navigate("employee_menu") {
                        popUpTo("startup") { inclusive = true }
                    }
                }
            )
        }
        composable("employee_dashboard") {
            DashboardEmployeeScreen(
                onNavigateToMenu = {
                    navController.navigate("employee_menu")
                },
                onNavigateToInventory = {
                    navController.navigate("employee_inventory")
                },
                onNavigateToRestocking = {
                    navController.navigate("employee_restocking")
                },
                onNavigateToNotifications = {
                    navController.navigate("notifications")
                }
            )
        }
        composable("employee_inventory") {
            InventoryEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToAdjustment = {
                    navController.navigate("employee_manage_stock")
                }
            )
        }
        composable("employee_pos") {
            POSEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_sales") {
            SalesEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_products") {
            ProductsEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_suppliers") {
            SuppliersEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_data") {
            DataModuleEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_reports") {
            ReportsComplianceEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_users") {
            UsersEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_restocking") {
            RestockingEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_manage_stock") {
            ManageStockScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_orders") {
            OrdersEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToMaps = { _ ->
                    navController.navigate("map_tracking")
                }
            )
        }
        composable("employee_menu") {
            MenuEmployeeScreen(
                onNavigateToDashboard = {
                    navController.navigate("employee_dashboard") {
                        popUpTo("employee_dashboard") { inclusive = true }
                    }
                },
                onNavigateToOrders = {
                    navController.navigate("employee_orders")
                },
                onNavigateToPOS = {
                    navController.navigate("employee_pos")
                },
                onNavigateToSales = {
                    navController.navigate("employee_sales")
                },
                onNavigateToInventory = {
                    navController.navigate("employee_inventory")
                },
                onNavigateToProducts = {
                    navController.navigate("employee_products")
                },
                onNavigateToSuppliers = {
                    navController.navigate("employee_suppliers")
                },
                onNavigateToData = {
                    navController.navigate("employee_data")
                },
                onNavigateToRestocking = {
                    navController.navigate("employee_restocking")
                },
                onNavigateToUsers = {
                    navController.navigate("employee_users")
                },
                onNavigateToReports = {
                    navController.navigate("employee_reports")
                },
                onNavigateToLogout = {
                    authViewModel.logout()
                    navController.navigate("startup") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("login_customer") {
            LogInCustomerScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToSignUp = {
                    navController.navigate("create_account_customer")
                },
                onNavigateToHome = {
                    navController.navigate("customer_home") {
                        popUpTo("startup") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        composable("create_account_customer") {
            CreateAccountCustomerScreen(
                onBack = {
                    navController.popBackStack()
                },
                onAccountCreated = {
                    navController.navigate("customer_home") {
                        popUpTo("startup") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        composable("customer_home") {
            CustomerHomeScreen(
                onNavigateToOrder = {
                    navController.navigate("order_tab")
                },
                onNavigateToAR = {
                    navController.navigate("ar_loading")
                },
                onNavigateToMenu = {
                    navController.navigate("menu")
                },
                onNavigateToCheckout = {
                    navController.navigate("checkout")
                },
                onNavigateToAddresses = {
                    navController.navigate("addresses")
                },
                onNavigateToNotifications = {
                    navController.navigate("notifications")
                }
            )
        }
        composable("notifications") {
            NotificationScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("menu") {
            MenuScreen(
                onNavigateToHome = {
                    navController.navigate("customer_home") {
                        popUpTo("customer_home") { inclusive = true }
                    }
                },
                onNavigateToOrder = {
                    navController.navigate("order_tab")
                },
                onNavigateToLogout = {
                    navController.navigate("logout_customer")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToHistory = {
                    navController.navigate("order_history")
                },
                onNavigateToAddresses = {
                    navController.navigate("addresses")
                },
                onNavigateToPaymentMethods = {
                    navController.navigate("payment_methods")
                },
                onNavigateToHelpCenter = {
                    navController.navigate("help_center")
                },
                onNavigateToAbout = {
                    navController.navigate("about")
                },
                onNavigateToPrivacyPolicy = {
                    navController.navigate("privacy_policy")
                }
            )
        }
        composable("logout_customer") {
            LogOutCustomerScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("startup") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
        composable("ar_loading") {
            ARLoadingScreen(
                onLoadingComplete = {
                    navController.navigate("ar_choose") {
                        popUpTo("ar_loading") { inclusive = true }
                    }
                }
            )
        }
        composable("ar_choose") {
            ARChooseScreen(
                onContinue = {
                    navController.navigate("ar_scan")
                }
            )
        }
        composable("ar_scan") {
            ARScanScreen(
                onScanComplete = {
                    navController.navigate("order_tab") {
                        popUpTo("customer_home")
                    }
                }
            )
        }
        composable("order_tab") {
            OrderTabScreen(
                onNavigateToHome = {
                    navController.navigate("customer_home") {
                        popUpTo("customer_home") { inclusive = true }
                    }
                },
                onNavigateToCheckout = {
                    navController.navigate("cart")
                },
                onNavigateToMenu = {
                    navController.navigate("menu")
                },
                cartViewModel = cartViewModel
            )
        }
        composable("cart") {
            CartScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToCheckout = {
                    navController.navigate("checkout")
                },
                onContinueShopping = {
                    navController.popBackStack()
                },
                cartViewModel = cartViewModel
            )
        }
        composable("checkout") {
            CheckoutScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPlaceOrder = {
                    // Handled inside CheckoutScreen via checkoutViewModel; cart is cleared there too.
                },
                onNavigateToTracking = {
                    navController.navigate("map_tracking")
                },
                onNavigateToAddresses = {
                    navController.navigate("addresses")
                },
                cartViewModel = cartViewModel
            )
        }
        composable("map_tracking") {
            MapTrackingScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("profile") {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("startup") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("order_history") {
            OrderHistoryScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("addresses") {
            AddressesScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("payment_methods") {
            PaymentMethodsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("help_center") {
            HelpCenterScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("about") {
            AboutScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}