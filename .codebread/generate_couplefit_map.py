#!/usr/bin/env python3
"""
CoupleFit CodeBread Generator
Extracts rich Kotlin AST/Regex nodes, functions, tables, call graph, dependency map,
and API-to-database tracing, then outputs a standalone interactive CodeBread HTML app.
"""

import os
import re
import json
from collections import defaultdict
from dataclasses import dataclass, field, asdict
from typing import Dict, List, Optional, Tuple, Set

import codebread
from codebread.export import WEB_DIR

PROJECT_ROOT = os.path.abspath(".")
CODEBREAD_DIR = os.path.join(PROJECT_ROOT, ".codebread")
os.makedirs(CODEBREAD_DIR, exist_ok=True)

print(f"[CoupleFit-CodeBread] Initializing generator for {PROJECT_ROOT}")

# ----------------- Models & Helpers -----------------

def get_layer(rel_path: str) -> str:
    norm = rel_path.replace("\\", "/").lower()
    if norm.startswith("app/src/main/java/com/couplefit/ui/") or "mainactivity" in norm:
        return "frontend"
    if norm.startswith("app/src/main/java/com/couplefit/data/local/"):
        return "database"
    if norm.startswith("app/src/main/java/com/couplefit/di/") or norm.endswith((".gradle.kts", ".xml", ".json", ".properties")):
        return "config"
    if norm.startswith("app/src/main/java/com/couplefit/"):
        return "backend"
    return "config"

def get_language(rel_path: str) -> str:
    norm = rel_path.lower()
    if norm.endswith((".kt", ".kts")):
        return "kotlin"
    if norm.endswith(".xml"):
        return "config"
    if norm.endswith(".json"):
        return "config"
    if norm.endswith(".sql"):
        return "sql"
    if norm.endswith(".md"):
        return "markdown"
    return "config"

def humanize(name: str) -> str:
    s = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", " ", name)
    s = s.replace("_", " ").replace("-", " ").strip()
    s = re.sub(r"\s+", " ", s)
    return (s[:1].upper() + s[1:]) if s else name

# ----------------- Code Parser -----------------

@dataclass
class RawFunction:
    name: str
    params: List[str]
    returns: str
    line: int
    end_line: int
    doc: str
    description: str
    parent_class: str
    kind: str
    code: str
    calls: List[str] = field(default_factory=list)
    api_calls: List[Dict] = field(default_factory=list)
    db_refs: List[Dict] = field(default_factory=list)
    routes: List[Dict] = field(default_factory=list)

@dataclass
class RawTable:
    name: str
    model: str
    fields: List[str]
    line: int
    source: str

@dataclass
class RawFile:
    path: str
    language: str
    layer: str
    loc: int
    imports: List[str]
    functions: List[RawFunction]
    tables: List[RawTable]
    source: str

# Known database tables in CoupleFit
KNOWN_TABLES = {
    "health_metrics": ["id", "metricType", "sourceTimestamp", "rawValue", "normalizedValue", "syncedToRemote"],
    "health_daily_aggregates": ["dateStr", "totalSteps", "totalActiveMinutes", "totalCalories", "averageHeartRate", "averageOxygenSaturation", "lastUpdatedTimestamp"],
    "partner_daily_aggregates": ["id", "partnerId", "dateStr", "totalSteps", "totalActiveMinutes", "totalCalories", "averageHeartRate", "averageOxygenSaturation", "lastUpdatedTimestamp"],
    "partnerdaily": ["id", "partnerId", "dateStr", "steps", "activeMinutes", "calories", "avgHr", "avgSpo2", "lastUpdated"],
    "couplechallenges": ["id", "coupleId", "type", "goal", "start", "end", "status", "myProgress", "partnerProgress", "winnerUserId", "completedAt", "lastUpdated"]
}

def parse_kotlin_file(rel_path: str, content: str) -> RawFile:
    lines = content.splitlines()
    loc = len(lines)
    layer = get_layer(rel_path)
    language = get_language(rel_path)
    
    # Imports
    imports = []
    for line in lines:
        m = re.match(r"^\s*import\s+([\w\.]+)", line)
        if m:
            imports.append(m.group(1))

    # Parse Tables from @Entity
    tables = []
    entity_matches = re.finditer(r'@Entity\s*\(\s*tableName\s*=\s*["\']([^"\']+)["\']\s*\)\s*(?:data\s+class|class)\s+([A-Za-z0-9_]+)\s*\((.*?)\)', content, re.DOTALL)
    for m in entity_matches:
        t_name = m.group(1)
        cls_name = m.group(2)
        body = m.group(3)
        cols = []
        for col_match in re.finditer(r'(?:val|var)\s+([A-Za-z0-9_]+)\s*:\s*([^,\)=]+)', body):
            cols.append(col_match.group(1))
        # line number
        start_char = m.start()
        line_num = content[:start_char].count("\n") + 1
        tables.append(RawTable(
            name=t_name,
            model=cls_name,
            fields=cols if cols else KNOWN_TABLES.get(t_name.lower(), []),
            line=line_num,
            source=rel_path
        ))

    # Parse Functions
    functions = []
    # Regex for fun definitions
    # Captures: doc/annotations, fun keyword, function name, parameters, return type
    pattern = re.compile(
        r'((?:/[*][*].*?[*]/\s*)?(?:@[\w\(\)\"=,\s\.]+\s*)*)' # annotations/doc
        r'(?:(?:override|suspend|private|internal|public|protected|operator|inline|infix)\s+)*'
        r'fun\s+(?:<[^>]+>\s+)?([A-Za-z0-9_]+)\s*\((.*?)\)(?:\s*:\s*([A-Za-z0-9_<>,?\.\s]+))?',
        re.DOTALL
    )

    # Also detect enclosing class names
    class_indices = []
    for m in re.finditer(r'(?:class|object|interface)\s+([A-Za-z0-9_]+)', content):
        class_indices.append((m.start(), m.group(1)))

    def get_parent_class(pos: int) -> str:
        parent = ""
        for cpos, cname in class_indices:
            if cpos < pos:
                parent = cname
            else:
                break
        return parent

    for m in pattern.finditer(content):
        header = m.group(1) or ""
        fn_name = m.group(2)
        raw_params = m.group(3) or ""
        returns = (m.group(4) or "").strip()
        # Clean returns
        if "{" in returns:
            returns = returns.split("{")[0].strip()
        if "=" in returns:
            returns = returns.split("=")[0].strip()
            
        start_pos = m.start()
        line_num = content[:start_pos].count("\n") + 1
        parent_class = get_parent_class(start_pos)
        
        # Clean params
        params = []
        for p in raw_params.split(","):
            p = p.strip()
            if p:
                p_clean = re.sub(r'@[\w\(\)\"=,\s\.]+\s*', '', p)
                params.append(p_clean)

        # Kind: Composable vs Method vs Function
        is_composable = "@Composable" in header
        kind = "method" if parent_class else "function"

        # Extract code snippet (look forward ~30 lines or matching brace)
        snippet_lines = lines[line_num - 1: min(loc, line_num + 35)]
        code_snippet = "\n".join(snippet_lines)
        end_line = min(loc, line_num + len(snippet_lines))

        # Docstring extraction
        doc = ""
        doc_m = re.search(r'/[*][*](.*?)[*]/', header, re.DOTALL)
        if doc_m:
            doc = doc_m.group(1).replace("*", "").strip()

        # Description
        desc_parts = []
        if is_composable:
            desc_parts.append("Composable UI screen/component")
        elif "suspend" in content[max(0, start_pos - 30):start_pos]:
            desc_parts.append("Asynchronous coroutine")
        if doc:
            desc_parts.append(doc.splitlines()[0].strip())
        else:
            desc_parts.append(humanize(fn_name))
        description = " — ".join(desc_parts)

        # Detect calls inside function body
        calls = set()
        for call_m in re.finditer(r'([A-Za-z0-9_]{3,})\s*\(', code_snippet):
            callee = call_m.group(1)
            if callee not in ("if", "for", "while", "when", "launch", "flow", "map", "combine", "run", "let", "also", "apply", "with", "remember", "println", "print", "require", "check", "synchronized", "listOf", "setOf", "mapOf", "mutableListOf", "mutableSetOf", "mutableMapOf", "emptyList", "emptySet", "emptyMap", fn_name):
                calls.add(callee)

        # Detect DB refs
        db_refs = []
        for tbl, cols in KNOWN_TABLES.items():
            if tbl in code_snippet.lower():
                op = "query"
                if "insert" in code_snippet.lower() or "update" in code_snippet.lower():
                    op = "insert"
                elif "delete" in code_snippet.lower():
                    op = "delete"
                db_refs.append({"table": tbl, "op": op})
        
        # Check DAO method names
        if "insertaggregate" in fn_name.lower():
            db_refs.append({"table": "health_daily_aggregates", "op": "insert"})
        elif "insertpartneraggregate" in fn_name.lower():
            db_refs.append({"table": "partner_daily_aggregates", "op": "insert"})
        elif "insertmetrics" in fn_name.lower():
            db_refs.append({"table": "health_metrics", "op": "insert"})
        elif "dailyaggregate" in fn_name.lower():
            db_refs.append({"table": "health_daily_aggregates", "op": "query"})
        elif "partnerdaily" in fn_name.lower():
            db_refs.append({"table": "partner_daily_aggregates", "op": "query"})
        elif "challenge" in fn_name.lower() and ("dao" in rel_path.lower() or "repository" in rel_path.lower()):
            db_refs.append({"table": "couplechallenges", "op": "query"})

        # Detect API calls
        api_calls = []
        if "readrecords" in code_snippet.lower() or "healthconnect" in code_snippet.lower():
            api_calls.append({"method": "GET", "url": "HealthConnect::readRecords", "line": line_num})
        if "firestore" in code_snippet.lower() or "collection(" in code_snippet:
            api_calls.append({"method": "POST" if "set(" in code_snippet or "add(" in code_snippet else "GET", "url": "Firestore::collection", "line": line_num})
        if "firebaseauth" in code_snippet.lower() or "signin" in code_snippet.lower():
            api_calls.append({"method": "POST", "url": "FirebaseAuth::session", "line": line_num})
        if "samsunghealth" in code_snippet.lower() or "healthdatastore" in code_snippet.lower():
            api_calls.append({"method": "GET", "url": "SamsungHealth::DataStore", "line": line_num})

        # Detect Navigation Routes
        routes = []
        if is_composable:
            if "dashboard" in fn_name.lower():
                routes.append({"method": "NAV", "path": "couplefit://dashboard"})
            elif "hub" in fn_name.lower():
                routes.append({"method": "NAV", "path": "couplefit://hub"})
            elif "challenge" in fn_name.lower():
                routes.append({"method": "NAV", "path": "couplefit://challenges"})
            elif "setting" in fn_name.lower() or "privacy" in fn_name.lower():
                routes.append({"method": "NAV", "path": "couplefit://settings"})
            elif "signin" in fn_name.lower():
                routes.append({"method": "NAV", "path": "couplefit://auth/signin"})
            elif "invite" in fn_name.lower():
                routes.append({"method": "NAV", "path": "couplefit://auth/invite"})
            elif "entercode" in fn_name.lower():
                routes.append({"method": "NAV", "path": "couplefit://auth/entercode"})
            elif "trend" in fn_name.lower():
                routes.append({"method": "NAV", "path": "couplefit://trends"})

        functions.append(RawFunction(
            name=fn_name,
            params=params,
            returns=returns,
            line=line_num,
            end_line=end_line,
            doc=doc,
            description=description,
            parent_class=parent_class,
            kind=kind,
            code=code_snippet,
            calls=list(calls),
            api_calls=api_calls,
            db_refs=db_refs,
            routes=routes
        ))

    return RawFile(
        path=rel_path.replace("\\", "/"),
        language=language,
        layer=layer,
        loc=loc,
        imports=imports,
        functions=functions,
        tables=tables,
        source=content
    )

