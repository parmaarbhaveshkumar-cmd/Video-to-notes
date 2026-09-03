package com.example.data.local

import com.example.data.model.BoardKeyFrame
import com.example.data.model.DiagramPoint
import com.example.data.model.DiagramType
import com.example.data.model.EngineeringDiagramData
import com.example.data.model.Flashcard
import com.example.data.model.LectureEntity
import com.example.data.model.MarkAnswer
import com.example.data.model.McqQuestion
import com.example.data.model.PyqEntity
import com.example.data.model.PyqItem
import com.example.data.model.QualityCheckReport
import com.example.data.model.SubjectEntity
import com.example.data.model.SyllabusTopic
import com.example.data.model.SyllabusUnit
import com.example.data.model.TopicSection
import com.example.data.model.VivaQuestion

object SampleData {

    fun getDefaultSubjects(): List<SubjectEntity> {
        val thermoSyllabus = listOf(
            SyllabusUnit(
                unitNumber = 1,
                unitTitle = "First & Second Law of Thermodynamics",
                weightageMarks = 18,
                topics = listOf(
                    SyllabusTopic("th_u1_1", "Zeroth & First Law Energy Balance", "Basic Concepts", true, true, "Mastered"),
                    SyllabusTopic("th_u1_2", "Kelvin-Planck & Clausius Statements", "Second Law", true, true, "Revised Once"),
                    SyllabusTopic("th_u1_3", "Carnot Theorem & Entropy Principle", "Second Law", true, true, "Mastered"),
                    SyllabusTopic("th_u1_4", "Availability & Exergy Analysis", "Exergy", false, false, "Pending")
                )
            ),
            SyllabusUnit(
                unitNumber = 2,
                unitTitle = "Air Standard Cycles (Otto, Diesel, Dual)",
                weightageMarks = 22,
                topics = listOf(
                    SyllabusTopic("th_u2_1", "Otto Cycle (P-V & T-S, Efficiency)", "Air Cycles", true, true, "Mastered"),
                    SyllabusTopic("th_u2_2", "Diesel Cycle Analysis", "Air Cycles", true, true, "Mastered"),
                    SyllabusTopic("th_u2_3", "Dual Combustion & Comparison", "Air Cycles", false, false, "Pending"),
                    SyllabusTopic("th_u2_4", "Mean Effective Pressure (MEP)", "Performance", false, false, "Pending")
                )
            ),
            SyllabusUnit(
                unitNumber = 3,
                unitTitle = "Vapour Power Cycles (Rankine)",
                weightageMarks = 20,
                topics = listOf(
                    SyllabusTopic("th_u3_1", "Simple Rankine Cycle on T-S & h-s", "Steam Cycles", false, false, "Pending"),
                    SyllabusTopic("th_u3_2", "Reheat & Regenerative Rankine Cycle", "Steam Cycles", false, false, "Pending"),
                    SyllabusTopic("th_u3_3", "Cogeneration & Binary Vapor Cycle", "Steam Cycles", false, false, "Pending")
                )
            ),
            SyllabusUnit(
                unitNumber = 4,
                unitTitle = "Refrigeration & Psychrometry",
                weightageMarks = 15,
                topics = listOf(
                    SyllabusTopic("th_u4_1", "Bell-Coleman Air Refrigeration", "Refrigeration", false, false, "Pending"),
                    SyllabusTopic("th_u4_2", "Vapour Compression Refrigeration (VCR)", "Refrigeration", false, false, "Pending"),
                    SyllabusTopic("th_u4_3", "Psychrometric Chart & Processes", "Psychrometry", false, false, "Pending")
                )
            )
        )

        val matScienceSyllabus = listOf(
            SyllabusUnit(
                unitNumber = 1,
                unitTitle = "Crystal Structures & Mechanical Testing",
                weightageMarks = 20,
                topics = listOf(
                    SyllabusTopic("ms_u1_1", "Stress-Strain Behavior of Mild Steel", "Mechanical Behavior", true, true, "Mastered"),
                    SyllabusTopic("ms_u1_2", "BCC, FCC, HCP Crystal Imperfections", "Crystallography", true, false, "Revised Once"),
                    SyllabusTopic("ms_u1_3", "Brinell & Rockwell Hardness Testing", "Testing", false, false, "Pending")
                )
            ),
            SyllabusUnit(
                unitNumber = 2,
                unitTitle = "Phase Diagrams & Iron-Carbon System",
                weightageMarks = 25,
                topics = listOf(
                    SyllabusTopic("ms_u2_1", "Gibbs Phase Rule & Lever Rule", "Phase Equilibrium", false, false, "Pending"),
                    SyllabusTopic("ms_u2_2", "Iron-Iron Carbide (Fe-C) Equilibrium Diagram", "Iron Carbon", false, false, "Pending"),
                    SyllabusTopic("ms_u2_3", "TTT & CCT Curves", "Phase Transformations", false, false, "Pending")
                )
            )
        )

        val mathSyllabus = listOf(
            SyllabusUnit(
                unitNumber = 1,
                unitTitle = "Linear Algebra & Matrices",
                weightageMarks = 20,
                topics = listOf(
                    SyllabusTopic("math_u1_1", "Eigenvalues & Eigenvectors", "Matrices", true, true, "Mastered"),
                    SyllabusTopic("math_u1_2", "Cayley-Hamilton Theorem", "Matrices", true, true, "Revised Once"),
                    SyllabusTopic("math_u1_3", "Diagonalization of Matrices", "Matrices", false, false, "Pending")
                )
            ),
            SyllabusUnit(
                unitNumber = 2,
                unitTitle = "Fourier Series & Transforms",
                weightageMarks = 25,
                topics = listOf(
                    SyllabusTopic("math_u2_1", "Dirichlet Conditions & Euler Formulas", "Fourier Series", true, true, "Mastered"),
                    SyllabusTopic("math_u2_2", "Half-Range Sine & Cosine Series", "Fourier Series", false, false, "Pending"),
                    SyllabusTopic("math_u2_3", "Fourier Integral & Transforms", "Transforms", false, false, "Pending")
                )
            )
        )

        val machineDrawingSyllabus = listOf(
            SyllabusUnit(
                unitNumber = 1,
                unitTitle = "Limits, Fits, Tolerances & Surface Finish",
                weightageMarks = 20,
                topics = listOf(
                    SyllabusTopic("md_u1_1", "Hole Basis vs Shaft Basis System", "Fits & Limits", true, true, "Mastered"),
                    SyllabusTopic("md_u1_2", "Geometric Dimensioning & Tolerancing (GD&T)", "Tolerances", false, false, "Pending")
                )
            )
        )

        val mechDesignSyllabus = listOf(
            SyllabusUnit(
                unitNumber = 1,
                unitTitle = "Stress Analysis & Mohr's Circle",
                weightageMarks = 25,
                topics = listOf(
                    SyllabusTopic("mdes_u1_1", "2D Principal Stresses & Mohr's Circle", "Stress Analysis", true, true, "Mastered"),
                    SyllabusTopic("mdes_u1_2", "Theories of Failure (Tresca, Von Mises)", "Failure Theories", false, false, "Pending")
                )
            )
        )

        val mfgSyllabus = listOf(
            SyllabusUnit(
                unitNumber = 1,
                unitTitle = "Casting & Metal Forming",
                weightageMarks = 20,
                topics = listOf(
                    SyllabusTopic("mfg_u1_1", "Gating System Design & Chvorinov Rule", "Casting", true, true, "Revised Once"),
                    SyllabusTopic("mfg_u1_2", "Rolling, Forging & Extrusion", "Metal Forming", false, false, "Pending")
                )
            )
        )

        val metrologySyllabus = listOf(
            SyllabusUnit(
                unitNumber = 1,
                unitTitle = "Linear & Angular Measurements",
                weightageMarks = 18,
                topics = listOf(
                    SyllabusTopic("met_u1_1", "Slip Gauges, Sine Bar & Auto-Collimator", "Precision Measurement", true, true, "Mastered"),
                    SyllabusTopic("met_u1_2", "CMM & Surface Roughness Parameters", "Metrology", false, false, "Pending")
                )
            )
        )

        return emptyList()
    }

