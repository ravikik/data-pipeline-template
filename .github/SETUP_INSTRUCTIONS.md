# AI Code Review - Setup Instructions

## Quick Setup (5 minutes)

Follow these steps to enable AI code review for your repository.

---

## Step 1: Obtain Access Token

Contact your **admin team** or **platform team** to obtain the AI Review Service access token.

**What to request:**
- "AI Code Review Service Access Token"
- For repository: `[your-repository-name]`
- Service endpoint: `http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com`
- API endpoint: `POST /api/review/pr` (for PR reviews)

**Expected response:**
- You'll receive a token string (e.g., `abc123xyz...`)
- Keep this token secure - it's like a password

---

## Step 2: Add Secret to Repository

1. **Navigate to repository settings:**
   - Go to your GitHub repository
   - Click **Settings** (top navigation)
   
2. **Access secrets section:**
   - In left sidebar, expand **Secrets and variables**
   - Click **Actions**

3. **Add new secret:**
   - Click **New repository secret** button
   
4. **Configure secret:**
   - **Name:** `AI_REVIEW_ACCESS_TOKEN` (exact name, case-sensitive)
   - **Value:** Paste the token you received from admin team
   - Click **Add secret**

---

## Step 3: Verify Setup

1. **Create a test pull request:**
   ```bash
   git checkout -b test/ai-review-setup
   echo "# Test AI Review" >> README.md
   git add README.md
   git commit -m "test: verify AI review setup"
   git push origin test/ai-review-setup
   ```

2. **Open the PR:**
   - Go to your repository on GitHub
   - Click **Pull requests**
   - Click **New pull request**
   - Select your test branch
   - Create the pull request

3. **Check workflow execution:**
   - Go to **Actions** tab
   - Click on the workflow run for your PR
   - Look for **AI Code Review** job
   - Verify it completes successfully

4. **Expected results:**
   - ✅ "🚀 Sending code to centralized AI review server..."
   - ✅ "📡 Server response status: 200" (or 201)
   - ✅ "✅ Successfully received AI code review"
   - ✅ Review comments appear on the PR

---

## Troubleshooting

### ❌ HTTP 400 - Bad Request

**Error message:**
```
📡 Server response status: 400
Response: {"error":"files array is required"}
```

**Solutions:**
1. Ensure payload includes `files` array (not `changed_files`)
2. Verify JSON payload is properly formatted
3. Check that files array is not empty
4. Ensure all required fields are present:
   - `repository`
   - `pr_number`
   - `files` (array of file paths)

### ❌ HTTP 401 - Unauthorized

**Error message:**
```
📡 Server response status: 401
⚠️  Warning: Centralized review server returned status 401
Response: {"error":"Unauthorized. No authentication credentials provided."}
```

**Solutions:**
1. Verify secret name is exactly: `AI_REVIEW_ACCESS_TOKEN`
2. Check if token has expired - contact admin for new token
3. Ensure you added the secret to the correct repository
4. Try removing and re-adding the secret

### ❌ HTTP 404 - Not Found

**Error message:**
```
📡 Server response status: 404
Response: Cannot POST /api/review
```

**Solutions:**
1. Verify endpoint path is `/api/review/pr` for PR reviews
2. Check service documentation for correct endpoint
3. Contact admin team if endpoint has changed

**Correct endpoints:**
- PR Reviews: `POST /api/review/pr`
- Batch by pattern: `POST /api/batch-review/by-pattern`
- Batch explicit: `POST /api/review/batch`

### ❌ HTTP 500 - Server Error

**Error message:**
```
📡 Server response status: 500
Response: {"error":"Batch review failed","details":"Cannot read properties of undefined (reading 'split')"}
```

**Possible Causes:**
1. Server-side parsing issue with payload structure
2. Missing or null required fields
3. Incorrect data types in payload
4. Server expects different field names or structure
5. Incompatibility between workflow payload and server expectations