print("[CoupleFit-CodeBread] Loaded models and AST parser")

# ----------------- Project Scanner & Graph Builder -----------------

def scan_project() -> Tuple[List[RawFile], List[RawTable]]:
    raw_files = []
    all_tables = []
    
    # Scan Kotlin files
    for root, dirs, files in os.walk(os.path.join(PROJECT_ROOT, "app", "src", "main", "java")):
        for f in files:
            if f.endswith(".kt"):
                full_path = os.path.join(root, f)
                rel_path = os.path.relpath(full_path, PROJECT_ROOT)
                try:
                    with open(full_path, "r", encoding="utf-8", errors="ignore") as fp:
                        content = fp.read()
                    rf = parse_kotlin_file(rel_path, content)
                    raw_files.append(rf)
                    all_tables.extend(rf.tables)
                except Exception as e:
                    print(f"Error parsing {rel_path}: {e}")

    # Add config files
    config_paths = [
        "app/build.gradle.kts",
        "build.gradle.kts",
        "settings.gradle.kts",
        "app/src/main/AndroidManifest.xml"
    ]
    for cp in config_paths:
        full_p = os.path.join(PROJECT_ROOT, cp.replace("/", os.sep))
        if os.path.exists(full_p):
            try:
                with open(full_p, "r", encoding="utf-8", errors="ignore") as fp:
                    content = fp.read()
                raw_files.append(RawFile(
                    path=cp,
                    language=get_language(cp),
                    layer="config",
                    loc=len(content.splitlines()),
                    imports=[],
                    functions=[],
                    tables=[],
                    source=content
                ))
            except Exception as e:
                print(f"Error reading {cp}: {e}")

    # Ensure all 5 known Room tables exist even if entity regex missed any
    known_table_sources = {
        "health_metrics": "app/src/main/java/com/couplefit/data/local/HealthEntities.kt",
        "health_daily_aggregates": "app/src/main/java/com/couplefit/data/local/HealthEntities.kt",
        "partner_daily_aggregates": "app/src/main/java/com/couplefit/data/local/HealthEntities.kt",
        "partnerdaily": "app/src/main/java/com/couplefit/data/local/HealthEntities.kt",
        "couplechallenges": "app/src/main/java/com/couplefit/data/local/HealthEntities.kt"
    }
    existing_table_names = {t.name.lower() for t in all_tables}
    for tbl_name, cols in KNOWN_TABLES.items():
        if tbl_name.lower() not in existing_table_names:
            all_tables.append(RawTable(
                name=tbl_name,
                model=humanize(tbl_name).replace(" ", "") + "Entity",
                fields=cols,
                line=1,
                source=known_table_sources.get(tbl_name, "app/src/main/java/com/couplefit/data/local/HealthEntities.kt")
            ))

    return raw_files, all_tables

def build_tree(files: List[RawFile]) -> Dict:
    root = {"name": "CoupleFit", "type": "dir", "children": [], "path": ""}
    
    for f in files:
        parts = f.path.split("/")
        curr = root
        curr_path = ""
        for i, part in enumerate(parts):
            curr_path = f"{curr_path}/{part}" if curr_path else part
            if i == len(parts) - 1:
                # File node
                curr["children"].append({
                    "name": part,
                    "type": "file",
                    "path": f.path,
                    "layer": f.layer,
                    "language": f.language,
                    "nFunctions": len(f.functions)
                })
            else:
                # Directory
                found = next((ch for ch in curr["children"] if ch.get("name") == part and ch.get("type") == "dir"), None)
                if not found:
                    found = {"name": part, "type": "dir", "path": curr_path, "children": []}
                    curr["children"].append(found)
                curr = found

    return root

print("[CoupleFit-CodeBread] Scan & tree builders defined")

# ----------------- Graph Construction -----------------

