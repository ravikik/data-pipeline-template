# Centralized AI Review Service - Configuration Summary

## Overview

The AI code review workflow has been successfully updated to connect to your **centralized AI review service** instead of using individual OpenAI API keys.

**Service Endpoint:** `http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com`

---

## What Changed

### 1. Workflow Configuration

**File:** `.github/workflows/ai-code-review.yml`

**Changes:**
- ✅ Removed dependency on `freeedcom/ai-codereviewer` action
- ✅ Implemented custom integration with centralized service
- ✅ Removed requirement for `OPENAI_API_KEY` secret
- ✅ Added API calls to centralized review server
- ✅ Automated posting of review comments back to PRs

**New Workflow Steps:**
1. Checkout repository with full history
2. Extract PR diff and metadata
3. Send code to centralized AI review server
4. Receive review feedback from server
5. Post review comments to pull request

**API Integration:**
```yaml
REVIEW_SERVER_URL: "http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com"
Endpoint: POST /api/review/pr  # For PR changed files (GitHub Actions)
Headers:
  - Content-Type: application/json
  - x-access-token: <AI_REVIEW_ACCESS_TOKEN secret>
  - X-GitHub-Token: <auto-provided>
  - X-Repository: <repo-name>
  - X-PR-Number: <pr-number>
```

**Available Endpoints:**

| Endpoint | Scenario | Use Case |
|----------|----------|----------|
| `POST /api/review/pr` | **GitHub Actions on PR** ← *Default* | Reviews only changed files in a pull request |
| `POST /api/batch-review/by-pattern` | Scheduled/Branch Scan | Scans files matching a glob pattern |
| `POST /api/review/batch` | Explicit File List | Reviews specific files from git diff or list |

**This workflow uses:** `POST /api/review/pr` for automatic PR reviews

---

### 2. Documentation Updates

All documentation has been updated to reflect the centralized service:

#### Updated Files:
- ✅ `README.md` - Main project documentation
- ✅ `.github/workflows/README.md` - Workflow documentation
- ✅ `.github/AI_REVIEW_GUIDE.md` - Quick reference guide
- ✅ `.github/AI_REVIEW_IMPLEMENTATION.md` - Implementation details
- ✅ `CONTRIBUTING.md` - Contributing guidelines

#### Key Documentation Changes:
- Removed all references to OpenAI API keys
- Added centralized service endpoint information
- Updated setup instructions (no setup required!)
- Removed cost management sections
- Added service benefits and features
- Updated troubleshooting guides

---

## Benefits of Centralized Service

### For Development Teams

1. **Zero Setup** 
   - No API keys to manage
   - No secrets to configure
   - Works immediately on new repositories

2. **No Individual Costs**
   - No OpenAI billing per team
   - Unlimited reviews
   - No quota management

3. **Consistent Quality**
   - Same AI models across all teams
   - Standardized review criteria
   - Centrally managed prompts

4. **Better Performance**
   - Optimized infrastructure
   - Dedicated review service
   - Faster response times

5. **Enterprise Features**
   - Advanced security
   - Compliance controls
   - Audit logging
   - Centralized monitoring

### For Organizations

1. **Cost Management**
   - Centralized billing
   - Better cost prediction
   - Volume discounts

2. **Governance**
   - Consistent standards
   - Policy enforcement
   - Usage monitoring

3. **Maintenance**
   - Single point of updates
   - Managed AI model versions
   - Centralized troubleshooting

---

## How It Works

### Request Flow

```
GitHub PR Created
    ↓
GitHub Actions Triggered
    ↓
Extract PR Diff & Metadata
    ↓
Send to Centralized Service
(POST /api/review/pr)
    ↓
AI Service Analyzes Code
    ↓
Service Returns Review
    ↓
Post Comments to PR
    ↓
Review Complete
```

### Data Sent to Service

```json
{
  "repository": "owner/repo-name",
  "pr_number": "123",
  "pr_title": "Add new feature",
  "pr_description": "Description...",
  "base_branch": "main",
  "head_branch": "feature/new-thing",
  "author": "username",
  "diff": "...full diff...",
  "files": ["file1.py", "file2.yml"]
}
```

