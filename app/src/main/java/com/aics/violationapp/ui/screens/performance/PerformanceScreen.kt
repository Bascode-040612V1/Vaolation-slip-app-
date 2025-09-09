package com.aics.violationapp.ui.screens.performance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aics.violationapp.data.local.analytics.PerformanceMonitor
import com.aics.violationapp.data.local.analytics.PerformanceMetrics
import com.aics.violationapp.data.network.NetworkModule
import com.aics.violationapp.utils.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(navController: NavController) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val performanceMonitor = remember { PerformanceMonitor(context) }
    val repository = remember { NetworkModule.provideRepository(context, preferencesManager.getBaseUrl()) }
    
    var metrics by remember { mutableStateOf<PerformanceMetrics?>(null) }
    var syncStats by remember { mutableStateOf<com.aics.violationapp.data.local.sync.SyncStats?>(null) }
    var cacheStats by remember { mutableStateOf<com.aics.violationapp.data.local.cache.CacheStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val scope = rememberCoroutineScope()
    
    // Load data
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                metrics = performanceMonitor.getPerformanceMetrics()
                syncStats = repository.getSyncStats()
                cacheStats = repository.getCacheStats()
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }
    
    val refreshData: () -> Unit = {
        scope.launch {
            isLoading = true
            try {
                metrics = performanceMonitor.getPerformanceMetrics()
                syncStats = repository.getSyncStats()
                cacheStats = repository.getCacheStats()
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Monitor") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = refreshData) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Performance Metrics Card
                item {
                    metrics?.let { metrics ->
                        PerformanceMetricsCard(metrics)
                    }
                }
                
                // Cache Statistics Card
                item {
                    cacheStats?.let { stats ->
                        CacheStatsCard(stats)
                    }
                }
                
                // Sync Statistics Card
                item {
                    syncStats?.let { stats ->
                        SyncStatsCard(stats)
                    }
                }
                
                // Network Savings Card
                item {
                    metrics?.let { metrics ->
                        NetworkSavingsCard(metrics)
                    }
                }
                
                // Actions Card
                item {
                    ActionsCard(
                        onResetMetrics = {
                            performanceMonitor.resetMetrics()
                            refreshData()
                        },
                        onStartSync = {
                            scope.launch {
                                repository.startSync()
                            }
                            refreshData()
                        },
                        onClearCache = {
                            scope.launch {
                                repository.clearCache()
                            }
                            refreshData()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceMetricsCard(metrics: PerformanceMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Performance Metrics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            MetricRow("Session Duration", "${metrics.sessionDuration / 1000}s")
            MetricRow("API Calls", "${metrics.apiCallCount}")
            MetricRow("Cache Hit Rate", "${"%.1f".format(metrics.cacheHitRate)}%")
            MetricRow("Avg API Time", "${metrics.averageApiTime}ms")
            MetricRow("Avg Cache Time", "${metrics.averageCacheTime}ms")
            MetricRow("Violations Submitted", "${metrics.violationSubmitCount}")
        }
    }
}

@Composable
fun CacheStatsCard(stats: com.aics.violationapp.data.local.cache.CacheStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Cache Statistics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            MetricRow("Violation Types Cached", "${stats.violationTypesCount}")
            MetricRow("Recent Students Cached", "${stats.recentStudentsCount}")
            MetricRow("Cache Size", "${stats.cacheSize / 1024}KB")
            
            stats.violationTypesLastSync?.let { lastSync ->
                val timeAgo = (System.currentTimeMillis() - lastSync) / 1000
                MetricRow("Last Sync", "${timeAgo}s ago")
            }
        }
    }
}

@Composable
fun SyncStatsCard(stats: com.aics.violationapp.data.local.sync.SyncStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sync Statistics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            MetricRow("Pending Violations", "${stats.pendingViolations}")
            MetricRow("Failed Violations", "${stats.failedViolations}")
            MetricRow("Completed Today", "${stats.completedToday}")
            MetricRow("Failed Today", "${stats.failedToday}")
            MetricRow("Is Syncing", if (stats.isSyncing) "Yes" else "No")
            
            stats.lastSyncTime?.let { lastSync ->
                val timeAgo = (System.currentTimeMillis() - lastSync) / 1000
                MetricRow("Last Sync", "${timeAgo}s ago")
            }
        }
    }
}

@Composable
fun NetworkSavingsCard(metrics: PerformanceMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Network Savings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            MetricRow("Saved Requests", "${metrics.networkSavings.savedRequests}")
            MetricRow("Saved Time", "${metrics.networkSavings.savedTimeMs / 1000}s")
            MetricRow("Data Saved", "${metrics.networkSavings.estimatedDataSavedBytes / 1024}KB")
        }
    }
}

@Composable
fun ActionsCard(
    onResetMetrics: () -> Unit,
    onStartSync: () -> Unit,
    onClearCache: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onResetMetrics,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset Metrics")
                }
                
                Button(
                    onClick = onStartSync,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Force Sync")
                }
                
                Button(
                    onClick = onClearCache,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear Cache")
                }
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}