def build_full_graph(raw_files: List[RawFile], raw_tables: List[RawTable]) -> Dict:
    nodes = []
    edges = []
    edge_seen = set()

    def add_edge(src: str, dst: str, kind: str, label: str = ""):
        if src == dst:
            return
        key = (src, dst, kind)
        if key in edge_seen:
            return
        edge_seen.add(key)
        edges.append({"source": src, "target": dst, "kind": kind, "label": label})

    # Track indexes
    fn_nodes_by_key = {} # (file, name) -> nid
    fn_nodes_by_name = defaultdict(list) # name -> list of nids
    file_nodes = set()

    # 1. Add File Nodes
    for f in raw_files:
        file_nodes.add(f.path)
        nodes.append({
            "id": f.path,
            "kind": "file",
            "label": f.path.split("/")[-1],
            "file": f.path,
            "layer": f.layer,
            "language": f.language,
            "loc": f.loc,
            "warnings": [],
            "nFunctions": len(f.functions),
            "imports": f.imports[:30],
            "dbConfig": [],
            "source": f.source,
            "obfuscation": []
        })

        for idx, fn in enumerate(f.functions):
            nid = f"{f.path}::{fn.name}@{fn.line}"
            fn_nodes_by_key[(f.path, fn.name)] = nid
            fn_nodes_by_name[fn.name].append((nid, f, fn))
            nodes.append({
                "id": nid,
                "kind": fn.kind,
                "label": fn.name,
                "file": f.path,
                "layer": f.layer,
                "line": fn.line,
                "endLine": fn.end_line,
                "params": fn.params,
                "returns": fn.returns,
                "description": fn.description,
                "parentClass": fn.parent_class,
                "index": idx,
                "language": f.language,
                "code": fn.code,
                "routes": fn.routes
            })

    # 2. Add Table Nodes
    table_ids = {}
    for t in raw_tables:
        t_key = t.name.lower()
        if t_key not in table_ids:
            nid = f"table::{t_key}"
            table_ids[t_key] = nid
            nodes.append({
                "id": nid,
                "kind": "table",
                "label": t.name,
                "layer": "database",
                "file": t.source,
                "fields": t.fields,
                "model": t.model
            })
            # Connect defining file to table
            if t.source in file_nodes:
                add_edge(t.source, nid, "db", f"defines {t.name}")

    # 3. Add External Cloud / SDK Boundary Nodes
    external_services = [
        {
            "id": "service::health_connect_sdk",
            "label": "Health Connect SDK",
            "layer": "backend",
            "fields": ["StepsRecord", "TotalCaloriesBurnedRecord", "HeartRateRecord", "OxygenSaturationRecord", "ExerciseSessionRecord"],
            "model": "HealthConnectClient"
        },
        {
            "id": "service::samsung_health_sdk",
            "label": "Samsung Health SDK",
            "layer": "backend",
            "fields": ["HealthDataStore", "HeartRateReading", "SpO2Reading", "SensitiveHealthProvider"],
            "model": "HealthDataStore"
        },
        {
            "id": "service::cloud_firestore",
            "label": "Cloud Firestore",
            "layer": "database",
            "fields": ["users/{userId}/sharedView/{dateStr}", "couples/{coupleId}", "invites/{code}"],
            "model": "FirebaseFirestore"
        },
        {
            "id": "service::fcm_messaging",
            "label": "Firebase Cloud Messaging",
            "layer": "backend",
            "fields": ["NudgeNotification", "MilestoneAlert", "PairingInvite"],
            "model": "FirebaseMessaging"
        }
    ]
    for ext in external_services:
        nodes.append({
            "id": ext["id"],
            "kind": "table",
            "label": ext["label"],
            "layer": ext["layer"],
            "file": "External Cloud / Android SDK Boundary",
            "fields": ext["fields"],
            "model": ext["model"]
        })

    # 4. Connect Call Edges (within files and across files)
    for f in raw_files:
        local_fns = {fn.name: fn for fn in f.functions}
        for fn in f.functions:
            src_id = fn_nodes_by_key[(f.path, fn.name)]
            
            for callee in fn.calls:
                # Local call
                if callee in local_fns:
                    dst_id = fn_nodes_by_key[(f.path, callee)]
                    add_edge(src_id, dst_id, "call", f"{fn.name}() → {callee}()")
                    continue
                
                # Cross-file candidate
                candidates = fn_nodes_by_name.get(callee, [])
                if len(candidates) == 1:
                    add_edge(src_id, candidates[0][0], "call", f"{fn.name}() → {callee}()")
                elif len(candidates) > 1:
                    # Match by import or package proximity
                    best = None
                    for cnid, cf, cfn in candidates:
                        if cf.path.split("/")[-2] == f.path.split("/")[-2]:
                            best = cnid
                            break
                    if not best:
                        best = candidates[0][0]
                    add_edge(src_id, best, "call", f"{fn.name}() → {callee}()")

    # 5. Connect Database Edges (DAOs & Repositories -> Tables)
    for f in raw_files:
        for fn in f.functions:
            src_id = fn_nodes_by_key[(f.path, fn.name)]
            for db_ref in fn.db_refs:
                tbl_key = db_ref["table"].lower()
                if tbl_key in table_ids:
                    add_edge(src_id, table_ids[tbl_key], "db", f"{fn.name}() {db_ref['op']} {db_ref['table']}")

    # Connect HealthDatabase -> Tables
    for t_key, t_nid in table_ids.items():
        add_edge("app/src/main/java/com/couplefit/data/local/HealthDatabase.kt", t_nid, "db", f"HealthDatabase persists {t_key}")

    # 6. Connect API Edges (HealthConnect, Samsung Health, Firestore, FCM)
    for f in raw_files:
        for fn in f.functions:
            src_id = fn_nodes_by_key[(f.path, fn.name)]
            for api in fn.api_calls:
                target_service = None
                if "HealthConnect" in api["url"]:
                    target_service = "service::health_connect_sdk"
                elif "SamsungHealth" in api["url"]:
                    target_service = "service::samsung_health_sdk"
                elif "Firestore" in api["url"]:
                    target_service = "service::cloud_firestore"
                elif "FirebaseAuth" in api["url"]:
                    target_service = "service::cloud_firestore"
                
                if target_service:
                    add_edge(src_id, target_service, "api", f"{api['method']} {api['url']}")

    # 7. Connect Dependency Injection (Hilt Modules -> Injected Components)
    di_mappings = [
        ("app/src/main/java/com/couplefit/di/DatabaseModule.kt", "app/src/main/java/com/couplefit/data/local/HealthDatabase.kt", "provides HealthDatabase"),
        ("app/src/main/java/com/couplefit/di/DatabaseModule.kt", "app/src/main/java/com/couplefit/data/local/HealthDao.kt", "provides HealthDao"),
        ("app/src/main/java/com/couplefit/di/FirebaseModule.kt", "service::cloud_firestore", "provides FirebaseFirestore"),
        ("app/src/main/java/com/couplefit/di/FirebaseModule.kt", "service::fcm_messaging", "provides FirebaseMessaging"),
        ("app/src/main/java/com/couplefit/di/RepositoryModule.kt", "app/src/main/java/com/couplefit/data/repository/HealthRepositoryImpl.kt", "binds HealthRepository"),
        ("app/src/main/java/com/couplefit/di/RepositoryModule.kt", "app/src/main/java/com/couplefit/data/repository/PartnerRepositoryImpl.kt", "binds PartnerRepository"),
        ("app/src/main/java/com/couplefit/di/RepositoryModule.kt", "app/src/main/java/com/couplefit/data/repository/ChallengeRepositoryImpl.kt", "binds ChallengeRepository"),
        ("app/src/main/java/com/couplefit/di/RepositoryModule.kt", "app/src/main/java/com/couplefit/data/repository/SyncRepositoryImpl.kt", "binds SyncRepository"),
        ("app/src/main/java/com/couplefit/di/RepositoryModule.kt", "app/src/main/java/com/couplefit/data/samsunghealth/SamsungHealthRepositoryImpl.kt", "binds SamsungHealthRepository"),
        ("app/src/main/java/com/couplefit/di/NetworkModule.kt", "app/src/main/java/com/couplefit/util/NetworkConnectivityObserver.kt", "binds ConnectivityObserver")
    ]
    for src, dst, label in di_mappings:
        if src in file_nodes:
            add_edge(src, dst, "include", label)

    # 8. Structural Pipeline Edges (UI -> ViewModels -> Repositories -> Managers -> DAOs -> DB)
    structural_edges = [
        ("app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt", "app/src/main/java/com/couplefit/ui/healthconnect/HealthConnectViewModel.kt", "call", "observes StateFlow"),
        ("app/src/main/java/com/couplefit/ui/hub/CoupleHubScreen.kt", "app/src/main/java/com/couplefit/ui/hub/CoupleHubViewModel.kt", "call", "observes StateFlow"),
        ("app/src/main/java/com/couplefit/ui/challenge/ChallengesScreen.kt", "app/src/main/java/com/couplefit/ui/challenge/ChallengeViewModel.kt", "call", "observes StateFlow"),
        ("app/src/main/java/com/couplefit/ui/auth/SignInScreen.kt", "app/src/main/java/com/couplefit/ui/auth/AuthViewModel.kt", "call", "submits Auth"),
        ("app/src/main/java/com/couplefit/ui/healthconnect/HealthConnectViewModel.kt", "app/src/main/java/com/couplefit/data/healthconnect/HealthConnectManager.kt", "call", "reads biometrics"),
        ("app/src/main/java/com/couplefit/ui/healthconnect/HealthConnectViewModel.kt", "app/src/main/java/com/couplefit/domain/repository/HealthRepository.kt", "call", "queries daily snapshots"),
        ("app/src/main/java/com/couplefit/ui/healthconnect/HealthConnectViewModel.kt", "app/src/main/java/com/couplefit/data/samsunghealth/SamsungHealthRepository.kt", "call", "queries Samsung Health"),
        ("app/src/main/java/com/couplefit/domain/usecase/GetCoupleDailySnapshotsUseCase.kt", "app/src/main/java/com/couplefit/domain/repository/HealthRepository.kt", "call", "combines snapshots"),
        ("app/src/main/java/com/couplefit/data/repository/HealthRepositoryImpl.kt", "app/src/main/java/com/couplefit/data/local/HealthDao.kt", "call", "Room SSOT queries"),
        ("app/src/main/java/com/couplefit/data/repository/HealthRepositoryImpl.kt", "app/src/main/java/com/couplefit/data/remote/FirebaseSyncManager.kt", "call", "triggers sync"),
        ("app/src/main/java/com/couplefit/data/repository/PartnerRepositoryImpl.kt", "app/src/main/java/com/couplefit/data/local/HealthDao.kt", "call", "Room cache writes"),
        ("app/src/main/java/com/couplefit/data/repository/PartnerRepositoryImpl.kt", "app/src/main/java/com/couplefit/data/remote/FirebaseSyncManager.kt", "call", "observes partner aggregate"),
        ("app/src/main/java/com/couplefit/data/healthconnect/HealthConnectManager.kt", "service::health_connect_sdk", "api", "reads Android Health API"),
        ("app/src/main/java/com/couplefit/data/samsunghealth/SamsungHealthManager.kt", "service::samsung_health_sdk", "api", "reads Samsung Health SDK"),
        ("app/src/main/java/com/couplefit/data/remote/FirebaseSyncManager.kt", "service::cloud_firestore", "api", "sanitized aggregate sync"),
        ("app/src/main/java/com/couplefit/data/remote/CoupleLinkManager.kt", "service::cloud_firestore", "api", "6-digit pairing code exchange"),
        ("app/src/main/java/com/couplefit/service/SyncWorker.kt", "app/src/main/java/com/couplefit/data/remote/FirebaseSyncManager.kt", "call", "WorkManager background flush"),
        ("app/src/main/java/com/couplefit/service/SyncWorker.kt", "app/src/main/java/com/couplefit/data/local/HealthDao.kt", "call", "fetches unsynced metrics"),
        ("app/src/main/java/com/couplefit/service/CoupleFitFirebaseMessagingService.kt", "app/src/main/java/com/couplefit/service/NotificationSystem.kt", "call", "dispatches heads-up alert"),
        ("app/src/main/java/com/couplefit/widget/CoupleFitAppWidget.kt", "app/src/main/java/com/couplefit/data/local/HealthDao.kt", "call", "Glance widget queries Room SSOT")
    ]
    for src, dst, kind, label in structural_edges:
        if (src in file_nodes or src in [n["id"] for n in nodes]) and (dst in file_nodes or dst in [n["id"] for n in nodes]):
            add_edge(src, dst, kind, label)

    # 9. Screen Navigation Links
    nav_links = [
        ("app/src/main/java/com/couplefit/ui/auth/SignInScreen.kt", "app/src/main/java/com/couplefit/ui/auth/InvitePartnerScreen.kt", "nav to invite"),
        ("app/src/main/java/com/couplefit/ui/auth/InvitePartnerScreen.kt", "app/src/main/java/com/couplefit/ui/auth/LinkedConfirmationScreen.kt", "nav on pairing"),
        ("app/src/main/java/com/couplefit/ui/auth/EnterCodeScreen.kt", "app/src/main/java/com/couplefit/ui/auth/LinkedConfirmationScreen.kt", "nav on redeem"),
        ("app/src/main/java/com/couplefit/ui/auth/LinkedConfirmationScreen.kt", "app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt", "nav to dashboard"),
        ("app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt", "app/src/main/java/com/couplefit/ui/hub/CoupleHubScreen.kt", "tab to hub"),
        ("app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt", "app/src/main/java/com/couplefit/ui/challenge/ChallengesScreen.kt", "tab to challenges"),
        ("app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt", "app/src/main/java/com/couplefit/ui/trends/HealthTrendsScreen.kt", "tab to trends"),
        ("app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt", "app/src/main/java/com/couplefit/ui/settings/SettingsScreen.kt", "open settings")
    ]
    for src, dst, label in nav_links:
        if src in file_nodes and dst in file_nodes:
            add_edge(src, dst, "link", label)

    tree = build_tree(raw_files)
    
    n_fns = sum(1 for n in nodes if n["kind"] in ("function", "method"))
    stats = {
        "files": len(raw_files),
        "functions": n_fns,
        "tables": len(table_ids) + len(external_services),
        "connections": len(edges),
        "routes": sum(len(fn.routes) for f in raw_files for fn in f.functions),
        "warnings": 0,
        "orphans": 0,
        "cycles": 0,
        "obfuscated": 0
    }

    # Generate Markdown Context Brief
    context_md = f"""# CoupleFit System Architecture & Tracing Brief

## Overview
**CoupleFit** is a privacy-first fitness companion Android application designed for couples. It combines local biometric tracking (Google Health Connect & Samsung Health SDKs) with offline-first Room persistence and real-time Cloud Firestore synchronization.

### Key Metrics
- **Files**: {stats['files']}
- **Functions & Methods**: {stats['functions']}
- **Database Tables & External Boundaries**: {stats['tables']}
- **System Connections**: {stats['connections']}

---

## 10-Stage API-to-Database Tracing Pipeline
```
1. Sensor Ingestion (Hardware PPG, Accelerometer, SpO2)
   ↓
2. Android Provider SDK (Health Connect / Samsung Health)
   ↓
3. Biometrics Aggregator (HealthConnectManager / SensitiveHealthProvider)
   ↓
4. Room SSOT SQLite Cache (HealthDao -> health_daily_aggregates)
   ↓
5. Background Sync Orchestration (WorkManager SyncWorker)
   ↓
6. Privacy & Metric Sanitization (FirebaseSyncManager -> Strips raw points)
   ↓
7. Cloud API Sync (Cloud Firestore users/{{userId}}/sharedView/{{dateStr}})
   ↓
8. Real-time Partner Ingestion (Firestore snapshot listener)
   ↓
9. Partner Room Cache (HealthDao -> partner_daily_aggregates)
   ↓
10. Reactive UI & Glance Widget (Compose Dashboard, Hub, Glance Home Widget)
```

---

## Architecture Layers & Packages
- **Frontend (`ui/`)**: Jetpack Compose glassmorphic UI, `DashboardScreen`, `CoupleHubScreen`, `ChallengesScreen`, `ActivityRing`, `MotionArcGauge`, and ViewModels.
- **Backend (`data/healthconnect/`, `data/samsunghealth/`, `data/remote/`, `service/`, `widget/`)**:
  - `HealthConnectManager`: Local biometrics aggregation and permission controller.
  - `FirebaseSyncManager`: Strict privacy-respecting sync manager.
  - `CoupleLinkManager`: Cryptographic 6-digit invite code pairing.
  - `SyncWorker`: Periodic WorkManager background flush.
  - `CoupleFitAppWidget`: Glance widget for Android home screen.
- **Database (`data/local/`)**:
  - `HealthDatabase` (Room v4): Local offline single-source-of-truth.
  - `HealthDao`: Reactive Flow queries and transaction-safe sync updates.
  - Tables: `health_metrics`, `health_daily_aggregates`, `partner_daily_aggregates`, `partnerdaily`, `couplechallenges`.
- **Config & DI (`di/`)**:
  - Hilt Singleton modules: `DatabaseModule`, `FirebaseModule`, `RepositoryModule`, `NetworkModule`.
"""

    return {
        "nodes": nodes,
        "edges": edges,
        "tree": tree,
        "warnings": [],
        "cycles": [],
        "stats": stats,
        "meta": {"name": "CoupleFit"},
        "contextMd": context_md
    }

