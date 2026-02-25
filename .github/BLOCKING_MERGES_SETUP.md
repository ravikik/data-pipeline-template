# 🚫 Blocking Merges with Quality Gate - Setup Guide

## Why Isn't My Merge Blocked?

The AI Code Review workflow **correctly fails** when the quality score is below 90, but GitHub doesn't automatically block merges just because a workflow fails. You need to configure **Branch Protection Rules** to enforce status checks.

---

## ✅ Quick Fix: Enable Branch Protection

### Step 1: Navigate to Branch Protection Settings

1. Go to your GitHub repository
2. Click **Settings** (top navigation)
3. In the left sidebar, click **Branches**
4. Under "Branch protection rules", click **Add rule** (or edit existing rule)

### Step 2: Configure Protection for Main Branch

**Branch name pattern:** `main` (or your default branch name)

**Enable these settings:**

#### ✅ Required - Status Checks
- ☑️ **Require status checks to pass before merging**
  - ☑️ **Require branches to be up to date before merging**
  
  **Select required status checks:**
  - ☑️ `Quality Gate - Enforce Standards` (CRITICAL - this one blocks merges)
  - ☑️ `AI Code Review`
  - ☑️ `Code Quality Analysis`
  - ☑️ `Complexity Analysis`
  - ☑️ `Security Scan`
  - ☑️ `Aggregate and Report`

#### ✅ Recommended - Additional Protections
- ☑️ **Require a pull request before merging**
  - **Required number of approvals before merging:** 1 (or more)
  - ☑️ **Dismiss stale pull request approvals when new commits are pushed**
  - ☑️ **Require review from Code Owners** (if using CODEOWNERS)

- ☑️ **Require conversation resolution before merging**
- ☑️ **Do not allow bypassing the above settings** (optional - prevents admin bypass)

#### ❌ Keep Disabled
- ☐ **Allow force pushes** (leave unchecked)
- ☐ **Allow deletions** (leave unchecked)

### Step 3: Save Changes

Click **Create** (or **Save changes**)

---

## 🎯 Result After Configuration

### Before Branch Protection
```
Quality Score: 85/100 ❌
Workflow Status: Failed ❌
Merge Button: ✅ Enabled (Can still merge!)  ← Problem!
```

### After Branch Protection
```
Quality Score: 85/100 ❌
Workflow Status: Failed ❌
Merge Button: 🚫 Disabled  ← Fixed!

Message: "Merging is blocked. Required status check 
'Quality Gate - Enforce Standards' has not passed."
```

---

## 📸 Visual Guide

### Finding Branch Protection

```
Repository
  └── Settings
      └── Branches
          └── Branch protection rules
              └── Add rule (or Edit)
```

### Configuring Required Checks

After clicking "Require status checks to pass before merging", you'll see a search box. Type to find checks:

```
Search for status checks in the last week for this repository

🔍 [Type to search...                              ]

Recently seen status checks:
  ☑️ Quality Gate - Enforce Standards    ← CHECK THIS!
  ☑️ AI Code Review
  ☑️ Code Quality Analysis
  ☑️ Complexity Analysis
  ☑️ Security Scan
  ☑️ Aggregate and Report
```

**Note:** Status checks only appear after they've run at least once. If you don't see them:
1. Create a test PR to trigger the workflow
2. Wait for it to complete
3. Return to branch protection and the checks will now appear

---

## 🔍 Verifying It Works

### Test 1: Create a PR with Issues

1. Make changes that introduce issues (e.g., add a `print()` statement)
2. Create a pull request
3. Wait for workflow to complete

**Expected result:**
- ❌ Workflow shows "Failed"
- 🚫 Merge button is **disabled** or shows "Required checks must pass"
- 💬 Comment shows score < 90

### Test 2: Create a Clean PR

1. Make quality changes (no issues)
2. Create a pull request
3. Wait for workflow to complete

**Expected result:**
- ✅ Workflow shows "Success"
- ✅ Merge button is **enabled**
- 💬 Comment shows score ≥ 90

---

## 🔧 Troubleshooting

### Issue: "I don't see the status checks in the list"

**Solution:**
1. The checks only appear after running at least once
2. Create a test PR to trigger the workflow
3. Wait for completion
4. Go back to branch protection settings
5. The checks should now be visible in the search

### Issue: "Merge button is still enabled even with failed checks"

**Possible causes:**

1. **Branch protection not applied to the correct branch**
   - Verify the rule pattern matches your branch name exactly
   - Pattern `main` only protects `main`, not `master` or `develop`

2. **Required checks not selected**
   - Go back to branch protection
   - Verify "Quality Gate - Enforce Standards" is checked
   - Save changes

3. **You have admin privileges**
   - Admins can bypass protections by default
   - Check "Do not allow bypassing the above settings" to enforce even for admins
   - Or test with a non-admin account

4. **Wrong repository**
   - Ensure you're configuring the correct repository's settings

