import os

replacements = {
    "SchedulerController.java": [
        ('@PreAuthorize("hasAnyRole(\'SUPER_ADMIN\', \'SECURITY_OFFICER\', \'AUDITOR\')")', '@PreAuthorize("hasAuthority(\'SCAN_VIEW\')")'),
        ('@PreAuthorize("hasRole(\'SUPER_ADMIN\')")', '@PreAuthorize("hasAuthority(\'SCAN_SCHEDULE\')")'),
        ('@PreAuthorize("hasAnyRole(\'SUPER_ADMIN\', \'SECURITY_OFFICER\')")', '@PreAuthorize("hasAuthority(\'SCAN_START\')")'),
    ],
    "RiskAssessmentController.java": [
        ('@PreAuthorize("hasAnyRole(\'SUPER_ADMIN\', \'SECURITY_OFFICER\', \'AUDITOR\')")', '@PreAuthorize("hasAuthority(\'RISK_VIEW\')")'),
    ],
    "PiiDetectionController.java": [
        ('@PreAuthorize("hasAnyRole(\'SUPER_ADMIN\', \'SECURITY_OFFICER\')")', '@PreAuthorize("hasAuthority(\'SCAN_START\')")'),
        ('@PreAuthorize("hasAnyRole(\'SUPER_ADMIN\', \'SECURITY_OFFICER\', \'AUDITOR\')")', '@PreAuthorize("hasAuthority(\'PII_VIEW\')")'),
    ],
    "CustomPatternController.java": [
        ('@PreAuthorize("hasAnyRole(\'SUPER_ADMIN\', \'SECURITY_OFFICER\', \'AUDITOR\')")', '@PreAuthorize("hasAuthority(\'SCAN_VIEW\')")'),
        ('@PreAuthorize("hasRole(\'SUPER_ADMIN\')")', '@PreAuthorize("hasAuthority(\'SCAN_START\')")'),
    ],
    "DatabaseController.java": [
        ('@PreAuthorize("hasAnyRole(\'SUPER_ADMIN\', \'SECURITY_OFFICER\', \'AUDITOR\')")', '@PreAuthorize("hasAuthority(\'DATABASE_VIEW\')")'),
        ('@PreAuthorize("hasAnyRole(\'SUPER_ADMIN\', \'SECURITY_OFFICER\')")', '@PreAuthorize("hasAuthority(\'DATABASE_TEST_CONNECTION\')")'),
    ],
    "MaskingPolicyController.java": [
        ('@PreAuthorize("hasAnyRole(\'SUPER_ADMIN\', \'SECURITY_OFFICER\', \'AUDITOR\')")', '@PreAuthorize("hasAuthority(\'MASKING_POLICY_VIEW\')")'),
    ],
    "UserController.java": [
        ('@PreAuthorize("hasAnyRole(\'SUPER_ADMIN\', \'SECURITY_OFFICER\')")', '@PreAuthorize("hasAuthority(\'USER_VIEW\')")')
    ]
}

def process_file(filepath):
    filename = os.path.basename(filepath)
    if filename not in replacements:
        return
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    modified = False
    for old, new in replacements[filename]:
        # for DatabaseController and MaskingPolicyController we have multiple different replacements needed
        # this simple string replace will globally replace.
        if old in content:
            content = content.replace(old, new)
            modified = True
            
    if modified:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filename}")

base_dir = r"e:\audit pii\backend\src\main\java\com\piiscan"
for root, dirs, files in os.walk(base_dir):
    for f in files:
        if f.endswith('.java'):
            process_file(os.path.join(root, f))