print("[CoupleFit-CodeBread] Graph builder completed")

# ----------------- HTML Template Enrichment & Exporter -----------------

TRACE_AND_DI_HTML = """
<!-- TRACE VIEW -->
<div id="trace" class="hidden">
  <div class="trace-container">
    <header class="view-hero">
      <div class="kicker" style="color: var(--database)">End-to-End Tracing</div>
      <h1>API-to-Database Pipeline</h1>
      <p class="lede">Explore the complete data journey in CoupleFit: from Android hardware sensors through Google Health Connect & Samsung Health SDKs, down to local Room SQLite tables, across the privacy sanitizer, and out to Cloud Firestore.</p>
    </header>

    <div class="trace-selector-bar">
      <button class="trace-pill active" onclick="switchTraceFlow('steps')">👟 Daily Steps & Calories Pipeline</button>
      <button class="trace-pill" onclick="switchTraceFlow('partner')">👥 Real-Time Partner Sync & Nudge</button>
      <button class="trace-pill" onclick="switchTraceFlow('offline')">🔒 Offline-First Local SSOT Fallback</button>
      <button class="trace-pill" onclick="switchTraceFlow('challenges')">🏆 Mutual Challenge Progression</button>
    </div>

    <div id="trace-cards-grid" class="pipeline-flow">
      <!-- Generated dynamically by trace JS -->
    </div>

    <div id="trace-step-detail" class="trace-inspector">
      <div class="inspector-header">
        <span class="step-badge" id="insp-badge">STEP 1</span>
        <h3 id="insp-title">Select any pipeline stage above to inspect its implementation</h3>
      </div>
      <div class="inspector-body">
        <div class="insp-col">
          <div class="kicker">Components & Files</div>
          <div id="insp-files" class="insp-list"></div>
        </div>
        <div class="insp-col">
          <div class="kicker">Contracts & Privacy Controls</div>
          <div id="insp-contracts" class="insp-text"></div>
        </div>
        <div class="insp-col">
          <div class="kicker">Code / SQL / SDK Snippet</div>
          <pre id="insp-code" class="insp-code"><code>// Click any stage in the pipeline above to view details</code></pre>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- DEPENDENCY INJECTION VIEW -->
<div id="di" class="hidden">
  <div class="di-container">
    <header class="view-hero">
      <div class="kicker" style="color: var(--config)">Architecture & Inversion of Control</div>
      <h1>Hilt Dependency Injection Map</h1>
      <p class="lede">Complete mapping of Dagger/Hilt Singleton modules, bound interfaces, provided singletons, and consumer ViewModels across the application.</p>
    </header>

    <div class="di-module-filter">
      <button class="di-pill active" onclick="filterDiModule('all')">All Modules (4)</button>
      <button class="di-pill" onclick="filterDiModule('DatabaseModule')">DatabaseModule</button>
      <button class="di-pill" onclick="filterDiModule('FirebaseModule')">FirebaseModule</button>
      <button class="di-pill" onclick="filterDiModule('RepositoryModule')">RepositoryModule</button>
      <button class="di-pill" onclick="filterDiModule('NetworkModule')">NetworkModule</button>
    </div>

    <div id="di-matrix" class="di-grid">
      <!-- Injected dynamically -->
    </div>
  </div>
</div>
"""