    fun getDefaultChapters(): List<com.example.data.model.ChapterEntity> {
        return emptyList()
    }

    fun getDefaultNotes(): List<com.example.data.model.NoteEntity> {
        return emptyList()
    }

    fun getDefaultLectures(): List<LectureEntity> {
        return emptyList()
    }

    fun getSampleExampleLectures(): List<LectureEntity> {
        val ottoTopic = TopicSection(
            topicId = "top_otto_1",
            topicName = "Air Standard Otto Cycle",
            timestampSeconds = 75, // 01:15
            definition = "The Otto cycle is an ideal thermodynamic cycle consisting of two reversible adiabatic (isentropic) processes and two reversible constant-volume (isochoric) processes. It forms the benchmark theoretical model for spark-ignition (petrol) internal combustion engines.",
            simpleExplanation = "In simple words: The petrol engine draws air-fuel mixture, compresses it tightly without letting heat escape (1-2), ignites it with a spark so pressure shoots up instantly at constant volume (2-3), pushes the piston down to produce power (3-4), and finally dumps heat out as exhaust at constant volume (4-1).",
            keyPoints = listOf(
                "Cycle has 4 distinct reversible processes: 2 Isentropic + 2 Isochoric.",
                "Heat addition (Q_in) occurs at Constant Volume (Process 2-3).",
                "Heat rejection (Q_out) occurs at Constant Volume (Process 4-1).",
                "Working fluid is assumed as an ideal gas (air) with constant specific heats."
            ),
            workingProcess = "1. Process 1-2 (Isentropic Compression): Piston moves from BDC to TDC, air compressed reversibly with no heat transfer (s1 = s2, P increases, V decreases).\n2. Process 2-3 (Constant Volume Heat Addition): Spark ignition occurs. Volume remains constant (V2 = V3), temperature and pressure rise steeply to maximum.\n3. Process 3-4 (Isentropic Expansion): High-pressure gas expands doing mechanical work on piston from TDC to BDC (s3 = s4).\n4. Process 4-1 (Constant Volume Heat Rejection): Exhaust valve opens, instantaneous blowdown cools gas to initial state at constant volume (V4 = V1).",
            formula = "η_otto = 1 - (1 / (r^(γ - 1)))",
            variablesAndUnits = listOf(
                "η_otto = Thermal efficiency of Otto cycle (dimensionless / percentage %)",
                "r = Compression ratio = V1 / V2 = (V_c + V_s) / V_c (dimensionless, typically 6 to 10 for SI engines)",
                "γ (gamma) = Ratio of specific heats (Cp / Cv) = 1.4 for standard air",
                "V_s = Swept volume (m³)",
                "V_c = Clearance volume (m³)",
                "P = Pressure (bar or kPa, 1 bar = 100 kPa)",
                "T = Absolute temperature (Kelvin K)"
            ),
            exampleProblem = "For a petrol engine with compression ratio r = 8.5 operating on standard air (γ = 1.4):\nη_otto = 1 - 1 / (8.5^(1.4 - 1)) = 1 - 1 / (8.5^0.4) = 1 - 1 / 2.353 = 1 - 0.425 = 0.575 (57.5% efficiency).",
            advantages = listOf(
                "High theoretical thermal efficiency for spark-ignition engines.",
                "Fast heat release due to constant volume combustion.",
                "Compact power-to-weight ratio in lightweight vehicles."
            ),
            disadvantages = listOf(
                "Compression ratio is limited (r ≤ 10-12) to prevent engine knock/auto-ignition.",
                "Higher peak pressure puts structural stress on cylinder head."
            ),
            applications = listOf(
                "Automobile 4-stroke & 2-stroke petrol engines (cars, motorcycles).",
                "Light aircraft piston engines.",
                "Portable gasoline generators and lawnmowers."
            ),
            importantExamPoints = listOf(
                "🔥 Exam Rule: Always state that thermal efficiency depends ONLY on compression ratio 'r' and heat capacity ratio 'γ'.",
                "⭐ Peak cycle temperature occurs at state 3 (T3), peak pressure at P3.",
                "💡 Work output = W_net = Q_in - Q_out = m*Cv*(T3 - T2) - m*Cv*(T4 - T1)."
            ),
            answers = listOf(
                MarkAnswer(
                    marks = 2,
                    title = "2-Mark Answer: Define Otto Cycle & State Efficiency Formula",
                    keyPoints = listOf("Definition as 2 isentropic + 2 isochoric processes", "Efficiency formula in terms of r and gamma"),
                    answerText = "The Otto cycle is an ideal air-standard cycle for spark-ignition engines consisting of two reversible adiabatic (isentropic) and two constant-volume processes.\n\nThermal Efficiency Formula:\nη_otto = 1 - 1 / (r^(γ - 1))\nWhere r = V1/V2 (compression ratio) and γ = Cp/Cv = 1.4 for air.",
                    diagramRequired = false,
                    formulaRequired = true
                ),
                MarkAnswer(
                    marks = 3,
                    title = "3-Mark Answer: Four Processes & P-V Sketch",
                    keyPoints = listOf("Name 4 processes accurately", "State where heat addition/rejection takes place", "Mention compression ratio definition"),
                    answerText = "The four processes of Otto Cycle are:\n1. 1-2: Reversible adiabatic (isentropic) compression (s = C)\n2. 2-3: Constant volume heat addition (Q_in = m*Cv*(T3-T2))\n3. 3-4: Reversible adiabatic (isentropic) expansion (s = C)\n4. 4-1: Constant volume heat rejection (Q_out = m*Cv*(T4-T1))\nEfficiency η = 1 - (1 / r^(γ-1)). Efficiency increases monotonically with compression ratio.",
                    diagramRequired = true,
                    formulaRequired = true
                ),
                MarkAnswer(
                    marks = 5,
                    title = "5-Mark Answer: Derivation of Air-Standard Efficiency of Otto Cycle",
                    keyPoints = listOf("State assumptions", "Q_in and Q_out equations", "Isentropic temperature-volume relations", "Final formula derivation"),
                    answerText = "Assumptions:\n1. Working fluid is air behaving as ideal gas with constant Cp, Cv.\n2. All processes are internally reversible.\n3. Heat addition & rejection are simulated from external reservoirs.\n\nDerivation:\n1. Heat supplied: Q_in = m·Cv·(T3 - T2)\n2. Heat rejected: Q_out = m·Cv·(T4 - T1)\n3. Thermal efficiency: η = (Q_in - Q_out) / Q_in = 1 - (T4 - T1) / (T3 - T2)\n   = 1 - [ T1·((T4/T1) - 1) ] / [ T2·((T3/T2) - 1) ]\n4. For isentropic process 1-2: T2 / T1 = (V1 / V2)^(γ-1) = r^(γ-1)\n5. For isentropic process 3-4: T3 / T4 = (V4 / V3)^(γ-1) = (V1 / V2)^(γ-1) = r^(γ-1)\n   Since T2/T1 = T3/T4, it implies T4/T1 = T3/T2, so [(T4/T1) - 1] / [(T3/T2) - 1] = 1.\n6. Substituting into η equation:\n   η_otto = 1 - (T1 / T2) = 1 - (1 / r^(γ-1)). [Proved]",
                    diagramRequired = true,
                    formulaRequired = true
                ),
                MarkAnswer(
                    marks = 7,
                    title = "7-Mark Answer: Comprehensive Otto Cycle Analysis, P-V & T-S, Mean Effective Pressure",
                    keyPoints = listOf("Detailed process breakdown", "P-V and T-S diagram explanation", "Full efficiency derivation", "Mean Effective Pressure (MEP) expression", "Practical limitations (knocking)"),
                    answerText = "1. Introduction & P-V / T-S Diagrams:\nThe Otto cycle governs 4-stroke spark ignition internal combustion engines. On the P-V plane, constant volume lines are vertical (V2=V3, V4=V1) and isentropic curves follow PV^γ = C.\n\n2. Efficiency Derivation:\nWork output W_net = Q_in - Q_out = m Cv (T3 - T2) - m Cv (T4 - T1).\nη_otto = 1 - (Q_out / Q_in) = 1 - (T4 - T1)/(T3 - T2).\nUsing isentropic relations (T2/T1 = r^(γ-1) and T3/T4 = r^(γ-1)), we get:\nη_otto = 1 - (1 / r^(γ - 1)).\n\n3. Mean Effective Pressure (MEP):\nMEP is the fictitious constant pressure that, if acting on the piston throughout the power stroke, would produce the same net work.\nMEP = W_net / (V1 - V2) = [ P1 · r · (rp - 1) · (r^(γ-1) - 1) ] / [ (γ - 1) · (r - 1) ]\nwhere rp = P3/P2 is the pressure ratio.\n\n4. Practical Constraints:\nAlthough efficiency increases as 'r' increases, in practical petrol engines 'r' is restricted to 8-11 because higher compression heats the air-fuel mixture above its auto-ignition temperature, causing premature explosive detonation (Engine Knock / Pinging).",
                    diagramRequired = true,
                    formulaRequired = true
                )
            ),
            diagram = EngineeringDiagramData(
                type = DiagramType.PV_DIAGRAM_OTTO,
                title = "Otto Cycle P-V and T-S Engineering Diagram",
                xAxisLabel = "Volume V (m³)",
                yAxisLabel = "Pressure P (bar)",
                points = listOf(
                    DiagramPoint(x = 0.85f, y = 0.15f, label = "1", description = "State 1: Start of isentropic compression (BDC, P1, V1, T1)"),
                    DiagramPoint(x = 0.25f, y = 0.45f, label = "2", description = "State 2: End of isentropic compression (TDC, P2, V2=Vc, T2)"),
                    DiagramPoint(x = 0.25f, y = 0.88f, label = "3", description = "State 3: Peak cycle pressure & temp after constant volume heat addition (P3, V3=V2, T3)"),
                    DiagramPoint(x = 0.85f, y = 0.35f, label = "4", description = "State 4: End of isentropic expansion (P4, V4=V1, T4)")
                ),
                processLabels = listOf(
                    "1→2: Isentropic Compression (s=C, PV^γ=C)",
                    "2→3: Const Volume Heat Addition (Q_in = mCvΔT)",
                    "3→4: Isentropic Expansion / Power Stroke (s=C)",
                    "4→1: Const Volume Heat Rejection (Q_out = mCvΔT)"
                ),
                notes = "Compression ratio r = V1 / V2. Work Net = Area enclosed by 1-2-3-4.",
                formula = "η = 1 - 1/(r^(γ-1))"
            )
        )

        val carnotTopic = TopicSection(
            topicId = "top_carnot_2",
            topicName = "Carnot Theorem and Maximum Efficiency",
            timestampSeconds = 480, // 08:00
            definition = "Carnot cycle is a totally reversible ideal thermodynamic cycle operating between two thermal reservoirs at temperatures TH and TL. Carnot theorem states that no heat engine operating between two given temperatures can be more efficient than a reversible Carnot engine.",
            simpleExplanation = "In simple terms: Carnot cycle gives the absolute upper theoretical speed-limit of efficiency for any heat engine. It proves that you can never turn 100% of heat into work, and efficiency depends purely on the reservoir temperatures in Kelvin.",
            keyPoints = listOf(
                "Consists of 2 Isothermal (constant temperature) + 2 Isentropic (reversible adiabatic) processes.",
                "Has the maximum possible thermal efficiency between temperatures TH and TL.",
                "Efficiency is independent of working fluid properties.",
                "Carnot efficiency: η_carnot = 1 - (TL / TH)."
            ),
            workingProcess = "1. 1-2 (Isothermal Heat Addition): Gas absorbs heat Q_H from hot source at constant high temperature TH.\n2. 2-3 (Isentropic Expansion): Reversible adiabatic expansion lowers temperature from TH down to TL.\n3. 3-4 (Isothermal Heat Rejection): Gas rejects heat Q_L to cold sink at constant low temperature TL.\n4. 4-1 (Isentropic Compression): Reversible adiabatic compression restores gas to initial state at TH.",
            formula = "η_carnot = (TH - TL) / TH = 1 - (TL / TH)",
            variablesAndUnits = listOf(
                "TH = Absolute temperature of heat source (Kelvin K, where K = °C + 273.15)",
                "TL = Absolute temperature of heat sink (Kelvin K)",
                "η_carnot = Maximum reversible Carnot efficiency (fraction or %)"
            ),
            exampleProblem = "A heat engine operates between a furnace at 600°C (873.15 K) and ambient water at 25°C (298.15 K):\nη_max = 1 - (298.15 / 873.15) = 1 - 0.341 = 0.659 (65.9% maximum limit).",
            advantages = listOf("Serves as the absolute standard of comparison for all real thermodynamic heat engines."),
            disadvantages = listOf("Impractical to build because isothermal requires extremely slow speed while isentropic requires extremely fast speed."),
            applications = listOf("Standard benchmark for steam turbines, gas turbines, and internal combustion cycles."),
            importantExamPoints = listOf(
                "🔥 Exam Rule: Temperatures MUST ALWAYS be converted to absolute Kelvin (K), never Celsius!",
                "⭐ Clausius Inequality: ∮ (dQ / T) ≤ 0 (= 0 for reversible Carnot cycle).",
                "💡 Second Law Corollary: All reversible engines operating between the same two temperature reservoirs have the same efficiency."
            ),
            answers = listOf(
                MarkAnswer(
                    marks = 2,
                    title = "2-Mark Answer: State Carnot Theorem & Formula",
                    keyPoints = listOf("Carnot theorem statement", "Kelvin temperature formula"),
                    answerText = "Carnot's Theorem states that no heat engine working between two fixed temperatures (TH and TL) can be more efficient than a reversible engine working between the same reservoirs.\nFormula: η_carnot = 1 - (TL / TH) (where TH, TL are in Kelvin).",
                    diagramRequired = false,
                    formulaRequired = true
                ),
                MarkAnswer(
                    marks = 5,
                    title = "5-Mark Answer: Explain Carnot Cycle with P-V and T-S Diagrams",
                    keyPoints = listOf("4 processes list", "T-S rectangle area explanation", "Efficiency formula derivation"),
                    answerText = "The Carnot cycle consists of 4 reversible processes:\n1. 1-2: Reversible isothermal heat addition at TH (Q_in = TH · ΔS)\n2. 2-3: Reversible adiabatic (isentropic) expansion from TH to TL (s = C)\n3. 3-4: Reversible isothermal heat rejection at TL (Q_out = TL · ΔS)\n4. 4-1: Reversible adiabatic (isentropic) compression from TL to TH (s = C)\n\nOn T-S diagram, the cycle forms a perfect rectangle.\nEfficiency η = (Q_in - Q_out) / Q_in = (TH·ΔS - TL·ΔS) / (TH·ΔS) = 1 - (TL / TH).",
                    diagramRequired = true,
                    formulaRequired = true
                )
            ),
            diagram = EngineeringDiagramData(
                type = DiagramType.PV_DIAGRAM_CARNOT,
                title = "Carnot Cycle P-V and T-S Diagram",
                xAxisLabel = "Entropy S (kJ/kg·K)",
                yAxisLabel = "Temperature T (K)",
                points = listOf(
                    DiagramPoint(0.2f, 0.8f, "1", "State 1: Start of isothermal heat addition at TH"),
                    DiagramPoint(0.7f, 0.8f, "2", "State 2: End of isothermal heat addition (Q_in = TH·ΔS)"),
                    DiagramPoint(0.7f, 0.3f, "3", "State 3: End of isentropic expansion at TL"),
                    DiagramPoint(0.2f, 0.3f, "4", "State 4: End of isothermal heat rejection (Q_out = TL·ΔS)")
                ),
                processLabels = listOf(
                    "1→2: Isothermal Heat Addition at TH",
                    "2→3: Isentropic Expansion (TH → TL)",
                    "3→4: Isothermal Heat Rejection at TL",
                    "4→1: Isentropic Compression (TL → TH)"
                ),
                notes = "On T-S diagram, Carnot cycle is an exact rectangle. Net work is the area inside rectangle.",
                formula = "η = 1 - (TL / TH)"
            )
        )

        val thermoBoardFrames = listOf(
            BoardKeyFrame(
                id = "frame_th_1",
                timestampSeconds = 755, // 12:35
                title = "Otto Cycle P-V & T-S State Points",
                visualType = "ENGINEERING_DIAGRAM",
                ocrExtractedContent = "P-V Indicator Diagram: 1->2 (s=C), 2->3 (V=C, Q_in), 3->4 (s=C, W_net), 4->1 (V=C, Q_out)",
                figureDescription = "Clear classroom chalkboard diagram with labelled state points 1, 2, 3, 4, clearance volume Vc, and swept volume Vs.",
                keyTakeaway = "Key visual reference for 5 & 7-mark university exam answers"
            ),
            BoardKeyFrame(
                id = "frame_th_2",
                timestampSeconds = 1100, // 18:20
                title = "Air Standard Efficiency Derivation",
                visualType = "WHITEBOARD_WRITING",
                ocrExtractedContent = "η_otto = 1 - (Q_out / Q_in) = 1 - (T4 - T1)/(T3 - T2) = 1 - (1 / r^(γ-1))",
                figureDescription = "Mathematical proof written step-by-step on whiteboard with boxed final formula for efficiency.",
                keyTakeaway = "Primary formula required for GTU / University numericals"
            ),
            BoardKeyFrame(
                id = "frame_th_3",
                timestampSeconds = 1450, // 24:10
                title = "Carnot Theorem & Temperature Limits",
                visualType = "PPT_SLIDE",
                ocrExtractedContent = "η_carnot = (T_H - T_L) / T_H [T in Kelvin] | Maximum theoretical upper boundary",
                figureDescription = "Projector presentation slide comparing Carnot maximum thermal efficiency against practical cycles.",
                keyTakeaway = "Crucial for exam: Temperatures must be in absolute Kelvin"
            )
        )

        val thermoLecture = LectureEntity(
            id = "lec_thermo_01",
            title = "Air Standard Cycles: Otto & Carnot Cycle Derivations",
            subject = "Engineering Thermodynamics",
            unitName = "Unit 2: Air Standard Cycles",
            dateEpoch = System.currentTimeMillis() - (86400000L * 1), // yesterday
            durationSeconds = 1845, // ~30 mins
            audioFilePath = "/storage/emulated/0/Download/thermo_lec_01.m4a",
            videoFilePath = "/storage/emulated/0/Movies/thermo_lec_01.mp4",
            mediaType = "VIDEO",
            videoQuality = "720p HD",
            boardKeyFramesJson = JsonUtils.boardKeyFrameListToJson(thermoBoardFrames),
            videoFileSizeMb = 412.5,
            audioFileSizeMb = 21.4,
            hasVideo = true,
            hasAudio = true,
            hasNotes = true,
            originalTranscript = "Good morning students, aaje apde unit 2 ma air standard cycles start karishu. Otto cycle and Carnot cycle are the most important topics for Gujarat and All-India exams. Samjho bhai, Otto cycle ma 4 processes hoy che: 2 isentropic compression/expansion and 2 constant volume heat addition and rejection. Formula derivation 5-marks ma 100% ave che, η = 1 - 1/(r^(γ-1)). Let's derive it step by step on board...",
            cleanTranscript = "Today we analyze Air Standard Cycles focusing on Otto Cycle and Carnot Cycle. The Otto cycle comprises four reversible processes: Isentropic compression (1-2), Constant volume heat addition (2-3), Isentropic expansion (3-4), and Constant volume heat rejection (4-1). Thermal efficiency is derived using energy balance and isentropic temperature ratios, yielding η = 1 - 1/(r^(γ-1)).",
            spokenLanguage = "Mixed Hinglish / Gujlish",
            summary = "Complete derivation of Otto Cycle efficiency, P-V and T-S diagram representations, compression ratio impact, and Carnot theorem temperature limits with 2, 3, 5, and 7-mark model answers.",
            topicsJson = JsonUtils.topicListToJson(listOf(ottoTopic, carnotTopic)),
            qualityCheckJson = JsonUtils.qualityReportToJson(
                QualityCheckReport(
                    isTechnicallyAccurate = true,
                    formulasVerified = true,
                    unitsStandardized = true,
                    markAlignmentVerified = true,
                    noHallucinationsDetected = true,
                    sourcesSummary = "Verified from classroom lecture audio + Standard Engineering Thermodynamics (P.K. Nag / Cengel & Boles)",
                    qualityScorePercent = 99
                )
            ),
            mcqsJson = JsonUtils.mcqListToJson(
                listOf(
                    McqQuestion("mcq_1", "The thermal efficiency of an ideal air-standard Otto cycle depends purely upon:", listOf("Compression ratio and ratio of specific heats", "Peak combustion pressure", "Engine speed in RPM", "Fuel calorific value"), 0, "η_otto = 1 - 1/(r^(γ-1)), thus it depends solely on compression ratio 'r' and adiabatic index 'γ'.", "Air Standard Otto Cycle"),
                    McqQuestion("mcq_2", "In an Otto cycle, heat addition and heat rejection occur respectively at:", listOf("Constant pressure & Constant pressure", "Constant volume & Constant volume", "Constant temperature & Constant volume", "Isentropic & Isochoric"), 1, "In Otto cycle, spark ignition causes constant volume heat addition (2-3) and exhaust causes constant volume heat rejection (4-1).", "Air Standard Otto Cycle"),
                    McqQuestion("mcq_3", "What is the maximum theoretical efficiency of a heat engine operating between 1000 K and 300 K?", listOf("70%", "30%", "85%", "50%"), 0, "η_carnot = 1 - (TL/TH) = 1 - (300/1000) = 1 - 0.3 = 0.70 (70%).", "Carnot Theorem and Maximum Efficiency"),
                    McqQuestion("mcq_4", "Why is compression ratio limited to around 8 to 11 in practical petrol engines?", listOf("To prevent engine knock / auto-ignition of fuel", "To reduce engine weight", "To increase thermal efficiency", "To avoid valve overlap"), 0, "Excess compression increases temperature above octane auto-ignition point leading to destructive engine knocking.", "Air Standard Otto Cycle")
                )
            ),
            flashcardsJson = JsonUtils.flashcardListToJson(
                listOf(
                    Flashcard("fc_1", "What is the formula for Otto Cycle Thermal Efficiency?", "η_otto = 1 - 1 / (r^(γ - 1))", "Thermodynamics", "η = 1 - (1 / r^(γ-1))", true),
                    Flashcard("fc_2", "State the 4 processes of Otto Cycle in sequence.", "1-2: Isentropic Compression\n2-3: Const Vol Heat Addition\n3-4: Isentropic Expansion\n4-1: Const Vol Heat Rejection", "Thermodynamics", "", false),
                    Flashcard("fc_3", "What is the formula for Carnot Efficiency?", "η_carnot = 1 - (TL / TH)\n(Note: Temperatures must be in absolute Kelvin)", "Thermodynamics", "η = (TH - TL) / TH", true),
                    Flashcard("fc_4", "Define Compression Ratio (r).", "r = V1 / V2 = Total Cylinder Volume / Clearance Volume = (Vc + Vs) / Vc", "Thermodynamics", "r = (Vc + Vs)/Vc", false)
                )
            ),
            vivaJson = JsonUtils.vivaListToJson(
                listOf(
                    VivaQuestion("Why can we not build an actual Carnot engine?", "Because isothermal heat addition/rejection requires piston to move infinitesimally slow (quasi-static), while isentropic expansion/compression requires piston to move infinitely fast (adiabatic). Both cannot happen in the same physical engine.", "State the contradiction between isothermal speed and adiabatic speed clearly."),
                    VivaQuestion("What is the difference between Otto Cycle and Diesel Cycle regarding heat addition?", "In Otto cycle heat is added at Constant Volume (spark plug ignition), while in Diesel cycle fuel is injected gradually so heat is added at Constant Pressure.", "Emphasize Constant Volume vs Constant Pressure combustion.")
                )
            ),
            isExamPrepReady = true
        )

        val matScienceTopic = TopicSection(
            topicId = "top_ms_1",
            topicName = "Stress-Strain Behavior of Mild Steel (Tensile Test)",
            timestampSeconds = 45,
            definition = "The stress-strain curve of mild steel (ductile material) describes the relationship between applied engineering stress (σ = P/A0) and resulting engineering strain (ε = ΔL/L0) under standard uniaxial tension test (ASTM E8).",
            simpleExplanation = "When you pull a steel rod, it first stretches elastically like a spring (Hooke's Law), reaches a yield point where atoms slip and it deforms permanently without extra load, then hardens with strain, hits maximum tensile strength, necks down in the middle, and finally snaps.",
            keyPoints = listOf(
                "Point A: Proportional Limit (Hooke's law σ = E·ε strictly holds).",
                "Point B: Elastic Limit (material returns to original size on unloading).",
                "Point C & D: Upper & Lower Yield Points (yielding due to carbon atoms unpinning dislocations / Cottrell atmospheres).",
                "Point E: Ultimate Tensile Strength (UTS - maximum load bearing capacity).",
                "Point F: Fracture / Breaking Point with Cup-and-Cone failure surface."
            ),
            workingProcess = "1. Elastic Region (O-A-B): Linear relationship up to proportional limit, elastic recovery up to B.\n2. Yielding Region (B-C-D): Lüders bands appear, plastic deformation occurs with yield drop.\n3. Strain Hardening Region (D-E): Crystal dislocation tangles increase resistance; stress rises to maximum UTS.\n4. Necking & Fracture (E-F): Local cross-sectional area decreases rapidly until ductile cup-and-cone rupture.",
            formula = "σ = P / A₀,  ε = ΔL / L₀,  E = σ / ε (Hooke's Law)",
            variablesAndUnits = listOf(
                "σ (sigma) = Engineering stress (N/mm² or MPa)",
                "ε (epsilon) = Engineering strain (dimensionless)",
                "E = Modulus of Elasticity / Young's Modulus (~200 to 210 GPa for mild steel)",
                "P = Applied tensile load (N or kN)",
                "A₀ = Initial cross-sectional area (mm²)",
                "L₀ = Gauge length (mm)"
            ),
            exampleProblem = "A mild steel rod of 12 mm diameter and 50 mm gauge length elongates by 0.05 mm under a 20 kN tensile load in elastic range:\nArea A₀ = π*(6)² = 113.1 mm²\nStress σ = 20,000 / 113.1 = 176.8 MPa\nStrain ε = 0.05 / 50 = 0.001\nYoung's Modulus E = 176.8 / 0.001 = 176,800 MPa = 176.8 GPa.",
            advantages = listOf("Provides fundamental mechanical properties: yield strength, tensile strength, ductility (% elongation), toughness, and modulus of resilience."),
            disadvantages = listOf("Engineering curve does not account for instantaneous reduced cross-sectional area (true stress-strain is higher after necking)."),
            applications = listOf("Structural steel design for bridges, buildings, pressure vessels, and automotive chassis."),
            importantExamPoints = listOf(
                "🔥 Exam Rule: Clearly mark Upper Yield Point (C), Lower Yield Point (D), UTS (E), and Fracture Point (F).",
                "⭐ Ductile fracture exhibits classic 'Cup and Cone' shape with 45° shear lips.",
                "💡 Area under stress-strain curve up to fracture represents 'Modulus of Toughness'."
            ),
            answers = listOf(
                MarkAnswer(
                    marks = 2,
                    title = "2-Mark Answer: Define Proportional Limit and Hooke's Law",
                    keyPoints = listOf("Definition of proportional limit", "Hooke's law formula"),
                    answerText = "Proportional Limit is the maximum stress up to which stress is directly proportional to strain (Hooke's Law holds: σ = E·ε). Beyond this point, the curve deviates from linearity.\nFormula: E = σ / ε (where E is Young's Modulus in GPa/MPa).",
                    diagramRequired = false,
                    formulaRequired = true
                ),
                MarkAnswer(
                    marks = 5,
                    title = "5-Mark Answer: Explain Stress-Strain Curve of Mild Steel with All Salient Points",
                    keyPoints = listOf("List all points O, A, B, C, D, E, F", "Explain elastic, plastic, strain hardening and necking zones", "Sketch required"),
                    answerText = "Salient points on the Mild Steel Tensile Curve:\n1. O to A (Proportional Limit): Stress ∝ Strain, slope gives Young's Modulus E.\n2. B (Elastic Limit): Maximum stress without permanent set.\n3. C (Upper Yield Point): Onset of plastic flow where dislocations break free from interstitial carbon atoms.\n4. D (Lower Yield Point): Plastic deformation continues at nearly constant lower stress.\n5. E (Ultimate Tensile Strength): Peak engineering stress before localized necking.\n6. F (Fracture Point): Cup-and-cone ductile failure due to microvoid coalescence.\nKey parameters extracted: Yield Strength (σ_y), Tensile Strength (σ_uts), % Elongation (ductility), and Toughness (total area under curve).",
                    diagramRequired = true,
                    formulaRequired = true
                )
            ),
            diagram = EngineeringDiagramData(
                type = DiagramType.STRESS_STRAIN_CURVE,
                title = "Stress-Strain Curve for Mild Steel under Tension",
                xAxisLabel = "Strain ε (ΔL/L₀)",
                yAxisLabel = "Stress σ (MPa)",
                points = listOf(
                    DiagramPoint(0.12f, 0.40f, "A", "A: Proportional Limit (σ = E·ε)"),
                    DiagramPoint(0.18f, 0.46f, "B", "B: Elastic Limit"),
                    DiagramPoint(0.24f, 0.54f, "C", "C: Upper Yield Point"),
                    DiagramPoint(0.32f, 0.48f, "D", "D: Lower Yield Point (Plastic flow)"),
                    DiagramPoint(0.65f, 0.88f, "E", "E: Ultimate Tensile Strength (UTS)"),
                    DiagramPoint(0.90f, 0.65f, "F", "F: Fracture / Breaking Point (Cup & Cone)")
                ),
                processLabels = listOf(
                    "O→B: Elastic Region",
                    "C→D: Yield Plateau / Lüders Bands",
                    "D→E: Strain Hardening",
                    "E→F: Necking & Ductile Fracture"
                ),
                notes = "Area under elastic curve = Resilience. Total area under curve = Toughness.",
                formula = "σ = P/A₀,  ε = ΔL/L₀"
            )
        )

        val matScienceBoardFrames = listOf(
            BoardKeyFrame(
                id = "frame_ms_1",
                timestampSeconds = 45,
                title = "Stress-Strain Curve on Board",
                visualType = "WHITEBOARD_WRITING",
                ocrExtractedContent = "σ = P/A0 | ε = ΔL/L0 | Point A Proportional Limit, C Upper Yield, E UTS, F Fracture",
                figureDescription = "Hand-drawn complete mild steel tensile curve with all 6 salient points labelled.",
                keyTakeaway = "Essential 7-mark diagram for Material Science university examinations"
            )
        )

        val matScienceLecture = LectureEntity(
            id = "lec_mat_01",
            title = "Tensile Testing & Stress-Strain Behavior of Ductile Materials",
            subject = "Material Science",
            unitName = "Unit 1: Mechanical Testing",
            dateEpoch = System.currentTimeMillis() - (86400000L * 3),
            durationSeconds = 1420,
            audioFilePath = "/storage/emulated/0/Download/mat_sci_lec_01.m4a",
            videoFilePath = "/storage/emulated/0/Movies/mat_sci_lec_01.mp4",
            mediaType = "VIDEO",
            videoQuality = "720p HD",
            boardKeyFramesJson = JsonUtils.boardKeyFrameListToJson(matScienceBoardFrames),
            videoFileSizeMb = 318.0,
            audioFileSizeMb = 16.2,
            hasVideo = true,
            hasAudio = true,
            hasNotes = true,
            originalTranscript = "Namaste students, today we discuss UTM tensile testing of mild steel. Dekho, when we apply tensile load on a standard ASTM mild steel specimen, pehle elastic zone aave che jya Hooke's law valid che. Then upper yield point C and lower yield point D aave che because of carbon atom dislocation pinning. Then strain hardening leads to Ultimate Tensile Strength E and necking happens before fracture at point F...",
            cleanTranscript = "Uniaxial tensile testing of ductile mild steel on a Universal Testing Machine (UTM) reveals distinct stages: Elastic region (O-A-B), Yield point phenomenon (C-D), Strain hardening (D-E), and Necking to Fracture (E-F). Key extracted properties include Young's Modulus, Yield Strength, UTS, % Elongation, and Modulus of Toughness.",
            spokenLanguage = "Mixed Hinglish / Gujlish",
            summary = "Step-by-step breakdown of mild steel stress-strain curve, Hooke's law, upper and lower yield points, UTS, necking, and cup-and-cone ductile fracture with 2 and 5-mark exam answers.",
            topicsJson = JsonUtils.topicListToJson(listOf(matScienceTopic)),
            qualityCheckJson = JsonUtils.qualityReportToJson(
                QualityCheckReport(
                    isTechnicallyAccurate = true,
                    formulasVerified = true,
                    unitsStandardized = true,
                    markAlignmentVerified = true,
                    noHallucinationsDetected = true,
                    sourcesSummary = "Verified from Material Science & Metallurgy (Callister / Raghavan)",
                    qualityScorePercent = 98
                )
            ),
            mcqsJson = JsonUtils.mcqListToJson(
                listOf(
                    McqQuestion("mcq_ms_1", "The area under the complete stress-strain curve up to the fracture point represents:", listOf("Toughness", "Resilience", "Hardness", "Yield strength"), 0, "Toughness is the total mechanical energy per unit volume absorbed by the material before fracture.", "Stress-Strain Behavior of Mild Steel"),
                    McqQuestion("mcq_ms_2", "Hooke's Law (Stress ∝ Strain) is valid strictly up to the:", listOf("Proportional Limit", "Elastic Limit", "Upper Yield Point", "Fracture Point"), 0, "Proportional limit is the exact limit of linear proportionality between stress and strain.", "Stress-Strain Behavior of Mild Steel")
                )
            ),
            flashcardsJson = JsonUtils.flashcardListToJson(
                listOf(
                    Flashcard("fc_ms_1", "What causes Upper and Lower Yield Points in Mild Steel?", "Cottrell atmospheres: Interstitial carbon/nitrogen solute atoms pin dislocations. Once pulled free at upper yield stress, dislocations move at lower stress.", "Material Science", "", true),
                    Flashcard("fc_ms_2", "What is the typical value of Young's Modulus (E) for mild steel?", "Approximately 200 to 210 GPa (2 x 10⁵ N/mm²)", "Material Science", "E ≈ 200 GPa", true)
                )
            ),
            vivaJson = JsonUtils.vivaListToJson(
                listOf(
                    VivaQuestion("What is the difference between Engineering Stress and True Stress?", "Engineering stress uses initial cross-sectional area (σ = P/A0), while True stress uses the instantaneous reduced cross-sectional area (σ_true = P/A_inst). True stress is always higher after necking starts.", "Mention instantaneous area vs original initial area.")
                )
            ),
            isExamPrepReady = true
        )

        return listOf(thermoLecture, matScienceLecture)
    }