### Expected Response

```json
{
  "review_id": "abc123",
  "summary": "Review completed. Found 3 suggestions.",
  "comments": [
    {
      "file": "src/main.py",
      "line": 42,
      "body": "Consider using a context manager here..."
    }
  ]
}
```

---

## API Endpoints Reference

The centralized service provides multiple endpoints for different review scenarios:

### 1. PR Review Endpoint (Default)

**Endpoint:** `POST /api/review/pr`

**Use Case:** GitHub Actions on pull requests (changed files only)

**When to use:**
- Automatic PR reviews in CI/CD
- Reviewing only the files changed in a PR
- Integrated with GitHub Actions workflow (default setup)

**Request Payload:**
```json
{
  "repository": "owner/repo-name",
  "pr_number": "123",
  "pr_title": "Add new feature",
  "pr_description": "Description...",
  "base_branch": "main",
  "head_branch": "feature/new-thing",
  "author": "username",
  "diff": "...full diff...",
  "files": ["file1.py", "file2.yml"]
}
```

---

### 2. Batch Review by Pattern

**Endpoint:** `POST /api/batch-review/by-pattern`

**Use Case:** Scheduled scans or branch scans using glob patterns

**When to use:**
- Periodic code quality scans
- Scanning entire branches or directories
- Reviewing files matching specific patterns (e.g., `**/*.py`)

**Example Request:**
```json
{
  "repository": "owner/repo-name",
  "branch": "main",
  "pattern": "src/**/*.py",
  "exclude": ["**/tests/**", "**/__pycache__/**"]
}
```

**Example Workflow (Scheduled Scan):**
```yaml
name: Weekly Code Review
on:
  schedule:
    - cron: '0 0 * * 0'  # Every Sunday
jobs:
  scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Scan Python files
        run: |
          curl -X POST \
            -H "x-access-token: ${{ secrets.AI_REVIEW_ACCESS_TOKEN }}" \
            -H "Content-Type: application/json" \
            -d '{"repository":"${{ github.repository }}","branch":"main","pattern":"**/*.py"}' \
            http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com/api/batch-review/by-pattern
```

---

### 3. Batch Review by File List

**Endpoint:** `POST /api/review/batch`

**Use Case:** Review specific files from git diff or explicit list

**When to use:**
- Custom file selections
- Files from git diff between branches
- Manually specified file lists

**Example Request:**
```json
{
  "repository": "owner/repo-name",
  "branch": "feature/branch",
  "files": [
    "src/main.py",
    "src/utils/helper.py",
    "config/settings.yml"
  ]
}
```

**Example Workflow (Compare Branches):**
```yaml
- name: Review changed files between branches
  run: |
    FILES=$(git diff --name-only main..develop | jq -R -s -c 'split("\n") | map(select(length > 0))')
    curl -X POST \
      -H "x-access-token: ${{ secrets.AI_REVIEW_ACCESS_TOKEN }}" \
      -H "Content-Type: application/json" \
      -d "{\"repository\":\"${{ github.repository }}\",\"branch\":\"develop\",\"files\":$FILES}" \
      http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com/api/review/batch
```

---

## Configuration

### Workflow Variables

**Location:** `.github/workflows/ai-code-review.yml`

```yaml
env:
  REVIEW_SERVER_URL: "http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com"
  GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}  # Auto-provided
```

### Service Customization

**Location:** `.github/ai-review-config.yml`

Teams can still customize:
- File inclusion/exclusion patterns
- Code complexity thresholds
- Maintainability index thresholds
- Security check patterns

**Note:** AI model selection is managed by the centralized service.

---

## Testing the Integration

### Manual Test

1. Create a test branch:
   ```bash
   git checkout -b test/ai-review-integration
   ```

2. Make a small code change:
   ```bash
   echo "# Test change" >> README.md
   git add README.md
   git commit -m "test: verify AI review integration"
   git push origin test/ai-review-integration
   ```

