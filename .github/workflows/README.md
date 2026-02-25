# GitHub Actions Workflows

This directory contains automated workflows for continuous integration, deployment, and AI-powered code review.

## Available Workflows

### 1. AI Code Review (`ai-code-review.yml`)

Automatically reviews pull requests using a centralized AI review service and static analysis tools.

**Features:**
- 🤖 AI-powered code review using enterprise-grade centralized service
- 🔍 Static code analysis with pylint and flake8
- 📊 Code complexity analysis
- 🔒 Security scanning with Trivy
- 🐳 Docker best practices validation
- ⚠️ Detection of debugging statements and TODOs
- 🚨 Security pattern checks for hardcoded credentials

**Setup:**

1. **Add Access Token:**
   - Go to repository Settings → Secrets and variables → Actions
   - Add a new secret named `AI_REVIEW_ACCESS_TOKEN`
   - Paste the access token provided by your admin team

**Centralized Review Service:**
- URL: `http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com`
- API Endpoint: `POST /api/review/pr` (for PR reviews)
- Authentication: x-access-token header
- Managed AI models and prompts
- Enterprise-grade infrastructure

**Available API Endpoints:**
- `POST /api/review/pr` - PR changed files (used by this workflow)
- `POST /api/batch-review/by-pattern` - Scheduled/branch scans
- `POST /api/review/batch` - Explicit file lists

**The workflow automatically runs on:**
- New pull requests
- Updates to existing pull requests
- Reopened pull requests

**Configure review settings (optional):**
- Edit `.github/ai-review-config.yml` to customize:
  - Review detail level
  - File patterns to include/exclude
  - Quality thresholds
  - Security check patterns

**Jobs:**

- **ai-code-review**: Sends code to centralized AI review service and posts comments
- **ai-code-quality-check**: Python-specific quality analysis
- **python-code-complexity**: Complexity and maintainability metrics
- **ai-security-scan**: Security vulnerability scanning

**Required Secrets:**
- `AI_REVIEW_ACCESS_TOKEN` - Access token for centralized AI review service (obtain from admin)
- `GITHUB_TOKEN` - Automatically provided by GitHub Actions (no setup needed)

---

### 2. Continuous Integration (`ci.yml`)

Runs automated tests on pull requests.

**Status:** Currently commented out

To enable:
1. Uncomment the workflow in `ci.yml`
2. Ensure your `Makefile` has `up` and `ci` targets defined

---

### 3. Continuous Deployment (`cd.yml`)

Deploys to EC2 instance when code is merged to main.

**Status:** Currently commented out

To enable:
1. Uncomment the workflow in `cd.yml`
2. Add the following secrets to your repository:
   - `SERVER_SSH_KEY` - SSH private key for EC2 access
   - `REMOTE_HOST` - EC2 instance hostname or IP
   - `REMOTE_USER` - SSH username (typically `ubuntu`)

---

## Configuration Files

### `.github/ai-review-config.yml`

Configuration for AI code review behavior:
- Model selection (GPT-4, etc.)
- Review detail level
- File inclusion/exclusion patterns
- Quality thresholds
- Security check patterns
- Custom review prompts

### `.github/CODEOWNERS`

Defines code ownership for automatic review requests.

---

## Best Practices

1. **Keep workflows enabled:** Regularly review and update workflow configurations
2. **Monitor workflow runs:** Check the Actions tab for failures
3. **Update dependencies:** Keep action versions up to date
4. **Secure secrets:** Never commit API keys or credentials
5. **Review AI feedback:** Use AI reviews as guidance, not absolute rules
6. **Customize thresholds:** Adjust quality thresholds based on your team's standards

---

## Troubleshooting

### AI Code Review not running

- Check that the workflow file is in `.github/workflows/`
- Ensure pull requests are being opened (not direct pushes to main)
- Verify the centralized review service is accessible
- Check the Actions tab for network or connectivity errors

### Permission errors

- Verify the workflow has necessary permissions in the YAML file
- Check repository settings → Actions → General → Workflow permissions

### Review comments not appearing

- Check the Acentralized review service is responding (check server status)
- Ensure the pull request has actual code changes
- Check network connectivity to the review serverits
- Ensure the pull request has actual code changes

---entralized Service Benefits

The AI code review workflow uses a **centralized review service**:
- ✅ **No individual costs** - Service managed centrally
- ✅ **No API key management** - Authentication handled by service
- ✅ **Consistent reviews** - Same AI models and standards across all teams
- ✅ **Managed infrastructure** - Optimized for performance and reliability
- ✅ **Enterprise features** - Advanced security, compliance, and audit logging

**Service Endpoint:** `http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com`API_MODEL`)

Monitor your OpenAI usage at: https://platform.openai.com/usage

---

## Support

For issues with workflows:
1. Check the Actions tab for detailed error logs
2. Review the workflow YAML syntax
3. Verify all required secrets are configured
4. Check the official documentation for each action used