**Immediate Actions:**
1. **Check the debug output** in workflow logs for:
   - "📋 Payload structure:" - shows what's being sent
   - "🔍 Debug Info:" - shows first part of request

2. **Verify payload matches API spec:**
   ```json
   {
     "repository": "owner/repo",
     "pr_number": 123,        // Number, not string
     "pr_title": "...",
     "pr_description": "...", // Can be empty string
     "base_branch": "main",
     "head_branch": "feature/...",
     "author": "username",
     "diff": "...",           // Full git diff
     "files": ["file1.py"]    // Non-empty array
   }
   ```

3. **Contact admin team** with:
   - Workflow run URL
   - The "📋 Payload structure" output from logs
   - Error message details
   - Request them to check server-side logs for the specific error

**Workaround:**
If the issue persists, you can disable the AI review temporarily:
```yaml
# Comment out the AI review step in .github/workflows/ai-code-review.yml
# - name: Send to Centralized AI Review Server
#   if: steps.pr-info.outputs.has_changes == 'true'
```

### ❌ HTTP 403 - Forbidden

**Error message:**
```
📡 Server response status: 403
```

**Solutions:**
1. Token may be valid but doesn't have access to this repository
2. Contact admin team to verify token permissions
3. Ensure repository is authorized for the service

### ❌ HTTP 500 - Server Error

**Error message:**
```
📡 Server response status: 500
```

**Solutions:**
1. Service may be experiencing issues
2. Contact admin team to check service status
3. Try again in a few minutes

### ❌ Workflow doesn't run

**Solutions:**
1. Verify workflow file is in `.github/workflows/ai-code-review.yml`
2. Check workflow has correct YAML syntax
3. Ensure PR has actual code changes (not just docs)
4. Check repository Actions are enabled (Settings → Actions)

---

## Verification Checklist

Use this checklist to ensure everything is configured correctly:

- [ ] Obtained access token from admin team
- [ ] Added `AI_REVIEW_ACCESS_TOKEN` secret to repository
- [ ] Secret name is exactly `AI_REVIEW_ACCESS_TOKEN` (case-sensitive)
- [ ] Created test pull request
- [ ] Workflow ran successfully (check Actions tab)
- [ ] Saw "Server response status: 200" in logs
- [ ] Review comments appeared on PR (or no issues found message)
- [ ] Closed/merged test PR

---

## What Happens Next?

Once configured, the AI code review will:

✅ **Automatically run** on every pull request  
✅ **Analyze code changes** using enterprise AI models  
✅ **Post review comments** with suggestions and issues  
✅ **Check code quality** with static analysis  
✅ **Scan for security** vulnerabilities  
✅ **Measure complexity** and maintainability  

---

## Support

### Need help?

**For token issues:**
- Contact your admin/platform team
- Provide repository name and error message

**For workflow issues:**
- Check [.github/workflows/README.md](.github/workflows/README.md)
- Review [.github/AI_REVIEW_GUIDE.md](.github/AI_REVIEW_GUIDE.md)
- Check Actions tab for detailed error logs

**For service issues:**
- Contact admin team with:
  - Repository name
  - PR number
  - Timestamp of issue
  - Error messages from Actions log

---

## Security Notes

🔒 **Keep your access token secure:**
- Never commit tokens to code
- Never share tokens publicly
- Only add to GitHub Secrets
- Rotate tokens periodically (ask admin)

🔒 **Token permissions:**
- Token only allows posting review comments
- Token cannot modify code or settings
- Token is scoped to specific repositories

---

## Additional Resources

- **User Guide:** [.github/AI_REVIEW_GUIDE.md](.github/AI_REVIEW_GUIDE.md)
- **Full Documentation:** [.github/CENTRALIZED_SERVICE_SETUP.md](.github/CENTRALIZED_SERVICE_SETUP.md)
- **Contributing:** [CONTRIBUTING.md](CONTRIBUTING.md)
- **Main README:** [README.md](README.md#ai-code-review)

---

**Service Endpoint:** `http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com`

**Setup Status:** ✅ Complete once secret is added and verified
