#!/usr/bin/env python3
"""
Script to fix name tags in POM files for Maven Central
"""
import sys
import os
import re

def fix_pom_file(pom_file):
    """Fix name tags in a POM file"""
    if not os.path.exists(pom_file):
        print(f"File not found: {pom_file}")
        return False
    
    print(f"Fixing POM file: {pom_file}")
    
    # Read the file
    with open(pom_file, 'r') as f:
        content = f.read()
    
    # Replace <n> with <name> and </n> with </name>
    content = content.replace('<n>', '<name>')
    content = content.replace('</n>', '</name>')
    
    # Write the file
    with open(pom_file, 'w') as f:
        f.write(content)
    
    # Verify the fix
    name_tags = []
    with open(pom_file, 'r') as f:
        for i, line in enumerate(f):
            if '<name>' in line:
                name_tags.append((i+1, line.strip()))
    
    if name_tags:
        print(f"Fixed name tags in: {pom_file}")
        for i, line in name_tags:
            print(f"{i}: {line}")
    else:
        print(f"No name tags found in: {pom_file}")
    
    return True

def main():
    """Main entry point"""
    # Find the version in build.gradle
    version = None
    with open('build.gradle', 'r') as f:
        for line in f:
            if '"sdkVersionName"' in line:
                match = re.search(r'"([^"]*)"$', line)
                if match:
                    version = match.group(1)
                    break
    
    if not version:
        print("Could not find version in build.gradle")
        return 1
    
    print(f"Using version: {version}")
    
    # Fix the Gradle-generated POM
    gradle_pom = "library/build/publications/release/pom-default.xml"
    if os.path.exists(gradle_pom):
        if fix_pom_file(gradle_pom):
            # Copy to build/pom.xml for reference
            os.system(f"cp {gradle_pom} library/build/pom.xml")
            print(f"Copied fixed POM to: library/build/pom.xml")
    
    # Fix the Maven local repository POM
    maven_pom = f"{os.path.expanduser('~')}/.m2/repository/com/paypal/messages/paypal-messages/{version}/paypal-messages-{version}.pom"
    if os.path.exists(maven_pom):
        fix_pom_file(maven_pom)
    
    return 0

if __name__ == "__main__":
    sys.exit(main())