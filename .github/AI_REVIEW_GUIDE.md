# AI Code Review - Quick Reference Guide1

## Overview

This repository uses an automated AI-powered code review system that connects to a **centralized review service** to analyze pull requests and provide intelligent feedback on code quality, security, and best practices.

**Centralized Service:** `http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com`

**Benefits:**
- ✅ Simple setup - just add access token secret
- ✅ No individual API costs to manage
- ✅ Consistent review standards across all teams
- ✅ Enterprise-grade AI models and infrastructure
- ✅ Managed updates and improvements
- ✅ **Comprehensive issue tracking and reporting**
- ✅ **Analytics dashboard for metrics tracking**

**Required Setup:**
- Add `AI_REVIEW_ACCESS_TOKEN` secret to your repository
- Obtain token from your admin team

## 📊 New Features

### Comprehensive Review Reports

Every PR now receives:

1. **Aggregated Issue List** - All findings from all tools in one place:
   - AI Review findings
   - Static analysis (Pylint/Flake8)
   - Complexity issues (Radon)
   - Security vulnerabilities (Trivy)

2. **Downloadable Report** - Complete review report available as workflow artifact:
   - Detailed issue breakdown by severity
   - Issues organized by category and source
   - Executive summary with metrics
   - Available for 90 days

3. **Analytics Dashboard** - Track code quality over time:
   - View trends and patterns at `/analytics/review-dashboard.html`
   - Monitor quality scores
   - Compare PR performance
   - Identify improvement areas

### How to Access Reports

#### PR Comments
After each review, a comprehensive comment is posted with:
- Total issues found
- Breakdown by severity (🚨 Critical, ❌ Error, ⚠️  Warning, 💡 Info)
- Top issues with file locations
- Link to full report

#### Full Report Download
1. Go to the PR's **Checks** tab
2. Click on **AI Code Review** workflow
3. Scroll to **Artifacts** section
4. Download **code-review-report** artifact
5. Extract and open `review-report.md`

#### Analytics Dashboard
1. Navigate to `/analytics/review-dashboard.html` in the repository
2. Or download from **Actions** → **Review Analytics Dashboard** → **Artifacts**
3. Open HTML file in your browser for interactive visualizations

## What Gets Checked?

### ✅ Automatically Reviewed

- **Python files** (`.py`) - Full AI review + static analysis
- **YAML files** (`.yml`, `.yaml`) - Configuration validation
- **Terraform files** (`.tf`) - Infrastructure as code review
- **Dockerfiles** - Best practices and security checks
- **Shell scripts** (`.sh`) - Security and syntax review

### ❌ Excluded from Review

- JSON files (`.json`)
- Markdown files (`.md`)
- Text files (`.txt`)
- CSV data files (`.csv`)
- License files
- Cache directories (`__pycache__`, `venv`, `.git`)

## Review Checks

### 1. AI-Powered Code Review (Centralized Service)

The centralized AI service analyzes your code for:
- **Code Quality**: Readability, maintainability, and best practices
- **Potential Bugs**: Logic errors and edge cases
- **Performance**: Optimization opportunities
- **Security**: Vulnerabilities and security anti-patterns
- **Documentation**: Missing or unclear documentation

### 2. Static Analysis (Python)

- **Pylint**: Code quality and standards compliance
- **Flake8**: Style guide enforcement (PEP 8)
- Checks run only on changed Python files

### 3. Code Complexity Analysis

- **Cyclomatic Complexity**: Measures code branching complexity
  - **Threshold**: ≤ 10 (configurable)
  - High complexity = harder to test and maintain
  
- **Maintainability Index**: Scores code maintainability (0-100)
  - **Threshold**: ≥ 65 (configurable)
  - Higher is better
  
- **Raw Metrics**: LOC, LLOC, SLOC, comments, multi-line strings

### 4. Security Scanning