    fun getDefaultPyqs(): List<PyqEntity> {
        val thermoPyqList = listOf(
            PyqItem(
                id = "pyq_th_1",
                question = "Derive an expression for the air-standard efficiency of an Otto cycle in terms of compression ratio and ratio of specific heats. State all assumptions clearly.",
                subject = "Engineering Thermodynamics",
                unit = "Unit 2: Air Standard Cycles",
                yearRepeated = listOf("Winter 2021", "Summer 2022", "Winter 2023", "Summer 2024", "Winter 2024"),
                frequencyCount = 5,
                marks = 7,
                priorityTag = "Very Important",
                solutionSummary = "State 4 air-standard assumptions → write Q_in = m·Cv·(T3-T2) & Q_out = m·Cv·(T4-T1) → use isentropic relations T2/T1 = T3/T4 = r^(γ-1) → prove η = 1 - 1/(r^(γ-1))."
            ),
            PyqItem(
                id = "pyq_th_2",
                question = "State Carnot Theorem and prove that all reversible heat engines operating between the same two temperature limits have the same thermal efficiency.",
                subject = "Engineering Thermodynamics",
                unit = "Unit 1: First & Second Law",
                yearRepeated = listOf("Summer 2022", "Winter 2023", "Summer 2024"),
                frequencyCount = 3,
                marks = 5,
                priorityTag = "Very Important",
                solutionSummary = "State Carnot theorem → combine assumed more-efficient engine E with reversed engine R to create a self-acting device violating Clausius / Kelvin-Planck statement."
            ),
            PyqItem(
                id = "pyq_th_3",
                question = "Compare Otto, Diesel, and Dual combustion cycles for (a) Same compression ratio and heat input, (b) Same maximum pressure and temperature.",
                subject = "Engineering Thermodynamics",
                unit = "Unit 2: Air Standard Cycles",
                yearRepeated = listOf("Winter 2022", "Summer 2024"),
                frequencyCount = 2,
                marks = 7,
                priorityTag = "Important",
                solutionSummary = "For same r & Q_in: η_Otto > η_Dual > η_Diesel. For same P_max & T_max: η_Diesel > η_Dual > η_Otto. Show with overlaid P-V and T-S diagrams."
            ),
            PyqItem(
                id = "pyq_th_4",
                question = "Define: (a) Compression Ratio, (b) Mean Effective Pressure, (c) Cut-off Ratio.",
                subject = "Engineering Thermodynamics",
                unit = "Unit 2: Air Standard Cycles",
                yearRepeated = listOf("Summer 2023", "Winter 2024"),
                frequencyCount = 2,
                marks = 3,
                priorityTag = "Useful for Revision",
                solutionSummary = "Define r = (Vc+Vs)/Vc; MEP = W_net / Vs; Cut-off ratio rc = V3/V2 for diesel cycle."
            )
        )

        val matSciPyqList = listOf(
            PyqItem(
                id = "pyq_ms_1",
                question = "Draw a neat, labelled stress-strain diagram for mild steel under tensile loading. Explain all salient points including upper and lower yield points, UTS, and fracture.",
                subject = "Material Science",
                unit = "Unit 1: Mechanical Testing",
                yearRepeated = listOf("Winter 2021", "Summer 2022", "Winter 2023", "Summer 2024"),
                frequencyCount = 4,
                marks = 7,
                priorityTag = "Very Important",
                solutionSummary = "Sketch graph with axes σ vs ε → label O, A (Proportional), B (Elastic), C (Upper Yield), D (Lower Yield), E (UTS), F (Fracture) → explain dislocation pinning."
            ),
            PyqItem(
                id = "pyq_ms_2",
                question = "Differentiate between Resilience, Proof Resilience, and Toughness with formulas and sketches.",
                subject = "Material Science",
                unit = "Unit 1: Mechanical Testing",
                yearRepeated = listOf("Summer 2023", "Winter 2024"),
                frequencyCount = 2,
                marks = 5,
                priorityTag = "Important",
                solutionSummary = "Resilience = energy in elastic range; Proof resilience = maximum elastic energy (σ_y² / 2E); Toughness = total area up to fracture."
            )
        )

        return listOf(
            PyqEntity("pyq_thermo", "Engineering Thermodynamics", JsonUtils.pyqListToJson(thermoPyqList)),
            PyqEntity("pyq_matsci", "Material Science", JsonUtils.pyqListToJson(matSciPyqList))
        )
    }
}
