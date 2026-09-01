package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamPrepScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = 7M & 5M Derivations, 1 = Formula Sheet, 2 = High-Yield Checklist, 3 = Examiner Rules

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Exam Preparation Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$selectedSubject • High-Yield Focus",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("7M & 5M Derivations") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Formula Sheet") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("1-Hr Revision") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Examiner Rules") })
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> { // 7M and 5M Derivations
                        item {
                            PrepCard(
                                marks = 7,
                                title = "Derivation of Air-Standard Otto Cycle Thermal Efficiency",
                                weightage = "Repeated 5x in University Exams",
                                steps = listOf(
                                    "State 4 standard air assumptions (ideal gas, reversible processes).",
                                    "Write heat supplied: Q_in = m·Cv·(T3 - T2).",
                                    "Write heat rejected: Q_out = m·Cv·(T4 - T1).",
                                    "Express efficiency: η = 1 - (T4 - T1) / (T3 - T2).",
                                    "Substitute isentropic relations: T2/T1 = T3/T4 = (V1/V2)^(γ-1) = r^(γ-1).",
                                    "Prove final equation: η_otto = 1 - 1 / (r^(γ - 1)).",
                                    "Sketch neat P-V and T-S diagram with clockwise state numbers 1-2-3-4."
                                )
                            )
                        }

                        item {
                            PrepCard(
                                marks = 7,
                                title = "Air-Standard Diesel Cycle Efficiency & Cut-off Ratio Derivation",
                                weightage = "Repeated 4x in University Exams",
                                steps = listOf(
                                    "Define Cut-off ratio rc = V3 / V2.",
                                    "Heat added at constant pressure: Q_in = m·Cp·(T3 - T2).",
                                    "Heat rejected at constant volume: Q_out = m·Cv·(T4 - T1).",
                                    "Express η = 1 - (1/γ) · [ (T4 - T1) / (T3 - T2) ].",
                                    "Substitute T2 = T1·r^(γ-1), T3 = T1·r^(γ-1)·rc, T4 = T1·rc^γ.",
                                    "Derive: η_diesel = 1 - (1/r^(γ-1)) · [ (rc^γ - 1) / (γ·(rc - 1)) ]."
                                )
                            )
                        }

                        item {
                            PrepCard(
                                marks = 5,
                                title = "Carnot Theorem & Maximum Theoretical Heat Engine Limit",
                                weightage = "Repeated 3x in University Exams",
                                steps = listOf(
                                    "State Carnot theorem accurately.",
                                    "Setup proof by contradiction with reversed engine.",
                                    "Derive η_carnot = (TH - TL) / TH = 1 - (TL / TH).",
                                    "Explain why all reversible engines have identical efficiency regardless of working substance."
                                )
                            )
                        }
                    }

                    1 -> { // Formula Sheet
                        item {
                            FormulaCard(
                                topic = "Air Standard Cycles (Thermodynamics)",
                                formulas = listOf(
                                    "Otto Efficiency" to "η_otto = 1 - 1 / (r^(γ - 1))",
                                    "Diesel Efficiency" to "η_diesel = 1 - (1/r^(γ-1)) * [ (rc^γ - 1) / (γ*(rc - 1)) ]",
                                    "Dual Cycle Efficiency" to "η_dual = 1 - (1/r^(γ-1)) * [ (rp*rc^γ - 1) / ((rp - 1) + γ*rp*(rc - 1)) ]",
                                    "Mean Effective Pressure" to "MEP = W_net / (V1 - V2) = W_net / V_swept",
                                    "Carnot Efficiency" to "η_carnot = 1 - (T_L / T_H)  [T in Kelvin]"
                                )
                            )
                        }

                        item {
                            FormulaCard(
                                topic = "Stress Analysis & Mohr's Circle (Machine Design)",
                                formulas = listOf(
                                    "Principal Stresses" to "σ1,2 = (σx + σy)/2 ± √[ ((σx - σy)/2)² + τxy² ]",
                                    "Maximum Shear Stress" to "τ_max = (σ1 - σ2) / 2 = Radius R",
                                    "Principal Angle" to "tan(2θp) = 2τxy / (σx - σy)",
                                    "Hydrostatic / Mean Stress" to "σ_avg = (σx + σy) / 2"
                                )
                            )
                        }
                    }

                    2 -> { // 1-Hour Revision Checklist
                        item {
                            ChecklistCard(
                                title = "1-Hour Rapid Exam Revision Checklist",
                                items = listOf(
                                    "Memorize Otto & Diesel efficiency equations with parameter definitions.",
                                    "Practice drawing P-V and T-S cycles freehand with arrows in under 90 seconds.",
                                    "Review temperature conversions: Kelvin K = °C + 273.15.",
                                    "Verify formula for compression ratio: r = (Vc + Vs) / Vc.",
                                    "Check 2-mark definitions: Cut-off ratio, MEP, Reversibility, Entropy."
                                )
                            )
                        }
                    }

                    3 -> { // Examiner Pitfalls & Rules
                        item {
                            PitfallCard(
                                title = "Top 5 Examiner Traps & Common Student Mistakes",
                                pitfalls = listOf(
                                    "Forgetting to convert Celsius to Kelvin: Carnot efficiency must ALWAYS use Kelvin (K).",
                                    "Missing arrowheads on P-V and T-S cycles: Deducts 1 to 2 marks immediately.",
                                    "Confusing Cp and Cv: Diesel combustion uses Cp (constant pressure), Otto combustion uses Cv (constant volume).",
                                    "Units mismatch in MEP: Ensure W_net is in Joules (J) and Volume in m³ to get MEP in Pascal (N/m²), then convert to bar (1 bar = 10⁵ Pa).",
                                    "Incomplete assumptions: Always write 'Working fluid is ideal gas' and 'Reversible processes'."
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrepCard(
    marks: Int,
    title: String,
    weightage: String,
    steps: List<String>
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (marks == 7) Color(0xFFEF4444) else Color(0xFFF59E0B)
                ) {
                    Text(
                        text = "$marks MARKS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = weightage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            steps.forEachIndexed { i, step ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${i + 1}. ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = step, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun FormulaCard(
    topic: String,
    formulas: List<Pair<String, String>>
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Functions, contentDescription = null, tint = Color(0xFF38BDF8))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = topic, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))

            formulas.forEach { (name, formula) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = name, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(
                        text = formula,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistCard(
    title: String,
    items: List<String>
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Color(0xFF10B981))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))

            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun PitfallCard(
    title: String,
    pitfalls: List<String>
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
            }
            Spacer(modifier = Modifier.height(12.dp))

            pitfalls.forEach { p ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("⚠️ ", fontSize = 12.sp)
                    Text(text = p, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7F1D1D), lineHeight = 18.sp)
                }
            }
        }
    }
}
