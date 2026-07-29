package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.StudentEntity
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WaqarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var darkTheme by remember { mutableStateOf(false) }

            WaqarCoachingTheme(darkTheme = darkTheme) {
                MainAppStructure(
                    viewModel = viewModel,
                    darkTheme = darkTheme,
                    onToggleDarkTheme = { darkTheme = !darkTheme }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure(
    viewModel: WaqarViewModel,
    darkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val toastMsg by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current
    val notifications by viewModel.allNotifications.collectAsState()
    val students by viewModel.allStudentsRaw.collectAsState()

    val unreadNotifCount = notifications.count { !it.isRead }

    var showAddStudentModal by remember { mutableStateOf(false) }
    var showRecordFeeModal by remember { mutableStateOf(false) }

    // Display toasts when requested
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.Students,
        Screen.FeeManagement,
        Screen.Attendance,
        Screen.Results
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_coaching_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Waqar Coaching",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (currentRoute) {
                                    Screen.Dashboard.route -> "Dashboard & Analytics"
                                    Screen.Students.route -> "Student Directory"
                                    Screen.FeeManagement.route -> "Fee Processing"
                                    Screen.Attendance.route -> "Attendance System"
                                    Screen.Results.route -> "Results & Marks"
                                    Screen.Notifications.route -> "System Alerts"
                                    Screen.Reports.route -> "Financial Reports"
                                    Screen.Admin.route -> "Admin Settings"
                                    else -> "Institute Portal"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = RoyalBluePrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    // Dark Mode Toggle
                    IconButton(onClick = onToggleDarkTheme) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }

                    // Alerts Bell with Badge
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge { Text("$unreadNotifCount") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentRoute == Screen.Notifications.route) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Alerts"
                            )
                        }
                    }

                    // Reports Button
                    IconButton(onClick = { navController.navigate(Screen.Reports.route) }) {
                        Icon(
                            imageVector = Icons.Outlined.Assessment,
                            contentDescription = "Reports",
                            tint = if (currentRoute == Screen.Reports.route) RoyalBluePrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Admin Button
                    IconButton(onClick = { navController.navigate(Screen.Admin.route) }) {
                        Icon(
                            imageVector = Icons.Outlined.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = if (currentRoute == Screen.Admin.route) RoyalBluePrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.filledIcon else screen.outlinedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RoyalBluePrimary,
                            selectedTextColor = RoyalBluePrimary,
                            indicatorColor = BlueContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateTo = { route -> navController.navigate(route) },
                    onOpenAddStudentDialog = { showAddStudentModal = true },
                    onOpenRecordFeeDialog = { showRecordFeeModal = true }
                )
            }

            composable(Screen.Students.route) {
                StudentManagementScreen(
                    viewModel = viewModel,
                    onOpenAddStudentDialog = { showAddStudentModal = true }
                )
            }

            composable(Screen.FeeManagement.route) {
                FeeManagementScreen(
                    viewModel = viewModel,
                    onOpenRecordFeeDialog = { showRecordFeeModal = true }
                )
            }

            composable(Screen.Attendance.route) {
                AttendanceScreen(viewModel = viewModel)
            }

            composable(Screen.Results.route) {
                ResultsMarksScreen(viewModel = viewModel)
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen(viewModel = viewModel)
            }

            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = viewModel)
            }

            composable(Screen.Admin.route) {
                AdminPanelScreen(viewModel = viewModel)
            }
        }
    }

    // Global Add Student Dialog Modal
    if (showAddStudentModal) {
        AddEditStudentDialog(
            studentToEdit = null,
            onDismiss = { showAddStudentModal = false },
            onSave = { name, className, courseName, parentName, address, mobile, monthlyFee, isActive ->
                viewModel.addOrUpdateStudent(
                    name = name,
                    className = className,
                    courseName = courseName,
                    parentName = parentName,
                    address = address,
                    mobileNumber = mobile,
                    monthlyFee = monthlyFee,
                    isActive = isActive
                )
                showAddStudentModal = false
            }
        )
    }

    // Global Record Fee Payment Dialog Modal
    if (showRecordFeeModal) {
        RecordFeePaymentDialog(
            students = students,
            onDismiss = { showRecordFeeModal = false },
            onSave = { studentId, amountPaid, dueAmount, paymentMode, month, notes ->
                viewModel.recordFeePayment(studentId, amountPaid, dueAmount, paymentMode, month, notes)
                showRecordFeeModal = false
            }
        )
    }
}
