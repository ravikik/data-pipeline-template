# AI Code Review - Quick Reference Guide - New1

## Overview

This repository uses an automated AI-powered code review system that connects to a **centralized review service** to analyze pull requests and provide intelligent feedback on code quality, security, and best practices.

**Centralized Service:** `http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com`

**Benefits:**
- ✅ Simple setup - just add access token secret
- ✅ No individual API costs to manage
- ✅ Consistent review standards across all teams
- ✅ Enterprise-grade AI models and infrastructure
- ✅ Managed updates and improvements

**Required Setup:**
- Add `AI_REVIEW_ACCESS_TOKEN` secret to your repository
- Obtain token from your admin team

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

## Best Practices for Working with AI Review

### Before Opening a PR

1. ✅ Run local tests: `make ci`
2. ✅ Check your code complexity: `radon cc . -a`
3. ✅ Run linters locally: `pylint your_file.py`
4. ✅ Remove debug statements
5. ✅ Add appropriate comments

### After Opening a PR

1. 📬 Wait for AI review to complete (~2-5 minutes)
2. 📖 Read all review comments carefully
3. 🔍 Address critical issues and warnings
4. 💬 Respond to comments you disagree with (explain your reasoning)
5. ✅ Mark conversations as resolved when fixed
6. 🔄 Push updates to trigger re-review

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

- 📚 Full documentation: [.github/workflows/README.md](../.github/workflows/README.md)
- 🐛 Issues with workflows: Check Actions tab for logs
- 💬 Questions: Open an issue or ask in pull request comments
- 🔧 Configuration help: See [ai-review-config.yml](../.github/ai-review-config.yml)

---

**Remember**: AI code review is a tool to assist you, not replace human judgment. Always critically evaluate suggestions and use your expertise to make final decisions.
