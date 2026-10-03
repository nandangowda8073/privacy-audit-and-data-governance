import os
import re

replacements = {
    r"SchedulerController\.java": [
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER', 'AUDITOR'\)\)\"\s*\n\s*public ResponseEntity.*getAllSchedules", r'@PreAuthorize("hasAuthority(\'SCAN_VIEW\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasRole\('SUPER_ADMIN'\)\)\"\s*\n\s*public ResponseEntity.*createSchedule", r'@PreAuthorize("hasAuthority(\'SCAN_SCHEDULE\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasRole\('SUPER_ADMIN'\)\)\"\s*\n\s*public ResponseEntity.*updateSchedule", r'@PreAuthorize("hasAuthority(\'SCAN_SCHEDULE\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER'\)\)\"\s*\n\s*public ResponseEntity.*deleteSchedule", r'@PreAuthorize("hasAuthority(\'SCAN_SCHEDULE\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER', 'AUDITOR'\)\)\"\s*\n\s*public ResponseEntity.*getScanHistory", r'@PreAuthorize("hasAuthority(\'SCAN_VIEW\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER'\)\)\"\s*\n\s*public ResponseEntity.*startScan", r'@PreAuthorize("hasAuthority(\'SCAN_START\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasRole\('SUPER_ADMIN'\)\)\"\s*\n\s*public ResponseEntity.*cancelScan", r'@PreAuthorize("hasAuthority(\'SCAN_CANCEL\')")\n    public ResponseEntity'),
    ],
    r"RiskAssessmentController\.java": [
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER', 'AUDITOR'\)\)\"", r'@PreAuthorize("hasAuthority(\'RISK_VIEW\')")')
    ],
    r"PiiDetectionController\.java": [
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER'\)\)\"\s*\n\s*public ResponseEntity.*detectFromText", r'@PreAuthorize("hasAuthority(\'SCAN_START\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER'\)\)\"\s*\n\s*public ResponseEntity.*detectFromFile", r'@PreAuthorize("hasAuthority(\'SCAN_START\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER', 'AUDITOR'\)\)\"\s*\n\s*public ResponseEntity.*getHistory", r'@PreAuthorize("hasAuthority(\'PII_VIEW\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER', 'AUDITOR'\)\)\"\s*\n\s*public ResponseEntity.*getStats", r'@PreAuthorize("hasAuthority(\'PII_VIEW\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER', 'AUDITOR'\)\)\"\s*\n\s*public ResponseEntity.*getCategories", r'@PreAuthorize("hasAuthority(\'PII_VIEW\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER', 'AUDITOR'\)\)\"\s*\n\s*public ResponseEntity.*exportReport", r'@PreAuthorize("hasAuthority(\'PII_EXPORT\')")\n    public ResponseEntity'),
    ],
    r"CustomPatternController\.java": [
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER', 'AUDITOR'\)\)\"", r'@PreAuthorize("hasAuthority(\'SCAN_VIEW\')")'),
        (r"@PreAuthorize\(\"hasRole\('SUPER_ADMIN'\)\)\"", r'@PreAuthorize("hasAuthority(\'SCAN_START\')")'),
    ],
    r"DatabaseController\.java": [
        (r"@PreAuthorize\(\"hasRole\('SUPER_ADMIN'\)\)\"\s*\n\s*public ResponseEntity.*addDatabase", r'@PreAuthorize("hasAuthority(\'DATABASE_CREATE\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER', 'AUDITOR'\)\)\"\s*\n\s*public ResponseEntity.*getAllDatabases", r'@PreAuthorize("hasAuthority(\'DATABASE_VIEW\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER', 'AUDITOR'\)\)\"\s*\n\s*public ResponseEntity.*getDatabase", r'@PreAuthorize("hasAuthority(\'DATABASE_VIEW\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasRole\('SUPER_ADMIN'\)\)\"\s*\n\s*public ResponseEntity.*updateDatabase", r'@PreAuthorize("hasAuthority(\'DATABASE_UPDATE\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasRole\('SUPER_ADMIN'\)\)\"\s*\n\s*public ResponseEntity.*deleteDatabase", r'@PreAuthorize("hasAuthority(\'DATABASE_DELETE\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER'\)\)\"\s*\n\s*public ResponseEntity.*testConnection", r'@PreAuthorize("hasAuthority(\'DATABASE_TEST_CONNECTION\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasRole\('SUPER_ADMIN'\)\)\"\s*\n\s*public ResponseEntity.*connect", r'@PreAuthorize("hasAuthority(\'DATABASE_TEST_CONNECTION\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasRole\('SUPER_ADMIN'\)\)\"\s*\n\s*public ResponseEntity.*getTables", r'@PreAuthorize("hasAuthority(\'DATABASE_VIEW\')")\n    public ResponseEntity'),
    ],
    r"MaskingPolicyController\.java": [
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER', 'AUDITOR'\)\)\"", r'@PreAuthorize("hasAuthority(\'MASKING_POLICY_VIEW\')")'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER'\)\)\"\s*\n\s*public ResponseEntity.*createPolicy", r'@PreAuthorize("hasAuthority(\'MASKING_POLICY_CREATE\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER'\)\)\"\s*\n\s*public ResponseEntity.*updatePolicy", r'@PreAuthorize("hasAuthority(\'MASKING_POLICY_UPDATE\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER'\)\)\"\s*\n\s*public ResponseEntity.*deletePolicy", r'@PreAuthorize("hasAuthority(\'MASKING_POLICY_DELETE\')")\n    public ResponseEntity'),
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER'\)\)\"\s*\n\s*public ResponseEntity.*previewMasking", r'@PreAuthorize("hasAuthority(\'MASKING_POLICY_VIEW\')")\n    public ResponseEntity'),
    ],
    r"UserController\.java": [
        (r"@PreAuthorize\(\"hasAnyRole\('SUPER_ADMIN', 'SECURITY_OFFICER'\)\)\"", r'@PreAuthorize("hasAuthority(\'USER_VIEW\')")')
    ],
}

def process_file(filepath):
    filename = os.path.basename(filepath)
    content = ""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    modified = False
    for pattern_file, reps in replacements.items():
        if re.search(pattern_file, filename):
            for pattern, repl in reps:
                new_content = re.sub(pattern, repl, content)
                if new_content != content:
                    content = new_content
                    modified = True
    
    if modified:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filename}")

def find_and_process():
    base_dir = r"e:\audit pii\backend\src\main\java\com\piiscan"
    for root, dirs, files in os.walk(base_dir):
        for f in files:
            if f.endswith('.java'):
                process_file(os.path.join(root, f))

find_and_process()
