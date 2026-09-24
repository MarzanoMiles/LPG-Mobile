package com.example.gastrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gastrack.ui.presentation.*
import com.example.gastrack.ui.theme.GasTrackTheme
import com.example.gastrack.ui.presentation.CheckoutScreen

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

    NavHost(
        navController = navController,
        startDestination = "startup"
    ) {
        composable("startup") {
            _root_ide_package_.com.example.gastrack.ui.presentation.StartUpScreen(
                onNavigateToCustomer = {
                    navController.navigate("login_customer")
                }
            ) {
                navController.navigate("login_employee")
            }
        }
        composable("login_employee") {
            _root_ide_package_.com.example.gastrack.ui.presentation.LogInEmployeeScreen(
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
                }
            )
        }
        composable("create_account_employee") {
            _root_ide_package_.com.example.gastrack.ui.presentation.CreateAccountEmployeeScreen(
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
            _root_ide_package_.com.example.gastrack.ui.presentation.DashboardEmployeeScreen(
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
            _root_ide_package_.com.example.gastrack.ui.presentation.InventoryEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToAdjustment = {
                    navController.navigate("employee_manage_stock")
                }
            )
        }
        composable("employee_pos") {
            _root_ide_package_.com.example.gastrack.ui.presentation.POSEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_sales") {
            _root_ide_package_.com.example.gastrack.ui.presentation.SalesEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_products") {
            _root_ide_package_.com.example.gastrack.ui.presentation.ProductsEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_suppliers") {
            _root_ide_package_.com.example.gastrack.ui.presentation.SuppliersEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_data") {
            _root_ide_package_.com.example.gastrack.ui.presentation.DataModuleEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_reports") {
            _root_ide_package_.com.example.gastrack.ui.presentation.ReportsComplianceEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_users") {
            _root_ide_package_.com.example.gastrack.ui.presentation.UsersEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_restocking") {
            _root_ide_package_.com.example.gastrack.ui.presentation.RestockingEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_manage_stock") {
            _root_ide_package_.com.example.gastrack.ui.presentation.ManageStockScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("employee_orders") {
            _root_ide_package_.com.example.gastrack.ui.presentation.OrdersEmployeeScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToMaps = { _ ->
                    navController.navigate("map_tracking")
                }
            )
        }
        composable("employee_menu") {
            _root_ide_package_.com.example.gastrack.ui.presentation.MenuEmployeeScreen(
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
                    navController.navigate("startup") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("login_customer") {
            _root_ide_package_.com.example.gastrack.ui.presentation.LogInCustomerScreen(
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
                }
            )
        }
        composable("create_account_customer") {
            _root_ide_package_.com.example.gastrack.ui.presentation.CreateAccountCustomerScreen(
                onBack = {
                    navController.popBackStack()
                },
                onAccountCreated = {
                    navController.navigate("customer_home") {
                        popUpTo("startup") { inclusive = true }
                    }
                }
            )
        }
        composable("customer_home") {
            _root_ide_package_.com.example.gastrack.ui.presentation.CustomerHomeScreen(
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
            _root_ide_package_.com.example.gastrack.ui.presentation.NotificationScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("menu") {
            _root_ide_package_.com.example.gastrack.ui.presentation.MenuScreen(
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
            _root_ide_package_.com.example.gastrack.ui.presentation.ARLoadingScreen(
                onLoadingComplete = {
                    navController.navigate("ar_choose") {
                        popUpTo("ar_loading") { inclusive = true }
                    }
                }
            )
        }
        composable("ar_choose") {
            _root_ide_package_.com.example.gastrack.ui.presentation.ARChooseScreen(
                onContinue = {
                    navController.navigate("ar_scan")
                }
            )
        }
        composable("ar_scan") {
            _root_ide_package_.com.example.gastrack.ui.presentation.ARScanScreen(
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
                }
            )
        }
        composable("cart") {
            _root_ide_package_.com.example.gastrack.ui.presentation.CartScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToCheckout = {
                    navController.navigate("checkout")
                },
                onContinueShopping = {
                    navController.popBackStack()
                }
            )
        }
        composable("checkout") {
            CheckoutScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPlaceOrder = {
                    // Handle Order Placement
                },
                onNavigateToTracking = {
                    navController.navigate("map_tracking")
                },
                onNavigateToAddresses = {
                    navController.navigate("addresses")
                }
            )
        }
        composable("map_tracking") {
            _root_ide_package_.com.example.gastrack.ui.presentation.MapTrackingScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("profile") {
            _root_ide_package_.com.example.gastrack.ui.presentation.ProfileScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("order_history") {
            _root_ide_package_.com.example.gastrack.ui.presentation.OrderHistoryScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("addresses") {
            _root_ide_package_.com.example.gastrack.ui.presentation.AddressesScreen(
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
            _root_ide_package_.com.example.gastrack.ui.presentation.HelpCenterScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("about") {
            _root_ide_package_.com.example.gastrack.ui.presentation.AboutScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("privacy_policy") {
            _root_ide_package_.com.example.gastrack.ui.presentation.PrivacyPolicyScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