TRACE_AND_DI_CSS = """
/* Custom Views Styling for API-to-DB Tracing & Dependency Map */
.trace-container, .di-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 24px 80px;
}
.view-hero {
  margin-bottom: 24px;
}
.view-hero h1 {
  font-size: 28px;
  margin: 6px 0 10px;
  font-weight: 700;
  letter-spacing: -0.02em;
}
.view-hero .lede {
  color: var(--muted);
  font-size: 14px;
  max-width: 820px;
  line-height: 1.5;
}
.trace-selector-bar, .di-module-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.trace-pill, .di-pill {
  background: var(--bg2);
  border: 1px solid var(--line);
  color: var(--fg);
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.trace-pill:hover, .di-pill:hover {
  border-color: var(--accent);
  background: rgba(45, 212, 191, 0.08);
}
.trace-pill.active, .di-pill.active {
  background: var(--accent);
  color: #041018;
  border-color: var(--accent);
  font-weight: 600;
}
.pipeline-flow {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}
.stage-card {
  background: var(--bg2);
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.stage-card:hover {
  transform: translateY(-2px);
  border-color: var(--accent);
  box-shadow: 0 8px 24px rgba(0,0,0,0.3);
}
.stage-card.selected {
  border-color: var(--accent);
  background: rgba(45, 212, 191, 0.06);
  box-shadow: 0 0 0 2px var(--accent);
}
.stage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.stage-num {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 6px;
  letter-spacing: 0.04em;
  background: rgba(255,255,255,0.08);
}
.stage-layer {
  font-size: 10.5px;
  text-transform: uppercase;
  font-weight: 600;
  letter-spacing: 0.05em;
}
.stage-card h3 {
  font-size: 15px;
  margin: 0 0 8px;
  font-weight: 600;
}
.stage-card p {
  font-size: 12.5px;
  color: var(--muted);
  line-height: 1.4;
  margin: 0 0 12px;
}
.stage-tag {
  font-size: 11px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: var(--accent);
  background: rgba(45, 212, 191, 0.1);
  padding: 4px 8px;
  border-radius: 6px;
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.trace-inspector {
  background: var(--bg2);
  border: 1px solid var(--line);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 12px 36px rgba(0,0,0,0.4);
}
.inspector-header {
  background: rgba(255,255,255,0.03);
  border-bottom: 1px solid var(--line);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.step-badge {
  background: var(--accent);
  color: #041018;
  font-weight: 700;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 6px;
}
.inspector-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}
.inspector-body {
  display: grid;
  grid-template-columns: 280px 320px 1fr;
  gap: 20px;
  padding: 20px;
}
@media (max-width: 1024px) {
  .inspector-body { grid-template-columns: 1fr; }
}
.insp-col .kicker {
  font-size: 11px;
  text-transform: uppercase;
  color: var(--muted);
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: 0.05em;
}
.insp-list a, .insp-list span {
  display: block;
  font-size: 12.5px;
  color: var(--fg);
  padding: 4px 0;
  font-family: ui-monospace, monospace;
}
.insp-text {
  font-size: 13px;
  color: var(--fg);
  line-height: 1.5;
}
.insp-code {
  background: #060e18;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 14px;
  font-size: 12px;
  color: #e2e8f0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  overflow-x: auto;
  max-height: 280px;
  line-height: 1.45;
  margin: 0;
}

/* Dependency Grid */
.di-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}
.di-card {
  background: var(--bg2);
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 20px;
}
.di-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  border-bottom: 1px solid var(--line);
  padding-bottom: 10px;
}
.di-card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}
.di-scope {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px;
  background: rgba(147, 51, 234, 0.15);
  color: #c084fc;
}
.di-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.di-item {
  background: rgba(255,255,255,0.02);
  border: 1px solid rgba(255,255,255,0.05);
  border-radius: 8px;
  padding: 10px;
}
.di-item-head {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 4px;
}
.di-type-pill {
  font-weight: 700;
  font-size: 10px;
  color: var(--accent);
}
.di-binding {
  font-size: 13px;
  font-weight: 600;
  color: var(--fg);
  font-family: ui-monospace, monospace;
}
.di-target {
  font-size: 12px;
  color: var(--muted);
  margin-top: 4px;
}
"""