- **Trivy**: Scans for known vulnerabilities in dependencies and code
- **Pattern Matching**: Detects hardcoded secrets:
  - Passwords
  - API keys
  - Tokens
  - Private keys
  - Secrets
  
- **Debug Statement Detection**:
  - `print()` statements
  - `console.log()`
  - `debugger`
  - `pdb.set_trace()`
  - `breakpoint()`

### 5. Docker Best Practices

- Checks for `:latest` tag usage (not recommended)
- Verifies non-root user configuration
- Validates Dockerfile security practices

## Understanding Review Comments

### 🚨 Critical Issues (Must Fix)

- **Hardcoded credentials** - Security vulnerability
- **High severity vulnerabilities** - From Trivy scan
- **Breaking changes** without justification

### ⚠️ Warnings (Should Fix)

- **Debug statements** in production code
- **High complexity** functions (> threshold)
- **Missing error handling**
- **Using `:latest` tag** in Dockerfiles
- **Running as root** in containers

### 📌 Informational (Consider)

- **TODO/FIXME** comments
- **Code style** suggestions
- **Refactoring** opportunities
- **Performance** optimizations
- **Documentation** improvements

## 📊 Quality Gate & Scoring System

### Overview

Every PR receives a **quality score (0-100)** based on issues found. PRs must score **≥90** to be eligible for merge.

📖 **Complete Guide:** [QUALITY_GATE_GUIDE.md](QUALITY_GATE_GUIDE.md)

### Scoring Formula

**Starting Score:** 100 points

**Deductions:**
- 🚨 **Critical Issue:** -20 points each
- ❌ **Error:** -10 points each  
- ⚠️  **Warning:** -5 points each
- 💡 **Info/Suggestion:** -1 point each

**Final Score:** `100 - (critical×20 + errors×10 + warnings×5 + info×1)`

### Grade Scale

| Grade | Score | Status | Can Merge? |
|-------|-------|--------|------------|
| 🏆 A+ | 95-100 | Excellent | ✅ Yes |
| ⭐ A | 90-94 | Good | ✅ Yes |
| 👍 B | 80-89 | Fair | ❌ **Blocked** |
| ⚠️  C | 70-79 | Poor | ❌ **Blocked** |
| ❌ D | 60-69 | Very Poor | ❌ **Blocked** |
| 🚫 F | 0-59 | Failing | ❌ **Blocked** |

**Minimum Required Score:** 90 / 100

### Examples

#### Example 1: Excellent Code
```
Issues found:
  🚨 Critical: 0
  ❌ Errors: 0
  ⚠️  Warnings: 1
  💡 Info: 3

Score: 100 - (0×20 + 0×10 + 1×5 + 3×1) = 92
Grade: A ⭐
Status: ✅ PASSED - May proceed to merge
```

#### Example 2: Needs Improvement  
```
Issues found:
  🚨 Critical: 1
  ❌ Errors: 2
  ⚠️  Warnings: 4
  💡 Info: 5

Score: 100 - (1×20 + 2×10 + 4×5 + 5×1) = 45
Grade: F 🚫
Status: ❌ BLOCKED - Quality standards not met
```

#### Example 3: Just Passing
```
Issues found:
  🚨 Critical: 0
  ❌ Errors: 1
  ⚠️  Warnings: 0
  💡 Info: 0

Score: 100 - (0×20 + 1×10 + 0×5 + 0×1) = 90
Grade: A ⭐
Status: ✅ PASSED - Minimum threshold met
```

### What Happens When You Fail?

If your score is **below 90**:

1. ❌ **Workflow fails** - PR cannot be merged
2. 📊 **Score posted** to PR comments with detailed breakdown
3. 💡 **Improvement suggestions** provided
4. 🔄 **Fix and re-run** - Push updates to trigger new review

### Improving Your Score

**Priority order for maximum impact:**

1. **Fix Critical Issues First** 
   - Each fix: +20 points
   - Often security or breaking issues
   - Biggest score impact