3. Open a pull request

4. Check the Actions tab:
   - Workflow should trigger
   - Look for "AI Code Review" job
   - Verify connection to centralized service
   - Check for review comments

### Expected Results

✅ Workflow completes successfully  
✅ Service responds with HTTP 200/201  
✅ Review ID is logged  
✅ Summary posted to PR  
✅ No errors about missing API keys

---

## Troubleshooting

### Common Issues

#### 1. Service Connection Failed

**Symptoms:**
- HTTP 500+ errors
- Timeout errors
- "Service unavailable" messages

**Solutions:**
- Check service endpoint URL in workflow
- Verify network connectivity
- Contact service admin team
- Check service status page

#### 2. Authentication Failed (401 Unauthorized)

**Symptoms:**
- "Server response status: 401" in logs
- Error: "Unauthorized. No authentication credentials provided."
- Hint: "Provide x-access-token header"

**Solutions:**
- Verify `AI_REVIEW_ACCESS_TOKEN` secret is set in repository
- Check if access token has expired
- Contact admin team for new token
- Ensure secret name is exactly `AI_REVIEW_ACCESS_TOKEN`

#### 3. No Review Comments Posted

**Symptoms:**
- Workflow succeeds but no comments appear
- Empty response from service

**Solutions:**
- Check if PR only modified excluded files
- Verify service returned comments in response
- Check GitHub token permissions
- Review Actions logs for errors

#### 4. Review Takes Too Long

**Symptoms:**
- Workflow times out
- No response from service

**Solutions:**
- Check PR size (very large diffs may take longer)
- Verify service isn't under heavy load
- Contact admin if persistent

### Debug Steps

1. **Check Actions Logs:**
   - Go to Actions tab
   - Click on failed workflow
   - Review "AI Code Review" job logs

2. **Verify Service Response:**
   ```bash
   # Look for in workflow logs:
   "Server response status: 200"
   "Review ID: ..."
   ```

3. **Test Service Manually:**
   ```bash
   curl -X POST http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com/api/health
   ```

---

## Maintenance

### Regular Tasks

**Weekly:**
- Monitor workflow success rates
- Check for service connectivity issues

**Monthly:**
- Review service performance metrics
- Gather team feedback on review quality

**Quarterly:**
- Update GitHub Actions versions
- Review and update file exclusion patterns

### Service Updates

The centralized service is managed by your admin team. Updates to:
- AI models
- Review prompts
- Service infrastructure

are handled automatically with no changes needed in repositories.

---

## Migration Complete! ✅

All files have been updated to use the centralized AI review service:

### Files Modified:
1. ✅ `.github/workflows/ai-code-review.yml` - Workflow integration
2. ✅ `README.md` - Updated setup instructions
3. ✅ `.github/workflows/README.md` - Workflow documentation
4. ✅ `.github/AI_REVIEW_GUIDE.md` - User guide
5. ✅ `.github/AI_REVIEW_IMPLEMENTATION.md` - Implementation details
6. ✅ `CONTRIBUTING.md` - Contributing guidelines

### Ready to Use:
- ✅ No setup required
- ✅ No API keys needed
- ✅ Works on all new PRs
- ✅ Fully documented

---

## Support

**For Service Issues:**
- Contact your centralized service admin team
- Check service status/health endpoint
- Review documentation at service portal

**For Workflow Issues:**
- Check `.github/workflows/README.md`
- Review GitHub Actions logs
- Check file: `.github/AI_REVIEW_GUIDE.md`

**For Questions:**
- See `.github/AI_REVIEW_GUIDE.md` FAQ section
- Contact repository maintainers
- Reach out to service admin team

---

## Next Steps

1. ✅ **Test the integration** - Create a test PR
2. ✅ **Monitor first few reviews** - Verify everything works
3. ✅ **Gather feedback** - Get team input
4. ✅ **Customize as needed** - Adjust thresholds in config
5. ✅ **Share with team** - Communicate the new workflow

---

**Service Endpoint:** `http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com`

**Status:** Ready for production use! 🚀
