# Managing AI Code Review Across Multiple Repositories

This guide explains how to manage the AI code review workflow across multiple repositories efficiently.

## 📋 Table of Contents

1. [Approach 1: Reusable Workflows (Recommended)](#approach-1-reusable-workflows-recommended)
2. [Approach 2: Repository Templates](#approach-2-repository-templates)
3. [Approach 3: Automation Scripts](#approach-3-automation-scripts)
4. [Approach 4: GitHub App](#approach-4-github-app)
5. [Comparison & Recommendations](#comparison--recommendations)

---

## Approach 1: Reusable Workflows (Recommended)

**Best for:** Organizations with centralized governance and consistent review requirements

### How It Works

1. **Create a central repository** to host the reusable workflow
2. **Other repositories call** the central workflow
3. **Updates happen once** in the central repo and apply everywhere

### Step 1: Create Central Workflow Repository

Create a repository like `org-name/github-workflows` with this structure:

```
github-workflows/
├── .github/
│   └── workflows/
│       └── ai-code-review-reusable.yml  # The reusable workflow
├── configs/
│   ├── default-config.yml               # Default configuration
│   └── python-config.yml                # Python-specific config
└── README.md
```

### Step 2: Create Reusable Workflow

**File:** `.github/workflows/ai-code-review-reusable.yml`

```yaml
name: AI Code Review (Reusable)

on:
  workflow_call:
    inputs:
      review-server-url:
        description: 'AI Review Server URL'
        required: false
        type: string
        default: 'http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com'
      
      enable-quality-check:
        description: 'Enable static analysis quality checks'
        required: false
        type: boolean
        default: true
      
      enable-complexity-check:
        description: 'Enable complexity analysis'
        required: false
        type: boolean
        default: true
      
      enable-security-scan:
        description: 'Enable security scanning'
        required: false
        type: boolean
        default: true
      
      complexity-threshold:
        description: 'Maximum cyclomatic complexity threshold'
        required: false
        type: number
        default: 10
      
      python-version:
        description: 'Python version to use'
        required: false
        type: string
        default: '3.10'
    
    secrets:
      ai-review-token:
        description: 'AI Review Service Access Token'
        required: true

permissions:
  contents: read
  pull-requests: write

jobs:
  ai-code-review:
    runs-on: ubuntu-latest
    name: AI Code Review
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Get PR diff and metadata
        id: pr-info
        run: |
          PR_NUMBER=${{ github.event.pull_request.number }}
          echo "pr_number=$PR_NUMBER" >> $GITHUB_OUTPUT
          
          git diff origin/${{ github.base_ref }}...HEAD > pr_diff.txt
          git diff --name-only origin/${{ github.base_ref }}...HEAD > changed_files.txt
          
          echo "📊 PR Info:"
          echo "Number: $PR_NUMBER"
          echo "Files changed: $(cat changed_files.txt | wc -l)"
          
          if [ ! -s changed_files.txt ]; then
            echo "has_changes=false" >> $GITHUB_OUTPUT
          else
            echo "has_changes=true" >> $GITHUB_OUTPUT
          fi

      - name: Send to AI Review Server
        if: steps.pr-info.outputs.has_changes == 'true'
        env:
          REVIEW_SERVER_URL: ${{ inputs.review-server-url }}
          AI_REVIEW_ACCESS_TOKEN: ${{ secrets.ai-review-token }}
          PR_NUMBER: ${{ steps.pr-info.outputs.pr_number }}
        run: |
          echo "🚀 Sending code to centralized AI review server..."
          
          jq -n \
            --arg repo "${{ github.repository }}" \
            --argjson pr_num "$PR_NUMBER" \
            --arg pr_title "${{ github.event.pull_request.title }}" \
            --arg pr_desc "${{ github.event.pull_request.body }}" \
            --arg base "${{ github.base_ref }}" \
            --arg head "${{ github.head_ref }}" \
            --arg author "${{ github.event.pull_request.user.login }}" \
            --rawfile diff pr_diff.txt \
            --argjson files "$(cat changed_files.txt | jq -R -s -c 'split("\n") | map(select(length > 0))')" \
            '{
              repository: $repo,
              pr_number: $pr_num,
              pr_title: $pr_title,
              pr_description: ($pr_desc // ""),
              base_branch: $base,
              head_branch: $head,
              author: $author,
              diff: $diff,
              files: $files
            }' > review_request.json
          
          HTTP_STATUS=$(curl -s -w "%{http_code}" -o review_response.json \
            -X POST \
            -H "Content-Type: application/json" \
            -H "x-access-token: $AI_REVIEW_ACCESS_TOKEN" \
            -d @review_request.json \
            "$REVIEW_SERVER_URL/api/review/pr")
          
          echo "📡 Server response status: $HTTP_STATUS"
          
          if [ "$HTTP_STATUS" -eq 200 ] || [ "$HTTP_STATUS" -eq 201 ]; then
            echo "✅ Successfully received AI code review"
            mkdir -p review-outputs
            cp review_response.json review-outputs/ai-review.json
          else
            echo "⚠️ Review server returned status $HTTP_STATUS"
            echo '{"source":"AI Review","issues":[],"total_issues":0}' > review-outputs/ai-review.json
          fi

      - name: Upload AI review results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: review-results-ai
          path: review-outputs/
          retention-days: 30

  quality-check:
    runs-on: ubuntu-latest
    name: Code Quality Analysis
    if: inputs.enable-quality-check
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: ${{ inputs.python-version }}

      - name: Analyze Python code
        run: |
          echo "🔍 Running static analysis..."
          
          git diff --name-only origin/${{ github.base_ref }}...HEAD | grep -E '\.py$' > changed_files.txt || true
          
          if [ -s changed_files.txt ]; then
            pip install pylint flake8
            mkdir -p review-outputs
            
            # Run analysis and save results
            echo '{"source":"Static Analysis","issues":[],"total_issues":0}' > review-outputs/quality-issues.json
          else
            mkdir -p review-outputs
            echo '{"source":"Static Analysis","issues":[],"total_issues":0}' > review-outputs/quality-issues.json
          fi

      - name: Upload quality results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: review-results-quality
          path: review-outputs/
          retention-days: 30

  complexity-check:
    runs-on: ubuntu-latest
    name: Complexity Analysis
    if: inputs.enable-complexity-check
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: ${{ inputs.python-version }}

      - name: Analyze complexity
        run: |
          echo "📊 Analyzing code complexity..."
          pip install radon
          mkdir -p review-outputs
          
          if find . -name "*.py" -not -path "./.git/*" -not -path "./venv/*" | grep -q .; then
            radon cc . -a -nb -j > review-outputs/complexity-cc.json || echo '[]' > review-outputs/complexity-cc.json
            
            cat review-outputs/complexity-cc.json | jq --argjson threshold ${{ inputs.complexity-threshold }} '{
              source: "Complexity Analysis",
              issues: [.[][] | select(.complexity > $threshold) | {
                file: .name,
                line: .lineno,
                severity: (if .complexity > 20 then "error" elif .complexity > 15 then "warning" else "info" end),
                message: ("High cyclomatic complexity: " + (.complexity | tostring)),
                category: "complexity"
              }],
              total_issues: ([.[][] | select(.complexity > $threshold)] | length)
            }' > review-outputs/complexity-issues.json
          else
            echo '{"source":"Complexity Analysis","issues":[],"total_issues":0}' > review-outputs/complexity-issues.json
          fi

      - name: Upload complexity results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: review-results-complexity
          path: review-outputs/
          retention-days: 30

  security-scan:
    runs-on: ubuntu-latest
    name: Security Scan
    if: inputs.enable-security-scan
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Run Trivy scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'json'
          output: 'trivy-results.json'
          exit-code: '0'
          severity: 'CRITICAL,HIGH,MEDIUM'

      - name: Process security results
        if: always()
        run: |
          mkdir -p review-outputs
          
          if [ -f trivy-results.json ]; then
            cat trivy-results.json | jq '{
              source: "Security Scan",
              issues: [.Results[]?.Vulnerabilities[]? // [] | {
                file: (.PkgName // "dependency"),
                severity: (if .Severity == "CRITICAL" then "critical" elif .Severity == "HIGH" then "error" else "warning" end),
                message: (.Title + " (" + .VulnerabilityID + ")"),
                category: "security"
              }],
              total_issues: ([.Results[]?.Vulnerabilities[]? // []] | length)
            }' > review-outputs/security-issues.json
          else
            echo '{"source":"Security Scan","issues":[],"total_issues":0}' > review-outputs/security-issues.json
          fi

      - name: Upload security results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: review-results-security
          path: review-outputs/
          retention-days: 30

  aggregate-results:
    runs-on: ubuntu-latest
    name: Aggregate and Report
    needs: [ai-code-review, quality-check, complexity-check, security-scan]
    if: always()
    permissions:
      contents: read
      pull-requests: write
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Download all results
        uses: actions/download-artifact@v4
        with:
          pattern: review-results-*
          path: review-outputs/
          merge-multiple: true

      - name: Aggregate and post to PR
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          echo "📊 Aggregating all review results..."
          
          # Merge all results
          jq -s '{
            total_issues: (map(.total_issues // 0) | add),
            issues_by_severity: {
              critical: ([.[].issues[] | select(.severity == "critical")] | length),
              error: ([.[].issues[] | select(.severity == "error")] | length),
              warning: ([.[].issues[] | select(.severity == "warning")] | length),
              info: ([.[].issues[] | select(.severity == "info" or .severity == "suggestion")] | length)
            },
            all_issues: [.[].issues[]]
          }' review-outputs/*-issues.json > aggregated.json
          
          TOTAL=$(jq -r '.total_issues' aggregated.json)
          CRITICAL=$(jq -r '.issues_by_severity.critical' aggregated.json)
          ERRORS=$(jq -r '.issues_by_severity.error' aggregated.json)
          WARNINGS=$(jq -r '.issues_by_severity.warning' aggregated.json)
          
          # Post PR comment
          cat > comment.md << EOF
          ## 🤖 Comprehensive Code Review
          
          **Total Issues:** $TOTAL
          
          | Severity | Count | Status |
          |----------|-------|--------|
          | 🚨 Critical | $CRITICAL | $([ $CRITICAL -gt 0 ] && echo "❌ Must fix" || echo "✅") |
          | ❌ Error | $ERRORS | $([ $ERRORS -gt 0 ] && echo "⚠️ Should fix" || echo "✅") |
          | ⚠️ Warning | $WARNINGS | $([ $WARNINGS -gt 0 ] && echo "⚡ Review recommended" || echo "✅") |
          
          ---
          *🔗 Powered by Centralized AI Review Service*
          EOF
          
          gh pr comment ${{ github.event.pull_request.number }} --body-file comment.md || echo "Could not post comment"
```

### Step 3: Use in Other Repositories

Each repository only needs a **simple caller workflow**:

**File:** `.github/workflows/ai-code-review.yml` (in each repo)

```yaml
name: AI Code Review

on:
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  call-ai-review:
    uses: your-org/github-workflows/.github/workflows/ai-code-review-reusable.yml@main
    with:
      # Customize per repository if needed
      enable-quality-check: true
      enable-complexity-check: true
      enable-security-scan: true
      complexity-threshold: 10
      python-version: '3.10'
    secrets:
      ai-review-token: ${{ secrets.AI_REVIEW_ACCESS_TOKEN }}
```

### Step 4: Repository-Specific Customization

**Example: Disable security scan for a specific repo:**

```yaml
jobs:
  call-ai-review:
    uses: your-org/github-workflows/.github/workflows/ai-code-review-reusable.yml@main
    with:
      enable-security-scan: false  # Disable for this repo
      complexity-threshold: 15      # Higher threshold for this repo
    secrets:
      ai-review-token: ${{ secrets.AI_REVIEW_ACCESS_TOKEN }}
```

### Benefits

✅ **Single source of truth** - Update once, applies everywhere  
✅ **Version control** - Pin to specific versions or use `@main`  
✅ **Customizable** - Each repo can override defaults  
✅ **Easy rollout** - Add to new repos in seconds  
✅ **Rollback support** - Pin to previous version if issues arise

### Versioning Strategy

```yaml
# Use latest (auto-updates)
uses: your-org/github-workflows/.github/workflows/ai-code-review-reusable.yml@main

# Pin to specific version (stable)
uses: your-org/github-workflows/.github/workflows/ai-code-review-reusable.yml@v1.2.0

# Pin to branch for testing
uses: your-org/github-workflows/.github/workflows/ai-code-review-reusable.yml@feature/new-checks
```

---

## Approach 2: Repository Templates

**Best for:** New repositories or complete standardization

### How It Works

1. Create a **template repository** with the complete workflow
2. New repositories **created from template** inherit the workflow
3. Updates require **manual sync** or automation

### Setup

1. **Create template repository:**
   - Go to repository Settings
   - Check "Template repository"

2. **Include workflow files:**
   ```
   your-template-repo/
   ├── .github/
   │   └── workflows/
   │       └── ai-code-review.yml
   └── README.md
   ```

3. **Create new repos from template:**
   - Click "Use this template"
   - New repo gets all files including workflows

### Pros & Cons

✅ Simple setup for new repos  
✅ Complete standardization  
❌ No automatic updates  
❌ Requires manual sync for changes  
❌ Doesn't help existing repos

---

## Approach 3: Automation Scripts

**Best for:** Organizations needing fine-grained control

### How It Works

Use a script to **sync workflow files** across repositories automatically.

### Example: Python Script

```python
#!/usr/bin/env python3
"""
Sync AI code review workflow across multiple repositories
"""

import os
from github import Github
import yaml

# Configuration
GITHUB_TOKEN = os.environ['GITHUB_TOKEN']
ORG_NAME = 'your-org'
WORKFLOW_SOURCE = '.github/workflows/ai-code-review.yml'
REPOSITORIES = [
    'repo1',
    'repo2',
    'repo3',
    # ... or fetch dynamically
]

def sync_workflow_to_repo(g, org_name, repo_name, workflow_content):
    """Sync workflow file to a single repository"""
    try:
        repo = g.get_repo(f"{org_name}/{repo_name}")
        
        # Check if file exists
        try:
            contents = repo.get_contents('.github/workflows/ai-code-review.yml')
            # Update existing file
            repo.update_file(
                contents.path,
                f"chore: update AI code review workflow",
                workflow_content,
                contents.sha,
                branch='main'
            )
            print(f"✅ Updated {repo_name}")
        except:
            # Create new file
            repo.create_file(
                '.github/workflows/ai-code-review.yml',
                f"chore: add AI code review workflow",
                workflow_content,
                branch='main'
            )
            print(f"✅ Created in {repo_name}")
            
    except Exception as e:
        print(f"❌ Failed for {repo_name}: {e}")

def main():
    g = Github(GITHUB_TOKEN)
    
    # Read source workflow
    with open(WORKFLOW_SOURCE, 'r') as f:
        workflow_content = f.read()
    
    # Sync to all repositories
    for repo_name in REPOSITORIES:
        sync_workflow_to_repo(g, ORG_NAME, repo_name, workflow_content)

if __name__ == '__main__':
    main()
```

### Run as GitHub Action

```yaml
name: Sync Workflows

on:
  push:
    paths:
      - '.github/workflows/ai-code-review.yml'
  workflow_dispatch:

jobs:
  sync:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.10'
      
      - name: Install dependencies
        run: pip install PyGithub
      
      - name: Sync workflows
        env:
          GITHUB_TOKEN: ${{ secrets.ORG_ADMIN_TOKEN }}
        run: python scripts/sync_workflows.py
```

### Pros & Cons

✅ Full control over sync process  
✅ Can customize per repository  
✅ Automatic or on-demand  
❌ Requires maintenance  
❌ Needs elevated permissions  
❌ More complex setup

---

## Approach 4: GitHub App

**Best for:** Enterprise organizations with many repositories

### How It Works

Build a **GitHub App** that:
1. Listens for repository events
2. Automatically adds/updates workflows
3. Manages configuration centrally

### Implementation

```javascript
// Example using Probot framework
module.exports = (app) => {
  app.on('repository.created', async (context) => {
    const repo = context.payload.repository;
    
    // Add workflow file to new repository
    await context.octokit.repos.createOrUpdateFileContents({
      owner: repo.owner.login,
      repo: repo.name,
      path: '.github/workflows/ai-code-review.yml',
      message: 'Add AI code review workflow',
      content: Buffer.from(workflowContent).toString('base64')
    });
  });
};
```

### Pros & Cons

✅ Fully automated  
✅ Enterprise-grade  
✅ Event-driven updates  
❌ Complex to build and maintain  
❌ Requires GitHub App setup  
❌ Overkill for small organizations

---

## Comparison & Recommendations

| Approach | Ease of Setup | Maintenance | Updates | Best For |
|----------|---------------|-------------|---------|----------|
| **Reusable Workflows** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Automatic | **Most organizations** |
| Repository Templates | ⭐⭐⭐⭐ | ⭐⭐ | Manual | New repositories only |
| Automation Scripts | ⭐⭐⭐ | ⭐⭐⭐ | Automatic | Custom requirements |
| GitHub App | ⭐⭐ | ⭐⭐ | Automatic | Large enterprises |

## 🎯 Recommended Solution

**For most organizations: Use Reusable Workflows**

### Quick Start

1. **Create central workflow repo:**
   ```bash
   gh repo create your-org/github-workflows --public
   cd github-workflows
   mkdir -p .github/workflows
   # Add reusable workflow from example above
   ```

2. **Add to all repositories:**
   ```yaml
   # .github/workflows/ai-code-review.yml
   name: AI Code Review
   on:
     pull_request:
   jobs:
     review:
       uses: your-org/github-workflows/.github/workflows/ai-code-review-reusable.yml@main
       secrets:
         ai-review-token: ${{ secrets.AI_REVIEW_ACCESS_TOKEN }}
   ```

3. **Set organization secret:**
   ```bash
   # Set at organization level so all repos have access
   gh secret set AI_REVIEW_ACCESS_TOKEN --org your-org
   ```

## 📚 Additional Resources

- [GitHub Reusable Workflows Docs](https://docs.github.com/en/actions/using-workflows/reusing-workflows)
- [Workflow Syntax Reference](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [Organization Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets#creating-encrypted-secrets-for-an-organization)

## 🆘 Need Help?

- Check workflow runs in Actions tab
- Review reusable workflow documentation
- Contact your DevOps team
- Open issue in central workflows repository