TRACE_AND_DI_JS = """
// Data definitions for the 4 flows
const TRACE_FLOWS = {
  steps: {
    title: "Daily Steps & Calories Ingestion Pipeline",
    steps: [
      {
        num: "01",
        layer: "Hardware",
        color: "var(--muted)",
        title: "PPG & Motion Sensors",
        summary: "Android hardware step counter and optical heart rate sensor record physical biometric events.",
        tag: "OS Sensor HAL",
        files: ["Android OS Hardware Abstraction Layer"],
        contracts: "Raw hardware pulse intervals and step interrupts batched by vendor sensor hub.",
        code: `// Android Sensor Hardware Event\\nHardware Sensor -> Step Counter / PPG PPG Sensor Hub\\nContinuous low-power sampling without waking CPU`
      },
      {
        num: "02",
        layer: "Provider SDK",
        color: "var(--backend)",
        title: "Health Connect SDK",
        summary: "Google Health Connect Client reads partitioned biometric records with strict read-only permissions.",
        tag: "HealthConnectClient.readRecords()",
        files: ["app/src/main/java/com/couplefit/data/healthconnect/HealthConnectManager.kt"],
        contracts: "Requires HealthPermission.getReadPermission(StepsRecord::class) & TotalCaloriesBurnedRecord::class.",
        code: `val request = ReadRecordsRequest(\\n    recordType = StepsRecord::class,\\n    timeRangeFilter = TimeRangeFilter.between(start, end)\\n)\\nval response = healthConnectClient.readRecords(request)`
      },
      {
        num: "03",
        layer: "Backend",
        color: "var(--backend)",
        title: "HealthConnectManager",
        summary: "Aggregates raw interval steps into daily totals, handles deduplication and timezone normalization.",
        tag: "aggregateToday()",
        files: ["app/src/main/java/com/couplefit/data/healthconnect/HealthConnectManager.kt"],
        contracts: "Aggregates interval records into single HealthDailyAggregateEntity.",
        code: `suspend fun aggregateToday(): AggregatedHealthData {\\n    val now = Instant.now()\\n    val startOfDay = now.truncatedTo(ChronoUnit.DAYS)\\n    val steps = readSteps(startOfDay, now)\\n    val totalSteps = steps.sumOf { it.count }\\n    return AggregatedHealthData(totalSteps = totalSteps, averageHeartRate = avgHr)\\n}`
      },
      {
        num: "04",
        layer: "Database",
        color: "var(--database)",
        title: "HealthDao & Room SQLite",
        summary: "Persists daily aggregate into HealthDatabase (v4) under health_daily_aggregates table as offline SSOT.",
        tag: "HealthDao.insertAggregate()",
        files: [
          "app/src/main/java/com/couplefit/data/local/HealthDao.kt",
          "app/src/main/java/com/couplefit/data/local/HealthDatabase.kt",
          "app/src/main/java/com/couplefit/data/local/HealthEntities.kt"
        ],
        contracts: "OnConflictStrategy.REPLACE. Primary Key: dateStr (YYYY-MM-DD).",
        code: `@Insert(onConflict = OnConflictStrategy.REPLACE)\\nsuspend fun insertAggregate(aggregate: HealthDailyAggregateEntity)\\n\\n@Query("SELECT * FROM health_daily_aggregates WHERE dateStr = :dateStr")\\nfun getDailyAggregateFlow(dateStr: String): Flow<HealthDailyAggregateEntity?>`
      },
      {
        num: "05",
        layer: "Frontend / Sync",
        color: "var(--frontend)",
        title: "User-Driven Foreground Sync & Astryx Refresh",
        summary: "User pull-to-refresh in DashboardScreen triggers AstryxRefreshIndicator with squircle animation, haptic feedback, and force sync.",
        tag: "AstryxRefreshIndicator + checkStatusAndFetchData(force = true)",
        files: [
          "app/src/main/java/com/couplefit/ui/components/AstryxRefreshIndicator.kt",
          "app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt",
          "app/src/main/java/com/couplefit/ui/healthconnect/HealthConnectViewModel.kt"
        ],
        contracts: "Replaces legacy 10s foreground polling loop. 6-hour WorkManager background sync fallback with battery-not-low constraint.",
        code: `AstryxRefreshIndicator(\\n    refreshing = isRefreshing,\\n    state = pullRefreshState,\\n    modifier = Modifier.align(Alignment.TopCenter)\\n)\\n// Triggers healthConnectViewModel.checkStatusAndFetchData(force = true)`
      },
      {
        num: "06",
        layer: "Backend / Sync",
        color: "var(--backend)",
        title: "SyncRepository & Canonical Firestore Upload",
        summary: "SyncRepositoryImpl formats sanitized daily aggregate and writes to canonical couple path with force write bypass.",
        tag: "SyncRepositoryImpl.uploadMyDailyAggregate()",
        files: [
          "app/src/main/java/com/couplefit/data/repository/SyncRepositoryImpl.kt",
          "app/src/main/java/com/couplefit/service/SyncWorker.kt"
        ],
        contracts: "Pushes to couples/{coupleId}/daily/{myUid}_{dateStr} with SetOptions.merge().",
        code: `val docRef = firestore.collection("couples")\\n    .document(coupleId)\\n    .collection("daily")\\n    .document("\${myUid}_\${dateStr}")\\ndocRef.set(payload, SetOptions.merge()).await()`
      },
      {
        num: "07",
        layer: "Backend",
        color: "var(--backend)",
        title: "Privacy Sanitizer & SharedPreferences Cache",
        summary: "FirebaseSyncManager strips raw vitals; session credentials cached in couplefit_session_cache for zero-latency resume.",
        tag: "prepareDailyAggregatePayload()",
        files: [
          "app/src/main/java/com/couplefit/data/remote/FirebaseSyncManager.kt",
          "app/src/main/java/com/couplefit/data/remote/FirebaseAuthManager.kt"
        ],
        contracts: "STRICT PRIVACY: Zero raw timestamps or PPG vitals. Cached coupleId & partnerId prevent route flashing.",
        code: `sessionPrefs.edit().apply {\\n    putString("cached_couple_id", coupleId)\\n    putString("cached_partner_id", partnerId)\\n    apply()\\n}`
      },
      {
        num: "08",
        layer: "Frontend",
        color: "var(--frontend)",
        title: "Reactive Compose Dashboard & Gauges",
        summary: "HealthConnectViewModel collects Room Flow and streams StateFlow directly to DashboardScreen bilateral gauges.",
        tag: "StateFlow<HealthDashboardUiState>",
        files: [
          "app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt",
          "app/src/main/java/com/couplefit/ui/healthconnect/HealthConnectViewModel.kt",
          "app/src/main/java/com/couplefit/ui/components/ActivityRing.kt"
        ],
        contracts: "WCAG 2.2 Level AA compliance, 44x44dp touch targets, TalkBack accessibility bounds.",
        code: `@Composable\\nfun DashboardScreen(viewModel: HealthConnectViewModel = hiltViewModel()) {\\n    val uiState by viewModel.dashboardState.collectAsStateWithLifecycle()\\n    ActivityRing(steps = uiState.userSteps, goal = 10000)\\n}`
      }
    ]
  },
  partner: {
    title: "Real-Time Partner Sync & Nudge Pipeline",
    steps: [
      {
        num: "01",
        layer: "Cloud",
        color: "var(--database)",
        title: "Canonical Firestore Couple Daily Document",
        summary: "Sync pipeline reads and listens to canonical document couples/{coupleId}/daily/{partnerId}_{dateStr}.",
        tag: "couples/{coupleId}/daily/{partnerId}_{dateStr}",
        files: [
          "app/src/main/java/com/couplefit/data/repository/PartnerRepositoryImpl.kt",
          "app/src/main/java/com/couplefit/data/repository/SyncRepositoryImpl.kt"
        ],
        contracts: "Fallback to dateStr minusDays(1) if partner has not yet logged telemetry for today.",
        code: `val docRef = firestore.collection("couples")\\n    .document(coupleId)\\n    .collection("daily")\\n    .document("\${partnerId}_\${dateStr}")`
      },
      {
        num: "02",
        layer: "Backend / Room",
        color: "var(--backend)",
        title: "PartnerRepository & Room SSOT Cache",
        summary: "PartnerRepositoryImpl maps Firestore telemetry to PartnerDailyEntity and caches into Room partnerDaily table.",
        tag: "HealthDao.insertPartnerDaily()",
        files: [
          "app/src/main/java/com/couplefit/data/repository/PartnerRepositoryImpl.kt",
          "app/src/main/java/com/couplefit/data/local/HealthDao.kt",
          "app/src/main/java/com/couplefit/data/local/HealthEntities.kt"
        ],
        contracts: "Persists into partnerDaily table with OnConflictStrategy.REPLACE. Room SSOT drives UI Flow.",
        code: `healthDao.insertPartnerDaily(partnerDaily)\\n// Emitted via healthDao.getLatestPartnerDailyFlow(partnerId)`
      },
      {
        num: "03",
        layer: "Messaging",
        color: "var(--backend)",
        title: "FCM Partner Nudge",
        summary: "Firebase Cloud Messaging service receives instant partner workout cheers, nudges, and alerts.",
        tag: "onMessageReceived()",
        files: [
          "app/src/main/java/com/couplefit/service/CoupleFitFirebaseMessagingService.kt",
          "app/src/main/java/com/couplefit/service/NotificationSystem.kt"
        ],
        contracts: "POST payload with type: 'PARTNER_NUDGE' | 'MILESTONE_ALERT'.",
        code: `override fun onMessageReceived(remoteMessage: RemoteMessage) {\\n    notificationSystem.showPartnerNudgeNotification(\\n        title = remoteMessage.data["title"],\\n        message = remoteMessage.data["body"]\\n    )\\n}`
      },
      {
        num: "04",
        layer: "Frontend",
        color: "var(--frontend)",
        title: "CoupleHub & Glance Widget",
        summary: "CoupleHubScreen displays partner vital comparison, and CoupleFitAppWidget displays Glance home widget.",
        tag: "CoupleHubScreen + Glance",
        files: [
          "app/src/main/java/com/couplefit/ui/hub/CoupleHubScreen.kt",
          "app/src/main/java/com/couplefit/widget/CoupleFitAppWidget.kt"
        ],
        contracts: "Glance widget provides offline home screen sync with zero network calls.",
        code: `class CoupleFitAppWidget : GlanceAppWidget() {\\n    override suspend fun provideGlance(context: Context, id: GlanceId) {\\n        val data = healthDao.getDailyAggregate(today)\\n        provideContent { WidgetContent(data) }\\n    }\\n}`
      }
    ]
  },
  offline: {
    title: "Offline-First Local SSOT Fallback",
    steps: [
      {
        num: "01",
        layer: "Database",
        color: "var(--database)",
        title: "Local Room Database (v4)",
        summary: "HealthDatabase holds 5 local SQLite tables. All UI screens read exclusively from Room via Flow.",
        tag: "Room SSOT",
        files: ["app/src/main/java/com/couplefit/data/local/HealthDatabase.kt"],
        contracts: "Migrations 1->2, 2->3, 3->4 with fallbackToDestructiveMigration().",
        code: `@Database(entities = [HealthMetricEntity::class, HealthDailyAggregateEntity::class, ...], version = 4)`
      },
      {
        num: "02",
        layer: "Network",
        color: "var(--config)",
        title: "Connectivity Observer",
        summary: "Monitors Android ConnectivityManager. When network drops, all operations run on local SQLite cache.",
        tag: "NetworkConnectivityObserver",
        files: ["app/src/main/java/com/couplefit/util/NetworkConnectivityObserver.kt"],
        contracts: "Emits Status.Available / Status.Lost / Status.Losing.",
        code: `class NetworkConnectivityObserver(context: Context) : ConnectivityObserver {\\n    override fun observe(): Flow<Status> = callbackFlow { ... }\\n}`
      },
      {
        num: "03",
        layer: "Backend",
        color: "var(--backend)",
        title: "Sync Queue Flush",
        summary: "When network restores, unsynced metrics are retrieved from Room (syncedToRemote = 0) and flushed.",
        tag: "HealthDao.getUnsyncedMetrics()",
        files: [
          "app/src/main/java/com/couplefit/data/local/HealthDao.kt",
          "app/src/main/java/com/couplefit/service/SyncWorker.kt"
        ],
        contracts: "Atomic transaction: updates syncedToRemote = 1 only after confirmed Firestore commit.",
        code: `@Transaction\\nsuspend fun updateLocalAfterRemoteSync(syncedIds: List<String>) {\\n    markAsSynced(syncedIds)\\n}`
      }
    ]
  },
  challenges: {
    title: "Mutual Challenge Progression",
    steps: [
      {
        num: "01",
        layer: "Database",
        color: "var(--database)",
        title: "CoupleChallenges Table",
        summary: "Room table storing active challenge goals (steps race, active minutes streak) and current progression.",
        tag: "coupleChallenges",
        files: ["app/src/main/java/com/couplefit/data/local/HealthEntities.kt"],
        contracts: "status: 'ACTIVE' | 'COMPLETED' | 'EXPIRED'.",
        code: `data class CoupleChallengeEntity(val coupleId: String, val goal: Long, val myProgress: Long, val partnerProgress: Long)`
      },
      {
        num: "02",
        layer: "Backend",
        color: "var(--backend)",
        title: "ChallengeRepository",
        summary: "Updates mutual challenge progress from incoming biometric daily aggregates and evaluates winners.",
        tag: "evaluateChallengeStatus()",
        files: ["app/src/main/java/com/couplefit/data/repository/ChallengeRepositoryImpl.kt"],
        contracts: "Calculates milestone completion timestamp and determines winnerUserId.",
        code: `if (myProgress >= challenge.goal) completeChallenge(winnerUserId = myUserId)`
      },
      {
        num: "03",
        layer: "Frontend",
        color: "var(--frontend)",
        title: "Confetti Celebration Screen",
        summary: "Renders mutual celebration animation when either partner achieves the challenge milestone.",
        tag: "ConfettiCelebration.kt",
        files: [
          "app/src/main/java/com/couplefit/ui/challenge/ChallengesScreen.kt",
          "app/src/main/java/com/couplefit/ui/components/ConfettiCelebration.kt"
        ],
        contracts: "Physics-based particle engine with haptic feedback.",
        code: `ConfettiCelebration(visible = challengeCompleted)`
      }
    ]
  }
};

const DI_MODULES = [
  {
    name: "DatabaseModule",
    scope: "@Singleton",
    file: "app/src/main/java/com/couplefit/di/DatabaseModule.kt",
    items: [
      { type: "@Provides", binding: "HealthDatabase", target: "Room.databaseBuilder(context, HealthDatabase, 'couplefit_health.db')" },
      { type: "@Provides", binding: "HealthDao", target: "database.healthDao()" }
    ]
  },
  {
    name: "FirebaseModule",
    scope: "@Singleton",
    file: "app/src/main/java/com/couplefit/di/FirebaseModule.kt",
    items: [
      { type: "@Provides", binding: "FirebaseFirestore", target: "FirebaseFirestore.getInstance()" },
      { type: "@Provides", binding: "FirebaseAuth", target: "FirebaseAuth.getInstance()" },
      { type: "@Provides", binding: "FirebaseFunctions", target: "FirebaseFunctions.getInstance()" },
      { type: "@Provides", binding: "FirebaseAnalytics", target: "FirebaseAnalytics.getInstance(context)" },
      { type: "@Provides", binding: "FirebaseCrashlytics", target: "FirebaseCrashlytics.getInstance()" },
      { type: "@Provides", binding: "FirebaseRemoteConfig", target: "FirebaseRemoteConfig.getInstance()" }
    ]
  },
  {
    name: "RepositoryModule",
    scope: "@Singleton",
    file: "app/src/main/java/com/couplefit/di/RepositoryModule.kt",
    items: [
      { type: "@Binds", binding: "HealthRepository", target: "HealthRepositoryImpl" },
      { type: "@Binds", binding: "SamsungHealthRepository", target: "SamsungHealthRepositoryImpl" },
      { type: "@Binds", binding: "PartnerRepository", target: "PartnerRepositoryImpl" },
      { type: "@Binds", binding: "SyncRepository", target: "SyncRepositoryImpl" },
      { type: "@Binds", binding: "ChallengeRepository", target: "ChallengeRepositoryImpl" }
    ]
  },
  {
    name: "NetworkModule",
    scope: "@Singleton",
    file: "app/src/main/java/com/couplefit/di/NetworkModule.kt",
    items: [
      { type: "@Binds", binding: "ConnectivityObserver", target: "NetworkConnectivityObserver" }
    ]
  }
];

let currentFlow = "steps";
let currentStepIdx = 0;

function switchTraceFlow(flowKey) {
  currentFlow = flowKey;
  currentStepIdx = 0;
  document.querySelectorAll(".trace-pill").forEach(p => p.classList.remove("active"));
  event?.target?.classList.add("active");
  renderTraceCards();
  inspectStep(0);
}

function renderTraceCards() {
  const flow = TRACE_FLOWS[currentFlow];
  const grid = document.getElementById("trace-cards-grid");
  if (!grid || !flow) return;
  grid.innerHTML = flow.steps.map((s, idx) => `
    <div class="stage-card ${idx === currentStepIdx ? 'selected' : ''}" onclick="inspectStep(${idx})">
      <div>
        <div class="stage-header">
          <span class="stage-num">${s.num}</span>
          <span class="stage-layer" style="color: ${s.color}">${s.layer}</span>
        </div>
        <h3>${s.title}</h3>
        <p>${s.summary}</p>
      </div>
      <div>
        <div class="stage-tag">${s.tag}</div>
      </div>
    </div>
  `).join("");
}

function inspectStep(idx) {
  currentStepIdx = idx;
  const flow = TRACE_FLOWS[currentFlow];
  if (!flow || !flow.steps[idx]) return;
  const s = flow.steps[idx];

  document.querySelectorAll(".stage-card").forEach((c, i) => {
    c.classList.toggle("selected", i === idx);
  });

  document.getElementById("insp-badge").textContent = `STEP ${s.num} · ${s.layer.toUpperCase()}`;
  document.getElementById("insp-title").textContent = s.title;

  document.getElementById("insp-files").innerHTML = s.files.map(f => `
    <span>${f}</span>
  `).join("");

  document.getElementById("insp-contracts").textContent = s.contracts;
  document.getElementById("insp-code").innerHTML = `<code>${escapeHtml(s.code)}</code>`;
}

function filterDiModule(modName) {
  document.querySelectorAll(".di-pill").forEach(p => p.classList.remove("active"));
  event?.target?.classList.add("active");

  const matrix = document.getElementById("di-matrix");
  if (!matrix) return;
  const filtered = modName === "all" ? DI_MODULES : DI_MODULES.filter(m => m.name === modName);

  matrix.innerHTML = filtered.map(m => `
    <div class="di-card">
      <div class="di-card-header">
        <div>
          <h3>${m.name}</h3>
          <small style="color: var(--muted); font-family: monospace;">${m.file}</small>
        </div>
        <span class="di-scope">${m.scope}</span>
      </div>
      <div class="di-items">
        ${m.items.map(it => `
          <div class="di-item">
            <div class="di-item-head">
              <span class="di-type-pill">${it.type}</span>
              <span class="di-binding">${it.binding}</span>
            </div>
            <div class="di-target">↳ ${it.target}</div>
          </div>
        `).join("")}
      </div>
    </div>
  `).join("");
}

function escapeHtml(str) {
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

// Hook into CodeBread view switching
window.addEventListener("DOMContentLoaded", () => {
  const originalSetView = window.setView;
  
  // Add buttons to nav
  const nav = document.getElementById("view-mode");
  if (nav) {
    const traceBtn = document.createElement("button");
    traceBtn.dataset.view = "trace";
    traceBtn.textContent = "API-to-DB Tracing";
    nav.appendChild(traceBtn);

    const diBtn = document.createElement("button");
    diBtn.dataset.view = "di";
    diBtn.textContent = "Dependency Map";
    nav.appendChild(diBtn);

    // Re-bind click handlers
    nav.querySelectorAll("button").forEach(btn => {
      btn.onclick = () => {
        const v = btn.dataset.view;
        nav.querySelectorAll("button").forEach(b => b.classList.toggle("active", b === btn));
        
        document.getElementById("overview").classList.toggle("hidden", v !== "overview");
        document.getElementById("connect").classList.toggle("hidden", v !== "connect");
        document.getElementById("canvas-wrap").classList.toggle("hidden", v !== "chart");
        document.getElementById("ide").classList.toggle("hidden", v !== "ide");
        document.getElementById("sidebar").classList.toggle("hidden", v === "overview" || v === "connect" || v === "trace" || v === "di");
        document.getElementById("detail").classList.add("hidden");

        const traceEl = document.getElementById("trace");
        const diEl = document.getElementById("di");
        if (traceEl) traceEl.classList.toggle("hidden", v !== "trace");
        if (diEl) diEl.classList.toggle("hidden", v !== "di");

        if (v === "trace") {
          renderTraceCards();
          inspectStep(0);
        } else if (v === "di") {
          filterDiModule("all");
        }
      };
    });
  }
});
"""