2. **Address Errors**
   - Each fix: +10 points
   - Usually logic or syntax problems
   - High ROI for score improvement

3. **Review Warnings**
   - Each fix: +5 points
   - Best practices and code quality
   - Good for fine-tuning score

4. **Consider Info Items**
   - Each fix: +1 point
   - Minor improvements
   - Useful when close to threshold

### Quick Improvement Guide

**Need +10 points?**
- Fix 1 error, OR
- Fix 2 warnings, OR  
- Fix 1 critical (gains 20, use 10)

**Need +20 points?**
- Fix 1 critical issue, OR
- Fix 2 errors, OR
- Fix 4 warnings

**Need +30 points?**
- Fix 1 critical + 1 error, OR
- Fix 3 errors, OR
- Fix 6 warnings

### Bypassing Quality Gate

**Not Recommended** - but if absolutely necessary:

Contact your team lead or admin to:
- Request temporary threshold adjustment
- Get override approval
- Document business justification

The quality gate protects code quality - bypassing defeats its purpose.

### Monitoring Your Score

- **PR Comment:** Detailed score breakdown posted automatically
- **Workflow Status:** Check mark (✅) or X (❌) in PR checks
- **Analytics Dashboard:** Track score trends over time
- **Artifacts:** Download `quality-score.json` for details

## Best Practices for Working with AI Review

### Before Opening a PR

1. ✅ Run local tests: `make ci`
2. ✅ Check your code complexity: `radon cc . -a`
3. ✅ Run linters locally: `pylint your_file.py`
4. ✅ Remove debug statements  
5. ✅ Add appropriate comments
6. ✅ **Aim for score ≥95** for excellent quality

### After Opening a PR

1. 📬 Wait for AI review to complete (~2-5 minutes)
2. 📊 **Check your quality score** in PR comments
3. 📖 Read all review comments carefully
4. 🔍 Address critical issues and errors first (for best score improvement)
5. 💬 Respond to comments you disagree with (explain your reasoning)
6. ✅ Mark conversations as resolved when fixed
7. 🔄 Push updates to trigger re-review

### Responding to AI Comments

**If you agree:**
```
Good catch! Fixed in latest commit.
```

**If you disagree:**
```
I understand the concern, but in this case [explain your reasoning].
The current approach is preferred because [justification].
```

**If you need clarification:**
```
Could you elaborate on why this is an issue? 
I'm not sure I understand the suggested improvement.
```

## Configuration

### Customizing Review Behavior

Edit [.github/ai-review-config.yml](../.github/ai-review-config.yml):

```yaml
# Note: AI model is now managed by the centralized service
# You can still customize file exclusions and thresholds

# Adjust complexity thresholds
thresholds:
  max_complexity: 15  # Allow more complex functions
  min_maintainability: 60  # Lower maintainability threshold

# Exclude additional files
files:
  exclude:
    - "**/tests/**"  # Skip test files
    - "**/migrations/**"  # Skip database migrations
```

### Disabling Specific Checks

In [.github/workflows/ai-code-review.yml](../.github/workflows/ai-code-review.yml):

```yaml
# Comment out jobs you don't want:
jobs:
  ai-code-review:
    # ... keep this for main AI review
  
  # ai-code-quality-check:
  #   # ... disable static analysis
  
  python-code-complexity:
    # ... keep complexity checks
```

## Troubleshooting

### "AI review didn't run"

**Possible causes:**
- PR only modifies excluded files (JSON, MD, etc.)
- Workflow file has syntax errors
- `OPENAI_API_KEY` secret not configured
**Solution:**
1. Check Actions tab for error messages (look for 401 Unauthorized)
2. Verify `AI_REVIEW_ACCESS_TOKEN` is set in repository secrets
3. Confirm access token is valid (contact admin if expired)
4. Verify network connectivity to review service
5. Ensure workflow file is valid YAML
6. Check service status with your admin team

### "Service unavailable"