### Issue: "Status check is 'Expected' but never runs"

**Solution:**
- Check that the workflow file is in `.github/workflows/` on the base branch
- Verify the workflow trigger includes `pull_request`
- Check Actions tab for error messages

### Issue: "All checks pass but merge is still blocked"

**Possible causes:**
- PR not up to date with base branch (enable "Require branches to be up to date")
- Conversation threads not resolved (if that option is enabled)
- Required reviews not approved
- Conflicts with base branch

---

## 🎓 Understanding How It Works

### Workflow Behavior

```yaml
# In .github/workflows/ai-code-review.yml

quality-gate:
  name: Quality Gate - Enforce Standards
  steps:
    - name: Enforce quality gate
      run: |
        if [ $SCORE -lt $THRESHOLD ]; then
          exit 1  # ← This makes the workflow FAIL
        fi
```

**Key points:**
- `exit 1` causes the workflow to fail
- Failed workflow = ❌ status in PR checks
- But doesn't block merges by itself

### Branch Protection Enforcement

```
Branch Protection Configuration:
├── Require status checks ← This setting blocks merges
│   └── Required checks:
│       └── Quality Gate - Enforce Standards ← Must be selected
│
└── Merge Decision:
    ├── If all required checks pass → ✅ Allow merge
    └── If any required check fails → 🚫 Block merge
```

---

## 📋 Quick Setup Checklist

Use this checklist to ensure everything is configured:

### Workflow Configuration
- ✅ Workflow file exists at `.github/workflows/ai-code-review.yml`
- ✅ `quality-gate` job includes `exit 1` when score < threshold
- ✅ Workflow has run at least once on a PR

### Branch Protection
- ✅ Branch protection rule exists for target branch (e.g., `main`)
- ✅ "Require status checks to pass before merging" is enabled
- ✅ "Quality Gate - Enforce Standards" is in the required checks list
- ✅ Changes are saved

### Testing
- ✅ Created test PR with intentional issues
- ✅ Quality score < 90
- ✅ Merge button is disabled
- ✅ Error message explains which check failed

---

## 🔐 Security Considerations

### Admin Bypass

By default, **repository admins can bypass branch protection**. To prevent this:

1. In branch protection settings, enable:
   - ☑️ **Do not allow bypassing the above settings**

2. Or use **rulesets** (newer feature):
   - Settings → Rules → Rulesets
   - Create ruleset with "Enforce for all administrators"

### Organization-Level Enforcement

For multiple repositories, use **Organization Rulesets**:

1. Go to Organization Settings → Repository rules
2. Create ruleset applying to all/selected repositories
3. Configure same protections
4. Enforce for administrators

---

## 🆘 Still Not Working?

If you've followed all steps and merges still aren't blocked:

### Double-Check Configuration

1. **View the PR checks section**
   - Do you see "Quality Gate - Enforce Standards"?
   - Is it showing ❌ Failed?

2. **Verify branch protection**
   ```bash
   # Using GitHub CLI
   gh api repos/:owner/:repo/branches/main/protection \
     | jq '.required_status_checks.contexts'
   
   # Should include "Quality Gate - Enforce Standards"
   ```

3. **Check your permissions**
   - Are you a repository admin?
   - Try with a different user account
   - Or enable "Do not allow bypassing"

4. **Inspect workflow logs**
   - Go to Actions tab
   - Click on the failed workflow run
   - Check "Enforce quality gate" step
   - Verify it shows `exit 1`

### Common Mistakes

❌ **Wrong check name**
- Must be exactly: `Quality Gate - Enforce Standards`
- This matches the `name:` field in the workflow job

❌ **Applied to wrong branch**
- Rule must match the branch you're merging into
- `main` ≠ `master` ≠ `develop`

❌ **Status check disabled in workflow**
- Check `.github/ai-review-config.yml`
- Ensure `quality_gate.enabled: true`

---

## 📚 Additional Resources

- **GitHub Docs:** [About protected branches](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- **GitHub Docs:** [About status checks](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/collaborating-on-repositories-with-code-quality-features/about-status-checks)
- **Quality Gate Guide:** [QUALITY_GATE_GUIDE.md](QUALITY_GATE_GUIDE.md)
- **AI Review Guide:** [AI_REVIEW_GUIDE.md](AI_REVIEW_GUIDE.md)

---

## 💡 Pro Tips

1. **Test with a non-admin account** to see the true user experience
2. **Use draft PRs** for testing without formal review
3. **Set up CODEOWNERS** for additional review requirements
4. **Enable GitHub Actions workflow approval** for forks
5. **Use rulesets** instead of legacy branch protection for better control
6. **Document** your branch protection rules in repository README

---

**Need help?** Open an issue with:
- Screenshot of branch protection settings
- Screenshot of PR checks section
- Link to failed workflow run
- Your role in the repository (admin/member)