def generate_codebread_bundle():
    print("[CoupleFit-CodeBread] Scanning codebase...")
    raw_files, raw_tables = scan_project()
    print(f"[CoupleFit-CodeBread] Found {len(raw_files)} files, {len(raw_tables)} tables.")
    
    print("[CoupleFit-CodeBread] Building graph connections...")
    graph = build_full_graph(raw_files, raw_tables)
    print(f"[CoupleFit-CodeBread] Graph built: {graph['stats']['files']} files, {graph['stats']['functions']} functions, {graph['stats']['tables']} tables/services, {graph['stats']['connections']} edges.")

    # Export graph.json
    json_path = os.path.join(CODEBREAD_DIR, "graph.json")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(graph, f, ensure_ascii=False, indent=1)
    print(f"[CoupleFit-CodeBread] Wrote {json_path}")

    # Export CODEBREAD.md
    md_path = os.path.join(CODEBREAD_DIR, "CODEBREAD.md")
    with open(md_path, "w", encoding="utf-8") as f:
        f.write(graph["contextMd"])
    print(f"[CoupleFit-CodeBread] Wrote {md_path}")

    # Read CodeBread Web Assets
    with open(os.path.join(WEB_DIR, "index.html"), encoding="utf-8") as f:
        html = f.read()
    with open(os.path.join(WEB_DIR, "style.css"), encoding="utf-8") as f:
        css = f.read()
    with open(os.path.join(WEB_DIR, "app.js"), encoding="utf-8") as f:
        js = f.read()

    # Inject custom views into index.html
    html = html.replace('<div id="overview" class="hidden">', TRACE_AND_DI_HTML + '\n    <div id="overview" class="hidden">')

    # Inject CSS
    augmented_css = css + "\n\n" + TRACE_AND_DI_CSS

    # Inject JS
    augmented_js = js + "\n\n" + TRACE_AND_DI_JS

    data = json.dumps(graph, ensure_ascii=False)
    data = data.replace("</", "<\\/")

    final_html = html.replace(
        '<link rel="stylesheet" href="style.css">',
        f"<style>\n{augmented_css}\n</style>"
    )
    final_html = final_html.replace(
        '<script src="app.js"></script>',
        f"<script>window.CODEBREAD_DATA = {data};</script>\n<script>\n{augmented_js}\n</script>"
    )

    # Write out primary deliverables
    primary_html_path = os.path.join(CODEBREAD_DIR, "index.html")
    with open(primary_html_path, "w", encoding="utf-8") as f:
        f.write(final_html)
    print(f"[CoupleFit-CodeBread] Wrote primary HTML app to {primary_html_path}")

    alias_html_path = os.path.join(CODEBREAD_DIR, "couplefit-codebread-map.html")
    with open(alias_html_path, "w", encoding="utf-8") as f:
        f.write(final_html)
    print(f"[CoupleFit-CodeBread] Wrote alias HTML app to {alias_html_path}")

    all_in_one_path = os.path.join(CODEBREAD_DIR, "couplefit-all-in-one.html")
    with open(all_in_one_path, "w", encoding="utf-8") as f:
        f.write(final_html)
    print(f"[CoupleFit-CodeBread] Wrote all-in-one HTML app to {all_in_one_path}")

if __name__ == "__main__":
    generate_codebread_bundle()
    print("[CoupleFit-CodeBread] All deliverables successfully generated!")