**Symptoms:**
- HTTP 404 (endpoint not found)
- HTTP 500 (server error - "Cannot read properties of undefined")
- HTTP 500+ errors
- Timeout errors
- "Service unavailable" messages

**Solutions:**
- If 404: Verify endpoint is `/api/review/pr` for PR reviews
- If 500 with "Cannot read properties": Check JSON payload format
  - Ensure `pr_number` is a number (not string)
  - Verify `files` array is not empty
  - Check all fields are properly escaped
- Contact service admin team
- Check service status page
- Verify endpoint URL in workflow file

**Correct API endpoints:**
- PR Reviews: `POST /api/review/pr` (default for this workflow)
- Batch by pattern: `POST /api/batch-review/by-pattern`
- Batch explicit: `POST /api/review/batch`

### "Too many comments from AI"

**Solution:**
- Reduce `max_comments` in config file
- Improve code quality before opening PR
- Split large PRs into smaller ones

### "Review comments are not helpful"

**Solution:**
- Contact your admin team to adjust centralized service settings
- Provide feedback on review quality for service improvements
- Customize file exclusions and thresholds in local config file

### "OpenAI API quota exceeded"

**Solution:**
- Not applicable - using centralized service
- Contact your admin team if service is experiencing issues

## Cost Management

### Typical Costs

| PRNo Individual Costs!

The centralized AI review service is **managed centrally** with no individual team costs:

- ✅ **No API keys required** - Authentication managed by service
- ✅ **No billing to manage** - Costs handled at organizational level
- ✅ **No quota limits** - Service scales automatically
- ✅ **Unlimited reviews** - Use as much as needed

### Service Benefits

- **Consistent Quality**: Same AI models and review standards
- **Optimized Performance**: Dedicated infrastructure for fast reviews
- **Managed Updates**: AI improvements deployed automatically
- **Enterprise Features**: Advanced security, compliance, and audit logging

Contact your admin team for questions about the centralized service.

### Cyclomatic Complexity

Measures the number of linearly independent paths through code:
- **1-5**: Simple, easy to test
- **6-10**: More complex, manageable
- **11-20**: Complex, hard to test
- **21+**: Very complex, definitely refactor

### Maintainability Index

Calculated from:
- Halstead Volume (program length)
- Cyclomatic Complexity
- Lines of Code
- Comment percentage

Scale:
- **0-9**: Red (difficult to maintain)
- **10-19**: Yellow (moderate difficulty)
- **20-100**: Green (maintainable)

## FAQ

**Q: Can I skip AI review for a specific PR?**
A: Yes, add `[skip ai-review]` to your PR title.

**Q: How long does AI review take?**
A: Usually 2-5 minutes, depending on PR size and API response time.

**Q: Can AI review approve/merge PRs?**
A: No, it only provides comments. Human approval is still required.

**Q: What if AI suggests something wrong?**
A: AI can make mistakes. Use your judgment and explain in comments why you're not following a suggestion.

**Q: Can I run AI review locally?**
A: Not currently. The workflow is designed to run in GitHub Actions.
 using the centralized service.

**Q: Is my code sent to a third party?**
A: Yes, code diffs are sent to the centralized AI review service. The service is managed by your organization and follows enterprise security and compliance standard
## Support

- 📚 Full documentation: [.github/workflows/README.md](../.github/workflows/README.md)- 🚦 Quality gate details: [QUALITY_GATE_GUIDE.md](QUALITY_GATE_GUIDE.md)- � Multi-repo management: [MULTI_REPO_MANAGEMENT.md](MULTI_REPO_MANAGEMENT.md)
- �🐛 Issues with workflows: Check Actions tab for logs
- 💬 Questions: Open an issue or ask in pull request comments
- 🔧 Configuration help: See [ai-review-config.yml](../.github/ai-review-config.yml)

---

**Remember**: AI code review is a tool to assist you, not replace human judgment. Always critically evaluate suggestions and use your expertise to make final decisions.
